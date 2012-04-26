package com.socialcomputing.labs.klout;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.container.MappableContainerException;

@Path("/klout")
public class KloutRestProvider {

    private static final ObjectMapper mapper = new ObjectMapper();
    
    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request,
    		           @QueryParam("api-url") String apiURL, 
    		           @QueryParam("access-token") String accessToken,
    		           @QueryParam("maptype") String mapType,
    				   @QueryParam("param") String param) {
    	
    	final MapParameters mapParameters = new MapParameters(apiURL, accessToken, mapType);
        //HttpSession session = request.getSession(true);
        //String key = param;
        //String result = (String)session.getAttribute(key);
        //if (result == null || result.length() == 0) {
            String result = getInfluencees(mapParameters, param);
            //session.setAttribute(key, result);
        //}
        return result;
    }
    
    private String getInfluencees(MapParameters mapParameters, String userId) {
        StoreHelper storeHelper = new StoreHelper();
        try {
        	addEntity(storeHelper, mapParameters, userId, 3);
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }
    
    
    private static void addEntity(StoreHelper storeHelper, MapParameters mapParameters, String userId, int level) 
    		throws JsonProcessingException, IOException, JMIException {
    	
    	// lowest level reached, nothing todo
    	if(level <= 0) return;
    	
    	// Query the api 
    	UrlHelper urlNodes = new UrlHelper(mapParameters.getURL());
        urlNodes.addParameter("key", mapParameters.accessToken);
        urlNodes.addParameter("users", userId);
        try {
			// TODO : only add the entity if a result is found
        	// If the user was already processed, skip it
        	// Else add it in the store
        	if(storeHelper.getEntity(userId) == null) {
        		Entity ent = storeHelper.addEntity(userId);
        		ent.addProperty("name", userId);
        		
        		// Then query the api to get the user's influencees
        		urlNodes.openConnections();
        		JsonNode result = mapper.readTree(urlNodes.getStream());
        		if(result.has("users")) {
        			for (JsonNode user : (ArrayNode) result.get("users")) {
        				if(user.has(mapParameters.mapType)) {
        					JsonNode influencees = user.get(mapParameters.mapType);
        					for (JsonNode influencee : (ArrayNode) influencees) {
        						String twitterScreenName = influencee.get("twitter_screen_name").getTextValue();
        						Attribute att = storeHelper.addAttribute(twitterScreenName);
        						att.addProperty("name", twitterScreenName);
        						ent.addAttribute(att, influencee.get("kscore").getIntValue());
        						
        						addEntity(storeHelper, mapParameters, twitterScreenName, level - 1);
        					}
        				}            		
        			}
        		}
        	}
	        
		}
        finally {
        	try {
				urlNodes.closeConnections();
			} catch (JMIException e) {
				// Do nothing couldn't close the connection
			}
        }
    }

    
    private class MapParameters {
    	
    	public String baseURL;
    	public String accessToken;
    	public String mapType;
    	
    	public MapParameters(String baseURL, String accessToken, String mapType) {
    		this.baseURL = baseURL;
    		this.accessToken = accessToken;
    		this.mapType = mapType;
    	}
    	
    	public String getURL() {
    		String url = this.baseURL;
    		if(this.mapType.equalsIgnoreCase("influencees")) {
    			url += "/soi/influencer_of.json";
    		}
    		else if(this.mapType.equalsIgnoreCase("influencers")) {
    			url += "/soi/influenced_by.json";
    		}
    		return url;
    	}
    }
}
