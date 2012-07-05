package com.socialcomputing.labs.bluekiwi.services;

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

@Path("/bluekiwi")
public class BluekiwiRestProvider {

    public static final String CLIENT_ID = "0093584955edfe9e5312";
    public static final String CLIENT_SECRET = "5ba5d71d6cbb3fe5a539";
    public static final String SUPER_TOKEN = "e169290f3065894c30e70d8aaa0033f2";
    //public static final String BK_URL = "http://partners.sandboxbk.net";
    public static final String BK_URL = "https://lecko.bluekiwi.net";
    
    
    public static final String CALLBACK_URL = "http://labs.just-map-it.com/bluekiwi/";
    public static final String AUTHORIZE_ENDPOINT = BK_URL + "/oauth2/authorize";
    public static final String TOKEN_ENDPOINT = BK_URL + "/oauth2/token";
    
    // Lecko beta user : justmapit_beta, id = 4293
    // Spaces of the beta users :
    //   - 2: Référentiel Lecko 
    //   - 26: Etudes & tendances 
    
    // public static final int SPACE_ID = 23;
    public static final int[] SPACES_ID = {2, 26};
    
    private static final Logger LOG = LoggerFactory.getLogger(BluekiwiRestProvider.class);
    
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
        StoreHelper storeHelper = new StoreHelper();
        
        try {
        	ObjectMapper mapper = new ObjectMapper();
        	ObjectNode q = mapper.createObjectNode();
        	q.put("text", query);
        	
        	// Filter with a list of spaces ids
        	ArrayNode spaces = q.putArray("destinationIds");
        	for(int space: SPACES_ID) {
        		spaces.add(space);	
        	}
        	      
            UrlHelper bluekiwiClient = new UrlHelper(POST, BluekiwiRestProvider.BK_URL + "/api/v3/post/_search");
            bluekiwiClient.addParameter("q", q.toString());
            bluekiwiClient.addParameter("access_token", token);
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
	                UrlHelper urlPost = new UrlHelper(BluekiwiRestProvider.BK_URL + "/api/v3/post/" + att.getId());
	                urlPost.addParameter("access_token", token);
	                urlPost.openConnections();
	                JsonNode cpost = mapper.readTree(urlPost.getStream());
	                addAuthor(storeHelper, cpost, att);
	                urlPost.closeConnections();
	                
	                
	                // Comments authors 
	                UrlHelper urlComments = new UrlHelper(BluekiwiRestProvider.BK_URL + "/api/v3/post/" + att.getId() + "/_reactions");
	                urlComments.addParameter("access_token", token);
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
    	UrlHelper urlHelper = new UrlHelper(UrlHelper.Type.POST, TOKEN_ENDPOINT);
    	urlHelper.addParameter("client_id", CLIENT_ID);
    	urlHelper.addParameter("client_secret", CLIENT_SECRET);
    	urlHelper.addParameter("code", code);
    	urlHelper.addParameter("redirect_uri", CALLBACK_URL);
    	urlHelper.addParameter("grant_type", "authorization_code");
    	
    	urlHelper.openConnections();
    	JsonNode tokenData = mapper.readTree(urlHelper.getStream());
    	String token = tokenData.get("access_token").getTextValue();
    	session.setAttribute("access_token", token);
    	return token;
    }
    

    /**
     * Helper function to add the supertoken with the appropriate 
     * sha1 signature before the api call
     * 
     * Warning, this function changes the http client state
     * 
     * @param urlHelper  the http client 
     * @param superToken super token to add 
     */
    public static UrlHelper addSuperToken(UrlHelper urlHelper, String superToken) {
    	// Signing the request
    	long now = new Date().getTime() / 1000;
    	
    	// Get a copy of urlHelper parameters
    	List<NameValuePair> parameters = new ArrayList<NameValuePair>(urlHelper.getParameters());
    	
    	// Add oauth_timestamp and access_token to the list
    	NameValuePair timestamp = new NameValuePair("oauth_timestamp", String.valueOf(now));
    	NameValuePair token = new NameValuePair("access_token", superToken);
    	parameters.add(timestamp);
    	parameters.add(token);
    	
    	// Order parameters by name, ascending
    	Collections.sort(parameters, new Comparator<NameValuePair>() {
            public int compare(NameValuePair e1, NameValuePair e2) {
                return e1.getName().compareTo(e2.getName());
            }
        });
    	
    	// Generates the sha1 signature of the parameters
    	String signParameters = "";
    	for(NameValuePair parameter : parameters) {
    		signParameters += "&" + parameter.getName() + "=" + parameter.getValue();  
    	}
    	signParameters = signParameters.substring(1);
    	String signature = HashUtil.getPHPSha1(BluekiwiRestProvider.CLIENT_ID + "&" + signParameters + "&" + BluekiwiRestProvider.CLIENT_SECRET);
    	
    	
    	switch (urlHelper.getType()) {
    	case GET:
    		urlHelper.addParameter(timestamp);
    		urlHelper.addParameter(token);
    		urlHelper.addParameter("oauth_signature", signature);
    		break;
    	case POST:
    		StringBuilder sb = new StringBuilder();
    		sb.append(urlHelper.getUrl());
    		try {
				sb.append("?").append(timestamp.getName()).append("=").append(URLEncoder.encode(timestamp.getValue(), "UTF-8"));
				sb.append("&").append(token.getName()).append("=").append(URLEncoder.encode(token.getValue(), "UTF-8"));
				sb.append("&").append("oauth_signature").append("=").append(URLEncoder.encode(signature, "UTF-8"));
    		} 
    		catch (UnsupportedEncodingException e) {
    			// Should not happen 
    		}
    		urlHelper.setUrl(sb.toString());
    		break;
    	}
    	return urlHelper;
    }
}