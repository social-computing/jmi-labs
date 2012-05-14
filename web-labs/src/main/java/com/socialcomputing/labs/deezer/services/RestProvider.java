package com.socialcomputing.labs.deezer.services;

import static com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper.Type.POST;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socialcomputing.labs.bluekiwi.utils.HashUtil;

import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.NameValuePair;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

@Path("/deezer")
public class RestProvider {

	public static final String APP_ID = "TODO";
	public static final String APP_SECRET = "YOUR_APP_SECRET";
	public static final String APP_PERMS = "email";
	public static final String CALLBACK_URL = "http://labs.just-map-it.com/deezer/";

	
    public static final String DEEZER_URL = "http://connect.deezer.com";
    public static final String AUTHORIZE_ENDPOINT = DEEZER_URL + "/oauth/auth.php";
    public static final String TOKEN_ENDPOINT = DEEZER_URL + "/oauth/access_token.php";
    
    
    private static final Logger LOG = LoggerFactory.getLogger(RestProvider.class);
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @DefaultValue("") @QueryParam("query") String query, @QueryParam("token") String token) {
        HttpSession session = request.getSession(true);
        String key = query;
        String result = null;//( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
           result = build(query, token);
           session.setAttribute( key, result);
        }
        return result;
    }
    
    String build(String query, String token) {
    	
    	// TODO : Start HERE ! 
    	
        StoreHelper storeHelper = new StoreHelper();
        
        try {
        	ObjectMapper mapper = new ObjectMapper();
        	ObjectNode q = mapper.createObjectNode();
        	q.put("text", query);
        	ArrayNode spaces = q.putArray("destinationIds");
        	// spaces.add(SPACE_ID);
        	      
            UrlHelper bluekiwiClient = new UrlHelper(POST, RestProvider.DEEZER_URL + "/api/v3/post/_search");
            bluekiwiClient.addParameter("q", q.toString());
            bluekiwiClient.addParameter("oauth_token", token);
            bluekiwiClient.openConnections();
            
            JsonNode response = mapper.readTree(bluekiwiClient.getStream());

        	/*
	       	 {"fieldsMask":"list",
	       	  "items":[
	       	  	{"id":"5159",
	       	  	 "type":"bookmark",
	       	  	 "url":"http:\/\/partners.sandboxbk.net\/people\/in\/Fayaz_Goulam\/conversations\/note?id=5159",
	       	  	 "title":"D\u00e9couvrez et installer le plugin Wordpress Just Map It! | Social Computing"
	       	  	}
	       	  ]
	       	 }
        	*/
            if(response.has("items")) {
            	JsonNode items = response.get("items");
            	LOG.debug("number of results : {}", items.size());
            	
                // Iterate through posts results
            	for(JsonNode item : (ArrayNode) items) {
            		Attribute att = storeHelper.addAttribute(item.get("id").getTextValue());
            		LOG.debug("content found with id = {}", att.getId());
            		att.addProperty("name", item.get("title").getTextValue());
            		att.addProperty("url", item.get("url").getTextValue());
            	
            	
	            	// Author
	                UrlHelper urlPost = new UrlHelper(RestProvider.DEEZER_URL + "/api/v3/post/" + att.getId());
	                urlPost.addParameter("oauth_token", token);
	                urlPost.openConnections();
	                JsonNode cpost = mapper.readTree(urlPost.getStream());
	                addAuthor(storeHelper, cpost, att);
	                urlPost.closeConnections();
	                
	                
	                // Comments authors 
	                UrlHelper urlComments = new UrlHelper(RestProvider.DEEZER_URL + "/api/v3/post/" + att.getId() + "/_reactions");
	                urlComments.addParameter("oauth_token", token);
	                urlComments.openConnections();
	                JsonNode commentsResponse = mapper.readTree(urlComments.getStream());
	                if(commentsResponse.has("items")) {
		                JsonNode comments = commentsResponse.get("items");
		                for(JsonNode comment : (ArrayNode) comments) {
		                	addCommentAuthor(storeHelper, comment, att);
		                }
	                }
	                urlComments.closeConnections();
            	}
            }
            bluekiwiClient.closeConnections();
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }
    
    
    /**
     * Helper function to add the content author to the entities
     * The content can be a post or a comment
     * 
     * @param storeHelper an instance of a StoreHelper used to manipulate and construct the jmi json format for the RestEntityConnector
     * @param content     a JsonNode of the current content being read
     * @param att         the attribute (post) to which this entity is linked 
     */
    public static void addAuthor(StoreHelper storeHelper, JsonNode content, Attribute att) {
    	JsonNode author = content.get("author");
    	LOG.debug("author found with id = {}", author.get("id").getTextValue());
        Entity ent = storeHelper.addEntity(author.get("id").getTextValue());
        ent.addProperty("name", "" + author.get("firstName").getTextValue() + " " + author.get("lastName").getTextValue());
        ent.addProperty("url", author.get("url").getTextValue());
        ent.addAttribute(att, 1); 
    }
    
    /**
     * Helper function to add all comments author to the entities
     * It is recursively called for nasted comments
     * 
     * @param storeHelper an instance of a StoreHelper used to manipulate and construct the jmi json format for the RestEntityConnector
     * @param content     a JsonNode of the current comment being read
     * @param att         the attribute (post) to which this entity is linked 
     */
    public static void addCommentAuthor(StoreHelper storeHelper, JsonNode content, Attribute att) {
    	addAuthor(storeHelper, content, att);
    	JsonNode reactions = content.get("reactions");
    	if(!reactions.isNull()) {
    		for(JsonNode reaction : (ArrayNode) reactions) {
    			addCommentAuthor(storeHelper, reaction, att);
    		}
    	}
    }
    
    /**
     * Helper function used to get a user access token from a 
     * code returned by bluekiwi after authentication
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
    	urlHelper.addParameter("app_secret", APP_SECRET);
    	urlHelper.addParameter("code", code);
    	urlHelper.addParameter("redirect_uri", CALLBACK_URL);
    	
    	urlHelper.openConnections();
    	String access_token = urlHelper.getResult();
       	session.setAttribute("access_token", access_token);
    	return access_token;
    }
}