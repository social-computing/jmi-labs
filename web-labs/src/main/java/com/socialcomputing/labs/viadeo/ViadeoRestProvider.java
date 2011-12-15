package com.socialcomputing.labs.viadeo;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.social.Person;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.social.SocialHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.Responses;

@Path("/viadeo")
public class ViadeoRestProvider {

    public static final String CLIENT_ID = "SocialComputingMapViTBUC";
    public static final String CLIENT_SECRET = "bDKYAIKwD1Lys";
    public static final String APP_URL = "http://labs.just-map-it.com/viadeo/";
    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/{kind}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @PathParam("kind") String kind, @QueryParam("access_token") String token) {
        HttpSession session = request.getSession(true);
        String key = kind;
        String result = ( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
           result = kind(kind, token);
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

    private String kind(String kind, String token) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            SocialHelper socialHelper = new SocialHelper( storeHelper);
            
            // Liste contacts
            UrlHelper urlHelper = new UrlHelper();
            urlHelper.setUrl( "https://api.viadeo.com/me/contacts.json");
            urlHelper.addParameter( "limit", "50");
            urlHelper.addParameter( "user_detail", "none");
            urlHelper.addParameter( "access_token", token);
            urlHelper.openConnections();
            JsonNode node = mapper.readTree(urlHelper.getStream());
            List<String> friendslist = new ArrayList<String>();
            ArrayNode friends = (ArrayNode)node.get( "data");
            do {
                for( JsonNode friend : friends) {
                    Person person = socialHelper.addPerson( friend.get("id").getTextValue());
                    person.addProperty("name", friend.get("name").getTextValue());
                    person.addProperty("url", friend.get("link").getTextValue());
                    friendslist.add( person.getId());
                }
                friends = null;
                String next = node.get( "paging").get( "next").getTextValue();
                if( next != "") {
                    UrlHelper nextHelper = new UrlHelper();
                    nextHelper.setUrl( next);
                    nextHelper.openConnections();
                    node = mapper.readTree( nextHelper.getStream());
                    friends = (ArrayNode)node.get( "data");
                }
            } while (friends != null);  
            
            if( kind.equalsIgnoreCase( "contacts")) {
                // My self
                UrlHelper uh = new UrlHelper();
                uh.setUrl( "https://api.viadeo.com/me.json");
                uh.addParameter("access_token", token);
                uh.openConnections();
                JsonNode me = mapper.readTree(uh.getStream());
                storeHelper.addGlobal( "$MY_VIADEO_ID", me.get("id").getTextValue());
                
                // Les amis entre eux 
                for (String friend : friendslist) {
                    UrlHelper uh1 = new UrlHelper();
                    uh1.setUrl( "https://api.viadeo.com/" + friend + "/mutual_contacts.json");
                    uh1.addParameter( "limit", "50");
                    uh1.addParameter("access_token", token);
                    uh1.openConnections();
                    node = mapper.readTree( uh1.getStream());
                    friends = (ArrayNode)node.get( "data");
                    while( friends != null) {
                        for( JsonNode friendJson : friends) {
                            socialHelper.setFriendShip( friend, friendJson.get("id").getTextValue());
                        }
                        friends = null;
                        String next = node.get( "paging").get( "next").getTextValue();
                        if( next != "") {
                            UrlHelper nextHelper = new UrlHelper();
                            nextHelper.setUrl( next);
                            nextHelper.openConnections();
                            node = mapper.readTree( nextHelper.getStream());
                            friends = (ArrayNode)node.get( "data");
                        }
                    }
                }
                
                // Je suis ami avec tous mes amis
                //setFriendShip((String)me.get("id"), friendslist);

                // Delete entities with only one attribute 
                Set<String> toRemove = new HashSet<String>();
                for( Entity entity : storeHelper.getEntities().values()) {
                    if( entity.getAttributes().size() == 1) {
                        toRemove.add( entity.getId());
                    }
                }
                for( String id : toRemove) {
                    storeHelper.removeEntity( id);
                }
            }
            else {
                // My self
                UrlHelper uh = new UrlHelper();
                uh.setUrl( "https://api.viadeo.com/me.json");
                uh.addParameter("access_token", token);
                uh.openConnections();
                JsonNode me = mapper.readTree(uh.getStream());
                String myId = me.get("id").getTextValue();
                Attribute attribute = storeHelper.addAttribute( myId);
                attribute.addProperty( "name", me.get("name").getTextValue());
                attribute.addProperty( "url", me.get("link").getTextValue());
                storeHelper.addGlobal( "$MY_VIADEO_ID", myId);
                friendslist.add( myId);
                
                for( String friend : friendslist) {
                    UrlHelper urlHelper2 = new UrlHelper();
                    urlHelper2.setUrl( "https://api.viadeo.com/" + friend + "/" + kind + ".json");
                    urlHelper2.addParameter( "limit", "50");
                    urlHelper2.addParameter( "access_token", token);
                    urlHelper2.openConnections();
                    
                    JsonNode node2 = mapper.readTree(urlHelper2.getStream());
                    ArrayNode kinds = (ArrayNode)node2.get( "data");
                    if( kinds != null) {
                        for( JsonNode curkind : kinds) {
                            if (curkind.get("id") != null && curkind.get("name") != null) {
                                Entity entity = storeHelper.addEntity( curkind.get("id").getTextValue());
                                entity.addProperty( "name", curkind.get("name").getTextValue());
                                entity.addProperty( "url", curkind.get("link").getTextValue());
                                entity.addAttribute( attribute, 1);
                            }
                        }
                    }
                }
                // Delete entities with only one attribute 
                Set<String> toRemove = new HashSet<String>();
                for( Entity entity : storeHelper.getEntities().values()) {
                    if( entity.getAttributes().size() == 1) {
                        toRemove.add( entity.getId());
                    }
                }
                for( String id : toRemove) {
                    //removeEntity( id);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return storeHelper.toJson();
    }

    public static String GetAccessToken(String code) {
        //return "260661a8436bd15ca45c24a4b5967dce";
        String token = "";
        UrlHelper urlHelper = new UrlHelper( UrlHelper.Type.POST, "https://secure.viadeo.com/oauth-provider/access_token2");
        urlHelper.addParameter( "grant_type", "authorization_code");
        urlHelper.addParameter( "client_id", ViadeoRestProvider.CLIENT_ID);
        urlHelper.addParameter( "client_secret", ViadeoRestProvider.CLIENT_SECRET);
        urlHelper.addParameter( "redirect_uri", ViadeoRestProvider.APP_URL);
        urlHelper.addParameter( "code", code);
        try {
            urlHelper.openConnections();
            JsonNode node = mapper.readTree( urlHelper.getStream());
            token = node.get( "access_token").getTextValue();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }
}
