package com.socialcomputing.labs.linkedin;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socialcomputing.labs.rest.LabsRest;
import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.OAuthHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

@Path("/linkedin")
public class RestProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LabsRest.class);
    
    public static final String API_KEY = "ejwzc9lafbwj";
    public static final String API_SECRET = "lNcgxo8yL1p0CsTp";
    public static final String REQUEST_TOKEN_URL = "https://api.linkedin.com/uas/oauth/requestToken";
    public static final String ACCESS_TOKEN_URL = "https://api.linkedin.com/uas/oauth/accessToken";
    public static final String OAUTH_URL = "https://www.linkedin.com/uas/oauth/authorize";
    public static final String CALLBACK = "https://labs.just-map-it.com/linkedin/map.jsp";
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @QueryParam("authtoken") String authToken, @QueryParam("authtokensecret") String authTokenSecret, @QueryParam("kind") String kind) {
        HttpSession session = request.getSession(true);
        String key = "skills";
        String result = ( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
            result = extract(authToken, authTokenSecret, kind);
            session.setAttribute( key, result);
        }
        return result;
    }
    
    private String extract(String authToken, String authTokenSecret, String kind) {
        StoreHelper dataStore = new StoreHelper();
        UrlHelper api = null;
        try {
            String secret = RestProvider.API_SECRET + "&" + authTokenSecret;
            
            OAuthHelper auth = OAuthHelper.GetOAuth(RestProvider.API_KEY, authToken);
            api = new UrlHelper("http://api.linkedin.com/v1/people/~:(id,formatted-name,headline,public-profile-url,skills)");
            auth.addOAuthParams(api, "GET", secret);
            //api.addHeader("Authorization", meAuth.getOAuthHeader(meUrl, "GET", secret));
            api.addHeader("x-li-format", "json");
            api.openConnections();
            JsonNode me = mapper.readTree(api.getStream());
            Attribute attribute = dataStore.addAttribute( me.get("id").getTextValue());
            attribute.addProperty("name", me.has("formattedName") ? me.get("formattedName").getTextValue() : "Xxxx Xxxx");
            attribute.addProperty("headline", me.has("headline") ? me.get("headline").getTextValue() : "");
            attribute.addProperty("url", me.has("publicProfileUrl") ? me.get("publicProfileUrl").getTextValue() : "");
            addSkills(dataStore, me, attribute, authToken, authTokenSecret);
            api.closeConnections();
            
            auth = OAuthHelper.GetOAuth(RestProvider.API_KEY, authToken);
            api = new UrlHelper("http://api.linkedin.com/v1/people/~/connections:(id,formatted-name,headline,public-profile-url,skills)");
            auth.addOAuthParams(api, "GET", secret);
            api.addHeader("x-li-format", "json");
            api.openConnections();
            JsonNode connections = mapper.readTree(api.getStream());
            for( JsonNode connection: (ArrayNode)connections.get("values")) {
                if( connection.has("skills")) {
                    attribute = dataStore.addAttribute( connection.get("id").getTextValue());
                    attribute.addProperty("name", connection.has("formattedName") ? connection.get("formattedName").getTextValue() : "Xxxx Xxxx");
                    attribute.addProperty("headline", connection.has("headline") ? connection.get("headline").getTextValue() : "");
                    attribute.addProperty("url", connection.has("publicProfileUrl") ? connection.get("publicProfileUrl").getTextValue() : "");
                    addSkills(dataStore, connection, attribute, authToken, authTokenSecret);
                }
            }
            api.closeConnections();
        }
        catch (JMIException e) {
            String r = api.getResult();
            StringBuilder sb = new StringBuilder();
            sb.append("Linkedin response\n").append(r);
            sb.append("\nLinkedin headers\n");
            Map<String,List<String>> map = api.getConnection().getHeaderFields();
            if( map != null) {
                for(String k: map.keySet()) {
                    sb.append(k).append(": ");
                    for( String v : map.get(k)) {
                        sb.append(v).append(" ");
                    }
                    sb.append("\n");
                }
            }
            LOG.debug(sb.toString());
            return StoreHelper.ErrorToJson( api.getResponseCode(), r, null);
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return dataStore.toJson();
    }
    
    void addSkills(StoreHelper dataStore, JsonNode person, Attribute attribute, String authToken, String authTokenSecret) {
        if( person.has("skills")) {
            JsonNode skills = person.get("skills");
            if( skills.has("values")) {
                for( JsonNode skill: (ArrayNode)skills.get("values")) {
                    skill = skill.get("skill");
                    if( skill != null) {
                        Entity entity = dataStore.addEntity( skill.get("name").getTextValue());
                        entity.addProperty("name", entity.getId());
                        entity.addAttribute(attribute, 1);
                    }
                }
            }
        }
    }
    
    /*void getConnections(StoreHelper dataStore, String id, String authToken, String authTokenSecret) {
        try {
            OAuthHelper auth = OAuthHelper.GetOAuth(RestProvider.API_KEY, authToken);
            UrlHelper api = new UrlHelper("http://api.linkedin.com/v1/people/id=" + id + "/connections:(id,first-name,last-name)");
            auth.addOAuthParams(api, "GET", RestProvider.API_SECRET + "&" + authTokenSecret);
            api.addHeader("x-li-format", "json");
            api.openConnections();
            JsonNode connections = mapper.readTree(api.getStream());
            for( JsonNode connection: (ArrayNode)connections.get("values")) {
                Attribute attribute = dataStore.addAttribute( connection.get("id").getTextValue());
                attribute.addProperty("name", connection.get("firstName").getTextValue() + " " + connection.get("lastName").getTextValue());
    
                Entity entity = dataStore.addEntity( connection.get("id").getTextValue());
                entity.addProperty("name", connection.get("firstName").getTextValue() + " " + connection.get("lastName").getTextValue());
                entity.addAttribute(attribute, 1);
            }
            api.closeConnections();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
    
    /*
    @Path("image-proxy")
    @Produces("image/*")
    public Response getThumbnail( @HeaderParam("Accept-Encoding") String encoding, @HeaderParam("If-Modified-Since") String cache, @HeaderParam("If-Modified-Since") String modified, @HeaderParam("If-None-Match") String match, @QueryParam("url") String url) {
        UrlHelper urlHelper = new UrlHelper( url);
        try {
            urlHelper.addHeader( "Accept-Encoding", encoding);
            urlHelper.addHeader( "If-Modified-Since", modified);
            urlHelper.addHeader( "If-None-Match", match);
            urlHelper.addHeader( "Cache-Control", cache);
            urlHelper.openConnections();
            HttpURLConnection connection = (HttpURLConnection) urlHelper.getConnection();
            String tag = connection.getHeaderField( "Etag");
            if( tag == null) tag = "";
            if( tag.startsWith("\"")) tag = tag.substring( 1);
            if( tag.endsWith("\"")) tag = tag.substring( 0, tag.length()-1);
            return Response.ok( urlHelper.getStream(), urlHelper.getContentType())
                .lastModified( new Date( connection.getLastModified()))
                .tag( tag)
                .status( connection.getResponseCode())
                .header( "Content-Length", connection.getContentLength())
                .build();
        }
        catch (Exception e) {
            Response.status( Responses.NOT_FOUND);
        }
        return null;
    }
*/
    
    
}
