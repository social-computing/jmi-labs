package com.socialcomputing.labs.knowledgeplaza;

import java.net.HttpURLConnection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.Responses;

@Path("/knowledgeplaza")
public class KPlazaRestProvider {

    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/{kind}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @PathParam("kind") String kind) {
        HttpSession session = request.getSession(true);
        String key = kind;
        String result = ( String)session.getAttribute( key);
        if (true) {//result == null || result.length() == 0) {
            if( kind.equalsIgnoreCase( "tile")) {
                result = tile_tag();
            }
            session.setAttribute( key, result);
        }
        return result;
    }

    @GET
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

    private String tile_tag() {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlHelper = new UrlHelper( "http://kp.url.com/api/stores");
            urlHelper.setBasicAuth( "fvaletas@social-computing.com", "jules2000");
            urlHelper.addParameter( "stores", "{\"stores\": [\"tiles\"], \"query\": { \"tags\": [], \"text\": \"test\" }, \"tiles\": { \"types\": [\"site\"], \"start\": 0, \"limit\": 1}})");
            urlHelper.openConnections();
            JsonNode movies = mapper.readTree(urlHelper.getStream());
            /*for (JsonNode movie : (ArrayNode) movies.get("feed").get("movie")) {
                Attribute attribute = storeHelper.addAttribute( movie.get("code").getValueAsText());
                attribute.addProperty("name", movie.get("title").getTextValue());
                
                UrlHelper urlHelper2 = new UrlHelper( "http://api.allocine.fr/rest/v3/movie");
                urlHelper2.addParameter( "format", "json");
                urlHelper2.addParameter( "code", attribute.getId());
                urlHelper2.openConnections();
                JsonNode movie2 = mapper.readTree(urlHelper2.getStream());
                for (JsonNode tag : (ArrayNode) movie2.get("movie").get("tag")) {
                    Entity entity = storeHelper.addEntity( tag.get("code").getValueAsText());
                    entity.addProperty("name", tag.get("$").getTextValue());
                    entity.addAttribute(attribute, 1);
                }
            }*/
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeHelper.toJson();
    }
    
}
