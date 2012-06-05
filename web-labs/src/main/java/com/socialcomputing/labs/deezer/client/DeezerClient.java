/**
 * 
 */
package com.socialcomputing.labs.deezer.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.NameValuePair;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 */
public class DeezerClient {
	private final String deezerApiUrl;
	private final String accessToken;
	
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Logger LOG = LoggerFactory.getLogger(DeezerClient.class);
	
	public DeezerClient(String deezerApiUrl, String accessToken) {
		this.deezerApiUrl = deezerApiUrl;
		this.accessToken  = accessToken; 
		LOG.info("intializing client with url = {} and access token = {}", deezerApiUrl, accessToken);
	}
	
	/**
	 * Get the connected user profile information
	 * 
	 * @return
	 * @throws IOException 
	 * @throws JMIException 
	 * @throws JsonProcessingException 
	 */
	public User getMyProfile() 
			throws JsonProcessingException, JMIException, IOException {
    	JsonNode me = this.api("/user/me");
    	return new User(me.get("id").getTextValue(),
    			        me.get("name").getTextValue(), 
    			        me.get("link").getTextValue(),
    			        me.get("picture").getTextValue(),
    			        me.get("country").getTextValue());
	}

	
	public Collection<Album> getMyFavoriteAlbums(NameValuePair... parameters) 
			throws JMIException, JsonProcessingException, IOException {
		
		return this.getUserFavoriteAlbums("me", parameters);
	}

	
	public Collection<Album> getUserFavoriteAlbums(String userId, NameValuePair... parameters) 
			throws JMIException, JsonProcessingException, IOException {
		LOG.debug("Getting user {} favorite albums", userId);		
		JsonNode response = this.api("/user/" + userId + "/albums", parameters);
		
    	// Construct the list of albums from the data given in the json object
		return mapAlbumsFromJSON(response);
	}

	
	public Collection<Artist> getMyFavoriteArtists(NameValuePair... parameters) 
			throws JMIException, JsonProcessingException, IOException {
		
		return this.getUserFavoriteArtists("me", parameters);
	}
	
	
	
	public Collection<Artist> getUserFavoriteArtists(String userId, NameValuePair... parameters) 
			throws JMIException, JsonProcessingException, IOException {
		LOG.debug("Getting user {} favorite artists", userId);
		JsonNode artistsResponse = this.api("/user/" + userId + "/artists", parameters);
		    	
    	// Construct the list of artists from the data given in the json object
    	List<Artist> artists= new ArrayList<Artist>();
    	if(artistsResponse.has("data")) {
    		for(JsonNode artist : (ArrayNode) artistsResponse.get("data")) {
    			artists.add(new Artist(artist.get("id").getTextValue(), 
    					             artist.get("name").getTextValue(),
    					             artist.has("link") ? artist.get("link").getTextValue() : "",
    					             artist.has("picture") ? artist.get("picture").getTextValue() : ""));
    		}
    	}
    	return artists;
	}
	
	public Collection<User> getAlbumFans(String albumId, NameValuePair... parameters) 
			throws JsonProcessingException, JMIException, IOException {
		LOG.debug("Getting album {} fans", albumId);
		JsonNode fansResponse = this.api("/album/" + albumId + "/fans", parameters);
		return mapFansFromJSON(fansResponse);
	}
		
	
	public Collection<User> getArtistFans(String artistId, NameValuePair... parameters) 
			throws JMIException, JsonProcessingException, IOException {
		LOG.debug("Getting artist {} fans", artistId);
		JsonNode fansResponse = this.api("/artist/" + artistId + "/fans", parameters);
		return mapFansFromJSON(fansResponse);
	}
	
	public Collection<Artist> getRelatedArtists(String artistId, NameValuePair... parameters) 
			throws JMIException, JsonProcessingException, IOException {
		LOG.debug("Getting artist {} related artists", artistId);
		JsonNode response = this.api("/artist/" + artistId + "/related", parameters);
		// Construct the list of albums from the data given in the json object
		return mapArtistsFromJSON(response);
	}
	
	
	private JsonNode api(String path, NameValuePair... parameters) 
			throws JMIException, JsonProcessingException, IOException {
		// Constructing the complete url used to call the deezer service
		UrlHelper deezerConnection = new UrlHelper(this.deezerApiUrl + path);
		
		// Add the access token as one of the parameters
		deezerConnection.addParameter("access_token", this.accessToken);
		
		// Also pass the additional parameters given as argument
		for(NameValuePair parameter : parameters) {
			deezerConnection.addParameter(parameter);
		}
		
		// Connect to the endpoint, parse the response as a json tree and return a jsonnode 
		deezerConnection.openConnections();
    	JsonNode response = mapper.readTree(deezerConnection.getStream());
    	deezerConnection.closeConnections();
    	return response;
	}
	
	
	public static List<User> mapFansFromJSON(JsonNode fansNode) {
		List<User> fans = new ArrayList<User>();
    	if(fansNode.has("data")) {
	    	for(JsonNode fan : (ArrayNode) fansNode.get("data")) {
	    		fans.add(new User(fan.get("id").getTextValue(),
	    				fan.get("name").getTextValue(), 
	    				fan.get("link").getTextValue(),
	    				fan.get("picture").getTextValue()));
			}
    	}
    	return fans;
	}
	
	public static List<Album> mapAlbumsFromJSON(JsonNode albumsNode) {
		List<Album> albums = new ArrayList<Album>();
    	if(albumsNode.has("data")) {
	    	for(JsonNode album : (ArrayNode) albumsNode.get("data")) {
    			albums.add(new Album(album.get("id").getTextValue(), 
			             album.get("title").getTextValue(),
			             album.get("link").getTextValue(),
			             album.get("cover").getTextValue()));
			}
    	}
    	return albums;
	}
	
	public static List<Artist> mapArtistsFromJSON(JsonNode artistNode) {
		List<Artist> artists = new ArrayList<Artist>();
    	if(artistNode.has("data")) {
	    	for(JsonNode artist : (ArrayNode) artistNode.get("data")) {
    			artists.add(new Artist(artist.get("id").getTextValue(), 
    					artist.get("name").getTextValue(),
    					artist.get("link").getTextValue(),
    					artist.get("picture").getTextValue()));
			}
    	}
    	return artists;
	}
}
