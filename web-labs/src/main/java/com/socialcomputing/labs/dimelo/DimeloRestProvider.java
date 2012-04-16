package com.socialcomputing.labs.dimelo;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

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
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.Responses;

@Path("/dimelo")
public class DimeloRestProvider {

    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/{kind}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, 
                       @PathParam("kind") String kind, 
                       @QueryParam("access-token") String accessToken, 
                       @QueryParam("www-url") String wwwUrl, 
                       @QueryParam("user-url") String userUrl, 
                       @QueryParam("feedback-url") String feedbackUrl, 
                       @QueryParam("param") String param) {
        HttpSession session = request.getSession(true);
        String key = kind + "/" + param;
        String result = ( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
            if( kind.equalsIgnoreCase("search")) {
                result = search(accessToken, wwwUrl, userUrl, feedbackUrl, param);
            }
            else if( kind.equalsIgnoreCase("user")) {
                result = user(accessToken, wwwUrl, userUrl, feedbackUrl, param);
            }
            else if( kind.equalsIgnoreCase("feedback")) {
                result = feedback(accessToken, wwwUrl, userUrl, feedbackUrl, param);
            }
            session.setAttribute( key, result);
        }
        return result;
    }

    @GET
    @Path("users/{id}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String users(@PathParam("id") String id,
                       @QueryParam("access_token") String accessToken, 
                       @QueryParam("user_url") String userUrl) {
        try {
            UrlHelper url = new UrlHelper( userUrl + "/1.0/users/" + id);
            url.addParameter( "access_token", accessToken);
            url.openConnections();
            return url.getResult();
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
    }

    @GET
    @Path("feedbacks/{id}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String feedbacks(@PathParam("id") String id,
                       @QueryParam("access_token") String accessToken, 
                       @QueryParam("feedback_url") String feedbackUrl) {
        try {
            UrlHelper url = new UrlHelper( feedbackUrl + "/1.0/feedbacks/" + id);
            url.addParameter( "access_token", accessToken);
            url.openConnections();
            return url.getResult();
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
    }
    
    // Search within feedbacks
    private String search(String accessToken, String wwwUrl, String userUrl, String feedbackUrl, String query) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlIdeas = new UrlHelper( feedbackUrl + "/1.0/feedbacks");
            urlIdeas.addParameter( "access_token", accessToken);
            urlIdeas.addParameter( "limit", "50");
            urlIdeas.addParameter( "search", query);
            urlIdeas.openConnections();
            JsonNode ideas = mapper.readTree(urlIdeas.getStream());
            for (JsonNode idea : (ArrayNode) ideas) {
                Attribute att = this.addIdea( storeHelper, idea);
                this.addContributors(accessToken, feedbackUrl, storeHelper, att, idea);
            }
            urlIdeas.closeConnections();
            this.fillUsersProperties(accessToken, wwwUrl, userUrl, storeHelper);
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }

    // Start from an Id
    private String feedback(String accessToken, String wwwUrl, String userUrl, String feedbackUrl, String feedback) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlIdea = new UrlHelper( feedbackUrl + "/1.0/feedbacks/" + feedback);
            urlIdea.addParameter( "access_token", accessToken);
            urlIdea.openConnections();
            JsonNode idea = mapper.readTree(urlIdea.getStream());
            Attribute att = this.addIdea( storeHelper, idea);
            this.addContributors(accessToken, feedbackUrl, storeHelper, att, idea);
            urlIdea.closeConnections();

            Collection<Entity> ents = new ArrayList<Entity>(storeHelper.getEntities().values());
            for( Entity ent : ents) {
                UrlHelper urlIdeas = new UrlHelper( feedbackUrl + "/1.0/feedbacks");
                urlIdeas.addParameter( "access_token", accessToken);
                urlIdeas.addParameter( "limit", "50");
                urlIdeas.addParameter( "user_id", ent.getId());
                urlIdeas.openConnections();
                JsonNode ideas = mapper.readTree(urlIdeas.getStream());
                for (JsonNode idea1 : (ArrayNode) ideas) {
                    att = this.addIdea( storeHelper, idea1);
                    this.addContributors(accessToken, feedbackUrl, storeHelper, att, idea1);
                }
                urlIdeas.closeConnections();
            }
            this.fillUsersProperties(accessToken, wwwUrl, userUrl, storeHelper);
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }
    
    // Start from an Id
    private String user(String accessToken, String wwwUrl, String userUrl, String feedbackUrl, String userId) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            Entity ent = storeHelper.addEntity( userId);
            UrlHelper urlIdeas = new UrlHelper( feedbackUrl + "/1.0/feedbacks");
            urlIdeas.addParameter( "access_token", accessToken);
            urlIdeas.addParameter( "limit", "150");
            urlIdeas.addParameter( "user_id", userId);
            urlIdeas.openConnections();
            JsonNode ideas = mapper.readTree(urlIdeas.getStream());
            for (JsonNode idea1 : (ArrayNode) ideas) {
                Attribute att = this.addIdea( storeHelper, idea1);
                ent.addAttribute( att, 1);
                this.addContributors(accessToken, feedbackUrl, storeHelper, att, idea1);
            }
            urlIdeas.closeConnections();

            this.fillUsersProperties(accessToken, wwwUrl, userUrl, storeHelper);
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }

    private Attribute addIdea(StoreHelper storeHelper, JsonNode idea) throws Exception {
        Attribute att = storeHelper.addAttribute(String.valueOf(idea.get("id").getIntValue()));
        att.addProperty("name", idea.get("title").getTextValue());
        att.addProperty("url", idea.get("permalink").getTextValue());
        return att;
    }
    
    private void addContributors(String accessToken, String feedbackUrl, StoreHelper storeHelper, Attribute att, JsonNode idea) throws Exception {
        // Author
        Entity ent = storeHelper.addEntity(String.valueOf(idea.get("user_id").getIntValue()));
        ent.addAttribute( att, 1);
        // Comments
        UrlHelper urlComments = new UrlHelper( feedbackUrl + "/1.0/feedbacks/" + att.getId() + "/comments");
        urlComments.addParameter( "access_token", accessToken);
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
        UrlHelper urlVotes = new UrlHelper( feedbackUrl + "/1.0/feedbacks/" + att.getId() + "/votes");
        urlVotes.addParameter( "access_token", accessToken);
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
    
    private void fillUsersProperties(String accessToken, String wwwUrl, String userUrl, StoreHelper storeHelper) throws Exception {
        UrlHelper urlUsers = new UrlHelper( userUrl + "/1.0/users");
        urlUsers.addParameter( "access_token", accessToken);
        urlUsers.addParameter( "limit", "150");
        for( Entity ent : storeHelper.getEntities().values()) {
            urlUsers.addParameter( "ids[]", ent.getId());
        }
        urlUsers.openConnections();
        JsonNode users = mapper.readTree(urlUsers.getStream());
        for (JsonNode user : (ArrayNode) users) {
            Entity ent = storeHelper.addEntity(String.valueOf(user.get("id").getIntValue()));
            ent.addProperty("name", user.get("firstname").getTextValue() + " " + user.get("lastname").getTextValue());        
            ent.addProperty("url", wwwUrl + "/users/" + ent.getId());
        }
        urlUsers.closeConnections();
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

}
