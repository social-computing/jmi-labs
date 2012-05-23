package com.socialcomputing.labs.deezer.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.Responses;

@Path("/deezer")
public class DeezerRestProvider {

	public static final String APP_ID = "101811";
	public static final String APP_SECRET = "853ab434e362306a6bc84f44afc04b71";
	public static final String APP_PERMS = "basic_access,email,manage_community,manage_library";
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
     * @param att      the attribute (post) to which this entity is linked
     *  
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
        			if(item.has("cover")) att.addProperty("image", item.get("cover").getTextValue());        				
        		}
        		else {
        			att.addProperty("name", item.get("name").getTextValue());
        			if(item.has("picture")) att.addProperty("image", item.get("picture").getTextValue());
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
     * code returned by deezer after authentication.
     * It stores the given access token and an expiration date in session 
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
    	
    	Map<String, String> parameters = UrlHelper.getParameters(urlHelper.getResult().replaceAll("(\\r|\\n)", "")); 
    	String access_token = parameters.get("access_token");
    
       	session.setAttribute("access_token", access_token);
       	
       	// Store expiration date
        DateTime expirationDate = new DateTime().plusSeconds(Integer.parseInt(parameters.get("expires")));
        session.setAttribute("expiration_date", expirationDate);
       	
    	return access_token;
    }

    
    /**
     * Check that the expiration date was not reached.
     * If it is the case, it discard the previously set access_token and expiration date from session
     *  
     * @param session the current user http session
     */
    public static void checkExpirationDate(HttpSession session) {
    	DateTime now = new DateTime();
    	DateTime expirationDate = (DateTime) session.getAttribute("expiration_date");
    	LOG.debug("Checking for access token expiration, now = {}, expires = {}", now, expirationDate);
    	// Expiration date reached
    	if(now.isAfter(expirationDate)) {
    		LOG.info("Expiration date reached, remove access token from session");
    		session.removeAttribute("access_token");
    		session.removeAttribute("expiration_date");
    	}
    }
    
    
    /**
     * Compares the state stored in the user session with the one provided 
     * in the response returned when a authentication code is asked with deezer api
     * 
     * @param session   the current user http session
     * @param state     state value given by deezer api call
     * @return boolean  true if the states are equals, false otherwise
     */
    public static boolean isStateValid(HttpSession session, String state) {
		String storedState = (String) session.getAttribute("state");
		LOG.debug("Comparing state in session {} with state returned by the request {}", storedState, state);
		return storedState != null && storedState.equals(state);
    }
}