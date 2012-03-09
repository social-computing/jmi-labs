package com.socialcomputing.labs.dimelo;

import java.net.HttpURLConnection;
import java.util.Date;

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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.Responses;

@Path("/dimelo")
public class DimeloRestProvider {

    public static final String IDEA_API_URL = "https://open-adisseo.api.ideas.dimelo.com";
    //public static final String IDEA_API_URL = "https://open-adisseo.api.ideas.dimelo.info:4432";
    public static final String USER_API_URL = "https://open-adisseo.api.users.dimelo.com";
    //public static final String USER_API_URL = "https://open-adisseo.api.users.dimelo.info:4433";
    public static final String ACCESS_TOKEN = "5eba7151e753e9fede13177a3098832f";
    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @QueryParam("query") String query) {
        HttpSession session = request.getSession(true);
        String key = query;
        String result = null; //( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
            result = extract(query);
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

    private String extract(String query) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlIdeas = new UrlHelper( DimeloRestProvider.IDEA_API_URL + "/1.0/feedbacks");
            urlIdeas.addParameter( "access_token", DimeloRestProvider.ACCESS_TOKEN);
            urlIdeas.addParameter( "limit", "50");
            urlIdeas.addParameter( "search", query);
            urlIdeas.openConnections();
            JsonNode ideas = mapper.readTree(urlIdeas.getStream());
            for (JsonNode idea : (ArrayNode) ideas) {
                Attribute att = storeHelper.addAttribute(String.valueOf(idea.get("id").getIntValue()));
                att.addProperty("name", idea.get("title").getTextValue());
                att.addProperty("link", idea.get("permalink").getTextValue());
                
                // Author
                Entity ent = storeHelper.addEntity(String.valueOf(idea.get("user_id").getIntValue()));
                ent.addAttribute( att, 1);
                // Comments
                UrlHelper urlComments = new UrlHelper( DimeloRestProvider.IDEA_API_URL + "/1.0/feedbacks/" + att.getId() + "/comments");
                urlComments.addParameter( "access_token", DimeloRestProvider.ACCESS_TOKEN);
                urlComments.openConnections();
                JsonNode commments = mapper.readTree(urlComments.getStream());
                for (JsonNode comment : (ArrayNode) commments) {
                    if( comment.has("user_id")) {
                        ent = storeHelper.addEntity(String.valueOf(comment.get("user_id").getIntValue()));
                        ent.addAttribute( att, 1);
                    }
                }
                urlComments.closeConnections();
                // Votes
                UrlHelper urlVotes = new UrlHelper( DimeloRestProvider.IDEA_API_URL + "/1.0/feedbacks/" + att.getId() + "/comments");
                urlVotes.addParameter( "access_token", DimeloRestProvider.ACCESS_TOKEN);
                urlVotes.openConnections();
                JsonNode votes = mapper.readTree(urlVotes.getStream());
                for (JsonNode vote : (ArrayNode) votes) {
                    if( vote.has("user_id")) {
                        ent = storeHelper.addEntity(String.valueOf(vote.get("user_id").getIntValue()));
                        ent.addAttribute( att, 1);
                    }
                }
                urlVotes.closeConnections();
            }
            urlIdeas.closeConnections();
/*            StringBuilder idUsers = new StringBuilder("[");
            boolean first = true;
            for( Entity ent : storeHelper.getEntities().values()) {
                if( first)
                    first = false;
                else
                    idUsers.append(',');
                idUsers.append(ent.getId());
                ent.addProperty("name", ent.getId());
            }
            idUsers.append(']');*/
            for( Entity ent : storeHelper.getEntities().values()) {
                UrlHelper urlUsers = new UrlHelper( DimeloRestProvider.USER_API_URL + "/1.0/users");
                urlUsers.addParameter( "access_token", DimeloRestProvider.ACCESS_TOKEN);
                //urlUsers.addParameter( "ids", idUsers.toString());
                urlUsers.addParameter( "ids", ent.getId());
                urlUsers.openConnections();
                JsonNode users = mapper.readTree(urlUsers.getStream());
                for (JsonNode user : (ArrayNode) users) {
                    //Entity ent = storeHelper.getEntity(String.valueOf(user.get("id").getIntValue()));
                    ent.addProperty("name", user.get("firstname").getTextValue() + " " + user.get("lastname").getTextValue());        
                }
                urlUsers.closeConnections();
            }
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }
}
