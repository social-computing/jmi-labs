package com.socialcomputing.labs.deezer.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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

import org.codehaus.jackson.JsonProcessingException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.socialcomputing.labs.deezer.InvalidEnumerationElement;
import com.socialcomputing.labs.deezer.MapType;
import com.socialcomputing.labs.deezer.client.Album;
import com.socialcomputing.labs.deezer.client.Artist;
import com.socialcomputing.labs.deezer.client.DeezerClient;
import com.socialcomputing.labs.deezer.client.User;
import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.NameValuePair;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.Responses;

@Path("/deezer")
public class DeezerRestProvider {

	public static final String APP_ID = "101811";
	public static final String APP_SECRET = "853ab434e362306a6bc84f44afc04b71";
	public static final String APP_PERMS = "basic_access,email,manage_community,manage_library,delete_library";
	public static final String CALLBACK_URL = "http://labs.just-map-it.com/deezer/";

    public static final String DEEZER_CONNECT_URL = "http://connect.deezer.com";
    public static final String DEEZER_API_URL = "http://api.deezer.com/2.0";
    public static final String AUTHORIZE_ENDPOINT = DEEZER_CONNECT_URL + "/oauth/auth.php";
    public static final String TOKEN_ENDPOINT = DEEZER_CONNECT_URL + "/oauth/access_token.php";
    
    private static final Logger LOG = LoggerFactory.getLogger(DeezerRestProvider.class);
    
    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response kind(@Context HttpServletRequest request, @QueryParam("maptype") String maptype, 
    			                                              @QueryParam("access_token") String access_token) {
    	MapType mapType;
    	try {
    		
    		mapType = MapType.fromValue(maptype);
    	}
    	catch(InvalidEnumerationElement iee) {
    		return Response.status(Responses.CLIENT_ERROR).build();
        	// TODO : Add an error code and an error message
    	}
    	
        	
    	// Store the last generated map of each maptype in session (helps to center / navigate)
        // TODO : set an expiration date ?
    	HttpSession session = request.getSession(true);
        String result = (String) session.getAttribute(maptype);
        if (result == null || result.length() == 0) {
        	LOG.debug("Generating map for type = {}", mapType);
            result = buildMap(mapType, access_token);
            session.setAttribute(maptype, result);
        }
        return Response.ok(result).build(); // result;
    }
    
    
    String buildMap(MapType maptype, String accessToken) {
    	StoreHelper storeHelper = new StoreHelper();
        try {
        	// Start by getting user id
        	DeezerClient dzClient = new DeezerClient(DEEZER_API_URL, accessToken);
        	User me = dzClient.getMyProfile();
        	
        	Collection<String> favIds = new HashSet<String>();
        	switch(maptype) {
	        	case ALBUM:
	        		Collection<Album> myFavAlbums = dzClient.getMyFavoriteAlbums();
	        		for(Album favAlbum : myFavAlbums) {
	        			favIds.add(favAlbum.id);
	        		}
	        		addUserByAlbum(storeHelper, dzClient, me, favIds, 1);
	        		break;
	        		
	        	case ARTIST:
	        		Collection<Artist> myFavArtists = dzClient.getMyFavoriteArtists();
	        		for(Artist favArtist : myFavArtists) {
	        			favIds.add(favArtist.id);
	        		}
	        		addUserByArtist(storeHelper, dzClient, me, favIds, 1);
	        		break;
	        		
	        	case RELARTIST:
	        		Collection<Artist> favArtists = dzClient.getMyFavoriteArtists();
	        		for(Artist favAlbum : favArtists) {
	        			favIds.add(favAlbum.id);
	        		}
	        		int i = 0;
	        		Iterator<Artist> it = favArtists.iterator();
	        		while(it.hasNext() && i < 10) {
	        			Artist a = it.next();
	        			i++;
	        			LOG.debug("Add related artists for user favorite artist = {}", a);
	        			addRelatedArtists(storeHelper, dzClient, a, favIds, 1);
	        		}
	        		break;
        	}
        	

        }
        catch (Exception e) {
        	LOG.error(e.getMessage(), e);
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }
    
    
    public static void addRelatedArtists(StoreHelper storeHelper, DeezerClient dzClient, Artist artist, Collection<String> favIds, int level) 
    		throws JsonProcessingException, JMIException, IOException {
    	
    	/*
    	if(storeHelper.getEntity(artist.id) != null) {
    		LOG.debug("Artist {} was already added, skipping...", artist);
    		return;
    	}
    	*/
    	
    	LOG.info("Add Artist {} to the attributes list", artist);
    	Attribute att = storeHelper.addAttribute(artist.id);
    	att.addProperty("name", artist.name);
    	att.addProperty("image", artist.picture);
    	att.addProperty("url", artist.link);
    	att.addProperty("infavlist", favIds.contains(artist.id));
    	
    	// Ask for this artist related artists
    	Collection<Artist> relatedArtists = dzClient.getRelatedArtists(artist.id, new NameValuePair("nb_items", "20"));
    	
    	
    	// Iterate through related artists and add them as the artist entities.
    	for(Artist relatedArtist : relatedArtists) {
    		Entity ent = storeHelper.getEntity(relatedArtist.id);

    		// If the artist was not already stored as entity create it
    		if(ent == null) {
    			ent = storeHelper.addEntity(relatedArtist.id);
    			ent.addProperty("name", relatedArtist.name);
    			ent.addProperty("image", relatedArtist.picture);
    			ent.addProperty("url", relatedArtist.link);
    			
    	    	// If this artist is in my favorite list set infavlist to true otherwise to false 
    	    	// It is usefull for the menu item state at the map initialization phase
    	    	ent.addProperty("infavlist", favIds.contains(relatedArtist.id));
    	    	
    			if(level > 0) {
    				// Call that function again for that related artist
    				addRelatedArtists(storeHelper, dzClient, relatedArtist, favIds, level - 1);
    			}
    		}
    		ent.addAttribute(att, 1);    		
    	}
	}

    
	/**
     * Add a user to the map entities and his favortie albums to the attributes list of that user
     * 
     * @param storeHelper
     * @param access_token
     * @param user
     * @param myFavoriteAlbums
     * @param level
     * 
     * @throws JMIException
     * @throws JsonProcessingException
     * @throws IOException
     */
    public static void addUserByAlbum(StoreHelper storeHelper, DeezerClient dzClient, User user,  Collection<String> favIds, int level) 
    		throws JMIException, JsonProcessingException, IOException {
    	
    	// Add the user given in paramaters to the map entities list
    	if(storeHelper.getEntity(user.id) != null) {
    		LOG.debug("user {} was already added, skipping...", user);
    		return;
    	}
    	LOG.info("Add user {} to the entities list", user);
    	Entity ent = storeHelper.addEntity(user.id);
    	ent.addProperty("name", user.name);
    	ent.addProperty("url", user.link);
    			
    	// Get 10 first user's favorite albums
    	Collection<Album> favAlbums = dzClient.getUserFavoriteAlbums(user.id, new NameValuePair("nb_items", "10"));
    	
    	
    	// Iterate through albums and add them as user attributes.
    	for(Album favAlbum : favAlbums) {
    		Attribute att = storeHelper.getAttribute(favAlbum.id);
    		// If the album was not already stored as attribute create it
    		// And go through this album fans
    		if(att == null) {
    			att = storeHelper.addAttribute(favAlbum.id);
    	    	att.addProperty("name", favAlbum.title);
    	    	att.addProperty("image", favAlbum.cover);
    	    	att.addProperty("url", favAlbum.link);
    			
    	    	// If this album is in my favorite list set infavlist to true otherwise to false 
    	    	// It is usefull for the menu item state at the map initialization
    	    	att.addProperty("infavlist", favIds.contains(favAlbum.id));
    	    	
    			if(level > 0) {
    				// For each album get a list of fans
    				Collection<User> fans = dzClient.getAlbumFans(favAlbum.id, new NameValuePair("nb_items", "50"));
    				for(User fan : fans) {
    					addUserByAlbum(storeHelper, dzClient, fan, favIds, level - 1);
    				}
    			}
    		}
    		ent.addAttribute(att, 1);    		
    	}
    }
    
    public static void addUserByArtist(StoreHelper storeHelper, DeezerClient dzClient, User user,  Collection<String> favIds, int level) 
    		throws JMIException, JsonProcessingException, IOException {
    	
    	// Add the user given in paramaters to the map entities list
    	if(storeHelper.getEntity(user.id) != null) {
    		LOG.debug("user {} was already added, skipping...", user);
    		return;
    	}
    	LOG.info("Add user {} to the entities list", user);
    	Entity ent = storeHelper.addEntity(user.id);
    	ent.addProperty("name", user.name);
    	ent.addProperty("url", user.link);
    			
    	// Get user favorite albums
    	Collection<Artist> favArtists = dzClient.getUserFavoriteArtists(user.id, new NameValuePair("nb_items", "10"));
    	
    	
    	// Iterate through artits and add them as user attributes.
    	for(Artist favArtist: favArtists) {
    		Attribute att = storeHelper.getAttribute(favArtist.id);
    		// If the album was not already stored as attribute create it
    		// And go through this album fans
    		if(att == null) {
    			att = storeHelper.addAttribute(favArtist.id);
    	    	att.addProperty("name", favArtist.name);
    	    	att.addProperty("image", favArtist.picture);
    	    	att.addProperty("url", favArtist.link);
    			
    	    	// If this artist is in my favorite list set infavlist to true otherwise to false 
    	    	// It is usefull for the menu item state at the map initialization
    	    	att.addProperty("infavlist", favIds.contains(favArtist.id));
    	    	
    			if(level > 0) {
    				// For each album get a list of fans
    				Collection<User> fans = dzClient.getArtistFans(favArtist.id, new NameValuePair("nb_items", "50"));
    				for(User fan : fans) {
    					addUserByArtist(storeHelper, dzClient, fan, favIds, level - 1);
    				}
    			}
    		}
    		ent.addAttribute(att, 1);    		
    	}
    }
    
    /*
    public static void addUser(StoreHelper storeHelper, String accessToken, String maptype, User user, Collection<String> favIds, int level) 
    		throws JMIException, JsonProcessingException, IOException {
    	
    	// Add the user given in paramaters to the map entities list
    	if(storeHelper.hasEntity(user.id)) {
    		LOG.debug("user {} was already added, skipping...", user);
    		return;
    	}
    	LOG.info("Add user {} to the entities list", user);
    	Entity ent = storeHelper.addEntity(user.id);
    	ent.addProperty("name", user.name);
    	ent.addProperty("url", user.link);
    			

    	DeezerClient dzClient = new DeezerClient(DEEZER_API_URL, accessToken);

    	Collection<Favorite> favorites = new ArrayList<Favorite>();
    	if(maptype.equalsIgnoreCase("artist")) {
    		// Get user favorite artists
    		Collection<Artist> favArtists = dzClient.getUserFavoriteArtists(user.id);
    		for(Artist artist : favArtists) {
    			favorites.add(new Favorite(artist.id, artist.name, artist.picture, artist.link));
    		}
    		
    	}
    	else {
    		Collection<Album> favAlbums = dzClient.getUserFavoriteAlbums(user.id);
    		for(Album album : favAlbums) {
    			favorites.add(new Favorite(album.id, album.title, album.cover, album.link));
    		}
    	}
    	
    	// Iterate through artits and add them as user attributes.
    	for(Favorite favorite: favorites) {
    		Attribute att;
    		// If the album was not already stored as attribute create it
    		// And go through this album fans
    		if(!storeHelper.hasAttribute(favorite.id)) {
    			att = storeHelper.addAttribute(favorite.id);
    	    	att.addProperty("name", favorite.name);
    	    	att.addProperty("image", favorite.image);
    	    	att.addProperty("url", favorite.url);
    			
    	    	// If this artist is in my favorite list set infavlist to true otherwise to false 
    	    	// It is usefull for the menu item state at the map initialization
    	    	att.addProperty("infavlist", favIds.containsKey(favorite.id));
    	    	
    			if(level > 0) {
    				// For each album get a list of fans
    				Collection<User> fans = dzClient.getArtistFans(favArtist.id);
    				for(User fan : fans) {
    					addUserByArtist(storeHelper, accessToken, fan, myFavoriteArtists, level - 1);
    				}
    			}
    		}
    		else {
    			att = storeHelper.getAttribute(favArtist.id);
    		}
    		ent.addAttribute(att, 1);    		
    	}

    }
    */
    
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
    	urlHelper.addParameter("output", "json");
    	urlHelper.openConnections();
    	
    	
    	// Reading data from the response body (path parameters formatted answer) :
    	Map<String, String> parameters = UrlHelper.getParameters(urlHelper.getResult().replaceAll("(\\r|\\n)", ""));
    	urlHelper.closeConnections();
    	
    	// Store user access token in session
    	String access_token = parameters.get("access_token");
       	session.setAttribute("access_token", access_token);
       	
       	// Store expiration date
        DateTime expirationDate = new DateTime().plusSeconds(Integer.parseInt(parameters.get("expires")));
        session.setAttribute("expiration_date", expirationDate);
       	
    	
    	// Trying to get the values from a JSON response
    	// Not working for now : need to validate this with deezer dev team
        /*
    	JsonNode response = mapper.readTree(urlHelper.getStream());
    	LOG.debug("Get access token returned json value: ", response.getTextValue());
    	urlHelper.closeConnections();
    	
    	String access_token = response.get("access_token").getTextValue();
    	session.setAttribute("access_token", access_token);
    	
    	DateTime expirationDate = new DateTime().plusSeconds(response.get("expires").getIntValue());
        session.setAttribute("expiration_date", expirationDate);
        */
        
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
    		session.removeAttribute("state");
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

    
    /**
     * Images proxy to be able to load images from the map 
     * 
     * @param encoding
     * @param cache
     * @param modified
     * @param match
     * @param url
     * 
     * @return 
     */
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
}