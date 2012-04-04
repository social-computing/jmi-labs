package com.socialcomputing.labs.twitter;

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

import com.socialcomputing.wps.server.planDictionnary.connectors.WPSConnectorException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.OAuthHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

@Path("/twitter")
public class RestProvider {
    
    public static final String API_KEY = "Sg5mYX9UDm6ZUAbJJ1Irgg";
    public static final String API_SECRET = "Z83bHSj0Qsle4UCdtcW0nA4kzcxYy0NAj9F1iQ";
    public static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    public static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    public static final String OAUTH_URL = "http://api.twitter.com/oauth/authorize";
    public static final String CALLBACK = "https://labs.just-map-it.com/twitter/";
    private static final ObjectMapper mapper = new ObjectMapper();
    
    
    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @QueryParam("authtoken") String authToken, @QueryParam("authtokensecret") String authTokenSecret, @QueryParam("query") String query) {
        HttpSession session = request.getSession(true);
        String key = query;
        String result = ( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
            result = extract(authToken, authTokenSecret, query);
            session.setAttribute( key, result);
        }
        return result;
    }
    
    private String extract(String authToken, String authTokenSecret, String query) {
        StoreHelper dataStore = new StoreHelper();
        UrlHelper api = null;
        try {
            String secret = RestProvider.API_SECRET + "&" + authTokenSecret;
            
            OAuthHelper auth = OAuthHelper.GetOAuth(RestProvider.API_KEY, authToken);
            api = new UrlHelper(" http://search.twitter.com/search.json");
            api.addParameter("rpp", "100");
            api.addParameter("q", query);
            api.addParameter("include_entities", "true");
            auth.addOAuthParams(api, "GET", secret);
            api.openConnections();
            JsonNode results = mapper.readTree(api.getStream());
            if( !results.has("error")) {
                for( JsonNode result : (ArrayNode) results.get("results")) {
                    Attribute attribute = dataStore.addAttribute( Integer.toString(result.get("id").getIntValue()));
                    attribute.addProperty("name", result.get("text").getTextValue());
                    if( result.has("entities")){
                        JsonNode entities = result.get("entities");
                        if( entities.has("hashtags")){
                            for( JsonNode hashtag : (ArrayNode) entities.get("hashtags")) {
                                Entity entity = dataStore.addEntity( hashtag.get("text").getTextValue());
                                entity.addProperty("name", "#" + entity.getId());
                                entity.addAttribute(attribute, 1);
                            }
                        }
                        if( entities.has("user_mentions")){
                            for( JsonNode user : (ArrayNode) entities.get("user_mentions")) {
                                Entity entity = dataStore.addEntity( Integer.toString(user.get("id").getIntValue()));
                                entity.addProperty("name", "@" + user.get("name").getTextValue());
                                entity.addAttribute(attribute, 1);
                            }
                        }
                    }
                }
            }
            else {
                return StoreHelper.ErrorToJson( 0, results.get("error").getTextValue(), null);
            }
        }
        catch (WPSConnectorException e) {
            return StoreHelper.ErrorToJson( api.getResponseCode(), api.getResult(), null);
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
    
}
