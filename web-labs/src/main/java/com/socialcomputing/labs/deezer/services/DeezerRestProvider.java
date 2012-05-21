package com.socialcomputing.labs.deezer.services;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

@Path("/deezer")
public class DeezerRestProvider {

	public static final String APP_ID = "101811";
	public static final String APP_SECRET = "853ab434e362306a6bc84f44afc04b71";
	public static final String APP_PERMS = "basic_access,email,manage_community";
	public static final String CALLBACK_URL = "http://labs.just-map-it.com/deezer/";

	
    public static final String DEEZER_CONNECT_URL = "http://connect.deezer.com";
    public static final String DEEZER_API_URL = "http://api.deezer.com/2.0";
    public static final String AUTHORIZE_ENDPOINT = DEEZER_CONNECT_URL + "/oauth/auth.php";
    public static final String TOKEN_ENDPOINT = DEEZER_CONNECT_URL + "/oauth/access_token.php";
    
    private static final Logger LOG = LoggerFactory.getLogger(DeezerRestProvider.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @QueryParam("maptype") String maptype, 
    			                                            @QueryParam("access_token") String access_token) {
        // HttpSession session = request.getSession(true);
        //String key = query;
        String result = null;//( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
        	// TODO : Sanitize inputs (maptype)
        	// Should throw an exception -> HTTP Error -> invalid input
        	LOG.debug("maptype = {}", maptype);
            result = build(maptype, access_token);
            //session.setAttribute( key, result);
        }
        return result;
    }
    
    String build(String maptype, String access_token) {
        StoreHelper storeHelper = new StoreHelper();
        try {
        	ObjectMapper mapper = new ObjectMapper();
        	
        	// Start by getting user id
        	UrlHelper deezerClient = new UrlHelper(DeezerRestProvider.DEEZER_API_URL + "/user/me");
        	deezerClient.addParameter("access_token", access_token);
        	deezerClient.openConnections();
        	JsonNode me = mapper.readTree(deezerClient.getStream());
        	deezerClient.closeConnections();

        	addUser(storeHelper, access_token, maptype, me, 1);
        }
        catch (Exception e) {
        	LOG.error(e.getMessage(), e);
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }

    
    
    
    /**
     * Helper function to add a friend as an entity and to explore recursively his prefered albums or artists
     * 
     * @param storeHelper an instance of a StoreHelper used to manipulate and construct the jmi json format for the RestEntityConnector
     * @param user     a JsonNode of the current content being read
     * @param att         the attribute (post) to which this entity is linked 
     * @throws JMIException 
     * @throws IOException 
     * @throws JsonProcessingException 
     */
    public static void addUser(StoreHelper storeHelper, String access_token, String maptype, JsonNode user, int level) 
    		throws JMIException, JsonProcessingException, IOException {
    	String userId = user.get("id").getTextValue();
    	LOG.debug("Add user with id = {}", userId);
        Entity ent = storeHelper.addEntity(userId);
        ent.addProperty("name", user.get("name").getTextValue());
        ent.addProperty("url", user.get("link").getTextValue());
        
    	// With the user id, get his favorites albums and query for the fans
    	UrlHelper deezerClient = new UrlHelper(DeezerRestProvider.DEEZER_API_URL + "/user/" + userId + "/" + maptype + "s");
    	deezerClient.addParameter("access_token", access_token);
    	deezerClient.addParameter("nb_items", "10");
    	deezerClient.openConnections();
    	JsonNode albumsResponse = mapper.readTree(deezerClient.getStream());
    	deezerClient.closeConnections();
    	
    	if(albumsResponse.has("data")) {
    		JsonNode data = albumsResponse.get("data");
    		
    		// Iterate through albums
    		LOG.debug("number of " + maptype + "s for user {} : {}", userId, albumsResponse.get("total").getIntValue());
        	for(JsonNode item : (ArrayNode) data) {
        		String id = item.get("id").getTextValue();
        		LOG.debug("Add {} with id = {}", maptype, id);
        		Attribute att = storeHelper.addAttribute(id);
        		if("album".equalsIgnoreCase(maptype)) {
        			att.addProperty("name", item.get("title").getTextValue());        			
        		}
        		else {
        			att.addProperty("name", item.get("name").getTextValue());
        		}
        		if(item.has("link")) {
        			att.addProperty("url", item.get("link").getTextValue());
        		}
        		ent.addAttribute(att, 1);
        		
        		// For each album get a list of fans
        		if(level > 0) {
	        		deezerClient = new UrlHelper(DeezerRestProvider.DEEZER_API_URL + "/" + maptype + "/" + id + "/fans");
	            	deezerClient.addParameter("access_token", access_token);
	            	deezerClient.addParameter("nb_items", "50");
	            	deezerClient.openConnections();
	            	JsonNode fansResponse = mapper.readTree(deezerClient.getStream());
	            	deezerClient.closeConnections();
	            	
	            	if(fansResponse.has("data")) {
	            		// Iterate through fans and get a list of favorite albums
	            		JsonNode fans = fansResponse.get("data");
	            		LOG.debug("number of fans for " + maptype + "{} : {}", id, fansResponse.get("total").getIntValue());
	            		for(JsonNode fan : (ArrayNode) fans) {
	            			addUser(storeHelper, access_token, maptype, fan, level - 1);
	            		}
	            	}
        		}
        	}
    	}
    }
    
    
    /**
     * Helper function used to get a user access token from a 
     * code returned by deezer after authentication
     * 
     * @param code     the retruned code
     * @param session  the current user http session
     * @return         an access token
     * 
     * @throws JsonProcessingException
     * @throws IOException
     * @throws WPSConnectorException
     */
    public static String getAccessToken(String code, HttpSession session) 
    		throws JsonProcessingException, IOException, JMIException {
    	UrlHelper urlHelper = new UrlHelper(TOKEN_ENDPOINT);
    	urlHelper.addParameter("app_id", APP_ID);
    	urlHelper.addParameter("secret", APP_SECRET);
    	urlHelper.addParameter("code", code);
    	urlHelper.openConnections();
    	
    	Map<String, String> parameters = UrlHelper.getParameters(urlHelper.getResult()); 
    	String access_token = parameters.get("access_token");
    
       	session.setAttribute("access_token", access_token);
       	// TODO : Store expiration date ?
       	
    	return access_token;
    }
}