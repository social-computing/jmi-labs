package com.socialcomputing.labs.facebook;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.social.SocialHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper.Type;
import com.sun.jersey.api.Responses;
import com.sun.jersey.core.util.Base64;


@Path("/facebook")
public class FacebookRestProvider {
   // JMI Test
/*    public static final String CLIENT_ID = "136353756473765";
    public static final String CLIENT_SECRET = "67118f943664c3cb42d3cfa053ce4bed";
    public static final String APP_URL = "http://apps.facebook.com/jmi-test/";
    */
    public static final String CLIENT_ID = "108710779211353";
    public static final String CLIENT_SECRET = "e155ed50ccf90de8d9c7dafbd88bb92d";
    public static final String APP_URL = "http://apps.facebook.com/just-map-it/";
    public static final String REDIRECT_URL = "http://labs.just-map-it.com/facebook/";
    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/{kind}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @PathParam("kind") String kind,
                       @QueryParam("access_token") String token) {
        HttpSession session = request.getSession(true);
        String result = ( String)session.getAttribute(kind);
        if (result == null || result.length() == 0) {
            try {
                result = kind(kind, token);
                session.setAttribute(kind, result);
            }
            catch (Exception e) {
                result = StoreHelper.ErrorToJson(e);
            }
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
    
    private String kind(String kind, String token) throws Exception {
        StoreHelper storeHelper = new StoreHelper();
        if (kind.equalsIgnoreCase("friends")) {
            SocialHelper socialHelper = new SocialHelper( storeHelper);
            
            // Liste amis
            UrlHelper urlHelper = new UrlHelper();
            urlHelper.setUrl("https://graph.facebook.com/me/friends");
            urlHelper.addParameter("access_token", token);
            urlHelper.openConnections();

            JsonNode node = mapper.readTree(urlHelper.getStream());
            List<String> friendslist = new ArrayList<String>();
            ArrayNode friends = (ArrayNode) node.get("data");
            for (JsonNode friend : friends) {
                socialHelper.addPerson(friend.get("id").getTextValue()).addProperty("name", friend.get("name").getTextValue());
                friendslist.add(friend.get("id").getTextValue());
            }

            // My self
            UrlHelper uh = new UrlHelper();
            uh.setUrl("https://graph.facebook.com/me");
            uh.addParameter("access_token", token);
            uh.openConnections();
            JsonNode me = mapper.readTree(uh.getStream());
            storeHelper.addGlobal("$MY_FB_ID", me.get("id").getTextValue());

            // Les amis entre eux
            for (int i = 0; i < friendslist.size() - 1; i++) {
                StringBuilder sb1 = new StringBuilder();
                StringBuilder sb2 = new StringBuilder();
                for (int j = i + 1; j < friendslist.size(); j++) {
                    sb1.append(friendslist.get(i)).append(",");
                    sb2.append(friendslist.get(j)).append(",");
                }
                String areFriends = "https://api.facebook.com/method/friends.areFriends";
                UrlHelper uh1 = new UrlHelper();
                uh1.setUrl(areFriends);
                uh1.setType(Type.POST);
                uh1.addParameter("uids1", sb1.toString());
                uh1.addParameter("uids2", sb2.toString());
                uh1.addParameter("access_token", token);
                uh1.addParameter("format", "json");
                uh1.openConnections();
                ArrayNode r = (ArrayNode) mapper.readTree(uh1.getStream());
                for (JsonNode rs : r) {
                    // System.out.println(rs.get("uid1") + "=>" +
                    // rs.get("uid2") + "=>" + rs.get("are_friends"));
                    if (rs.get("are_friends").getBooleanValue())
                        socialHelper.setFriendShip(String.valueOf(rs.get("uid1").getLongValue()),
                                      String.valueOf(rs.get("uid2").getLongValue()).toString());
                }
            }

            // Je suis ami avec tous mes amis
            // setFriendShip((String)me.get("id"), friendslist);

            // Delete entities with only one attribute
            Set<String> toRemove = new HashSet<String>();
            for (Entity entity : storeHelper.getEntities().values()) {
                if (entity.getAttributes().size() == 1) {
                    toRemove.add(entity.getId());
                }
            }
            for (String id : toRemove) {
                storeHelper.removeEntity(id);
            }
        }
        else {
            UrlHelper urlHelper = new UrlHelper();
            urlHelper.setUrl("https://graph.facebook.com/me/friends");
            urlHelper.addParameter("access_token", token);
            urlHelper.openConnections();

            JsonNode node = mapper.readTree(urlHelper.getStream());
            ArrayNode friends = (ArrayNode) node.get("data");

            // My self
            UrlHelper uh = new UrlHelper();
            uh.setUrl("https://graph.facebook.com/me");
            uh.addParameter("access_token", token);
            uh.openConnections();
            JsonNode me = mapper.readTree(uh.getStream());
            storeHelper.addGlobal("$MY_FB_ID", me.get("id").getTextValue());
            friends.add(me);

            for (JsonNode friend : friends) {
                Attribute attribute = storeHelper.addAttribute(friend.get("id").getTextValue());
                attribute.addProperty("name", friend.get("name") != null ? friend.get("name").getTextValue() : " ");

                UrlHelper urlHelper2 = new UrlHelper();
                urlHelper2.setUrl("https://graph.facebook.com/" + friend.get("id").getTextValue() + "/" + kind);
                urlHelper2.addParameter("access_token", token);
                urlHelper2.openConnections();

                JsonNode node2 = mapper.readTree(urlHelper2.getStream());
                ArrayNode kinds = (ArrayNode) node2.get("data");
                for (JsonNode curkind : kinds) {
                    if (curkind.get("id") != null && curkind.get("name") != null) {
                        Entity entity = storeHelper.addEntity(curkind.get("id").getTextValue());
                        entity.addProperty("name", curkind.get("name").getTextValue());
                        entity.addAttribute(attribute, 1);
                    }
                }
            }
            // Delete entities with only one attribute
            Set<String> toRemove = new HashSet<String>();
            for (Entity entity : storeHelper.getEntities().values()) {
                if (entity.getAttributes().size() == 1) {
                    toRemove.add(entity.getId());
                }
            }
            for (String id : toRemove) {
                storeHelper.removeEntity(id);
            }
        }
        return storeHelper.toJson();
    }

    public static String GetProperty(String signed_request, String property) {
        String ret = null;
        if (signed_request != null) {
            int pos = signed_request.indexOf('.');
            if (pos > 0) {
                String json = new String(Base64.decode(signed_request.substring(pos + 1)));
                JsonNode node;
                try {
                    node = mapper.readTree(json);
                    if (node.get(property) != null)
                        ret = node.get(property).getTextValue();
                }
                catch (Exception e) {
                    System.out.println(json);
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
    
    @POST
    @Path("upload-photo")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED}) 
    public Response uploadPhoto( @FormParam("token") String token, 
                                 @FormParam("id") String id, 
                                 @FormParam("title") String title, 
                                 @FormParam("image") String image) {
        //url: "https://graph.facebook.com/" + this.map.getProperty("$MY_FB_ID") + "/photos",
        // If the url is provided, get the data from the remote url
        try {
            HttpClient      client = new DefaultHttpClient();
            HttpPost        post   = new HttpPost("https://graph.facebook.com/" + id + "/photos");
            MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
            entity.addPart( "access_token", new StringBody( token, "text/plain", Charset.forName( "UTF-8" )));
            entity.addPart( "name", new StringBody( title, "text/plain", Charset.forName( "UTF-8" )));
            entity.addPart( "filedata", new ByteArrayBody( Base64.decode(image), "image/png", "image.png"));
            post.setEntity( entity );
            HttpResponse response = client.execute( post);
            HttpEntity rentity = response.getEntity();
            String contentType = "application/json";//rentity.getContentType() == null ? "text/plain" : rentity.getContentType().getValue();
            ResponseBuilder rb = Response.ok( EntityUtils.toString( rentity, "UTF-8" ), contentType);
            rb.status(response.getStatusLine().getStatusCode());
            client.getConnectionManager().shutdown();
            return rb.build();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    public static String GetAccessToken(String code) {
        String token = "";
        UrlHelper urlHelper = new UrlHelper();
        urlHelper.setUrl("https://graph.facebook.com/oauth/access_token");
        urlHelper.addParameter( "client_id", FacebookRestProvider.CLIENT_ID);
        urlHelper.addParameter( "redirect_uri", FacebookRestProvider.REDIRECT_URL + "postinstall.jsp");
        urlHelper.addParameter( "client_secret", FacebookRestProvider.CLIENT_SECRET);
        urlHelper.addParameter( "code", code);
        try {
            urlHelper.openConnections();
            String response = urlHelper.getResult(); 
            for( String p : response.split("&")) {
                if( p.startsWith( "access_token=")) {
                    token = p.substring( p.indexOf( '=') + 1);
                    break;
                }
            }
        }
        catch (JMIException e) {
            e.printStackTrace();
        }
        return token;
    }
}
