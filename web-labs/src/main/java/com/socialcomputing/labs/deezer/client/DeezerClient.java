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
		LOG.debug("getting my profile information");
		UrlHelper deezerConnection = new UrlHelper(this.deezerApiUrl + "/user/me");
		deezerConnection.addParameter("access_token", this.accessToken);
		deezerConnection.openConnections();
    	JsonNode me = mapper.readTree(deezerConnection.getStream());
    	deezerConnection.closeConnections();
    	return new User(me.get("id").getTextValue(),
    			        me.get("name").getTextValue(), 
    			        me.get("link").getTextValue(),
    			        me.get("picture").getTextValue(),
    			        me.get("country").getTextValue());
	}
	
	public Collection<Album> getMyFavoriteAlbums() 
			throws JMIException, JsonProcessingException, IOException {
		
		return this.getUserFavoriteAlbums("me");
	}
	
	
	public Collection<Album> getUserFavoriteAlbums(String userId) 
			throws JMIException, JsonProcessingException, IOException {
		LOG.debug("getting user {} favorite albums", userId);
		// Open connection to the remote server
		UrlHelper deezerConnection = new UrlHelper(this.deezerApiUrl + "/user/" + userId + "/albums");
    	deezerConnection.addParameter("access_token", this.accessToken);
    	deezerConnection.addParameter("nb_items", "10");
    	deezerConnection.openConnections();
    	
    	// Parse the reponse as a json tree with jackson and close the connection
    	JsonNode albumsResponse = mapper.readTree(deezerConnection.getStream());
    	deezerConnection.closeConnections();	
    	
    	// Construct the list of albums from the data given in the json object
    	List<Album> albums= new ArrayList<Album>();
    	if(albumsResponse.has("data")) {
    		for(JsonNode album : (ArrayNode) albumsResponse.get("data")) {
    			albums.add(new Album(album.get("id").getTextValue(), 
    					             album.get("title").getTextValue(),
    					             album.get("link").getTextValue(),
    					             album.get("cover").getTextValue()));
    		}
    	}
    	return albums;
	}
	
	
	public Collection<Artist> getMyFavoriteArtists() 
			throws JMIException, JsonProcessingException, IOException {
		
		return this.getUserFavoriteArtists("me");
	}
	
	public Collection<Artist> getUserFavoriteArtists(String userId) 
			throws JMIException, JsonProcessingException, IOException {
		LOG.debug("getting user {} favorite artists", userId);
		// Open connection to the remote server
		UrlHelper deezerConnection = new UrlHelper(this.deezerApiUrl + "/user/" + userId + "/artists");
    	deezerConnection.addParameter("access_token", this.accessToken);
    	deezerConnection.addParameter("nb_items", "10");
    	deezerConnection.openConnections();
    	
    	// Parse the reponse as a json tree with jackson and close the connection
    	JsonNode artistsResponse = mapper.readTree(deezerConnection.getStream());
    	deezerConnection.closeConnections();	
    	
    	// Construct the list of albums from the data given in the json object
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
	
	public Collection<User> getAlbumFans(String albumId) 
			throws JMIException, JsonProcessingException, IOException {
		LOG.debug("getting album {} fans", albumId);
		UrlHelper deezerConnection = new UrlHelper(this.deezerApiUrl + "/album/" + albumId + "/fans");
		deezerConnection.addParameter("access_token", this.accessToken);
		deezerConnection.addParameter("nb_items", "50");
		deezerConnection.openConnections();
    	JsonNode fansResponse = mapper.readTree(deezerConnection.getStream());
    	deezerConnection.closeConnections();
    	
    	List<User> fans = new ArrayList<User>();
    	if(fansResponse.has("data")) {
	    	for(JsonNode fan : (ArrayNode) fansResponse.get("data")) {
	    		fans.add(new User(fan.get("id").getTextValue(),
	    				fan.get("name").getTextValue(), 
	    				fan.get("link").getTextValue(),
	    				fan.get("picture").getTextValue()));
			}
    	}
    	return fans;
	}

	public Collection<User> getArtistFans(String artistId) 
			throws JMIException, JsonProcessingException, IOException {
		LOG.debug("getting artist {} fans", artistId);
		UrlHelper deezerConnection = new UrlHelper(this.deezerApiUrl + "/artist/" + artistId + "/fans");
		deezerConnection.addParameter("access_token", this.accessToken);
		deezerConnection.addParameter("nb_items", "50");
		deezerConnection.openConnections();
    	JsonNode fansResponse = mapper.readTree(deezerConnection.getStream());
    	deezerConnection.closeConnections();
    	
    	List<User> fans = new ArrayList<User>();
    	if(fansResponse.has("data")) {
	    	for(JsonNode fan : (ArrayNode) fansResponse.get("data")) {
	    		fans.add(new User(fan.get("id").getTextValue(),
	    				fan.get("name").getTextValue(), 
	    				fan.get("link").getTextValue(),
	    				fan.get("picture").getTextValue()));
			}
    	}
    	return fans;
	}
}
