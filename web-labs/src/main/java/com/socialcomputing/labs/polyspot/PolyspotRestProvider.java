package com.socialcomputing.labs.polyspot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
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
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.socialcomputing.wps.server.planDictionnary.connectors.WPSConnectorException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.Responses;

@Path("/polyspot")
public class PolyspotRestProvider {

    public static final String API_KEY = "key";
    public static final String API_URL = "http://demo.polyspot.com:8588/integrationService/compositeRequest.do";
    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/{kind}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @PathParam("kind") String kind, 
                       @DefaultValue("") @QueryParam("plsptSession") String plsptSession,
                       @DefaultValue("top:month") @QueryParam("filter") String filter) {
        HttpSession session = request.getSession(true);
        String key = kind + "_" + filter;
        String result = null; //( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
             if( plsptSession.length() == 0) {
                 UrlHelper s = new UrlHelper( API_URL);
                 s.addParameter("command", "createsessionjj");
                 s.addParameter("r_format", "json");
                 s.addParameter("login", "admin");
                 s.addParameter("password", "polyspotdemo");
                 try {
                    s.openConnections();
                    JsonNode resNode = mapper.readTree(s.getStream());
                    String error = serializeError( resNode);
                    if( error != null) return error;
                    plsptSession = resNode.get("sessionid").getTextValue();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

             }
            
            if( kind.equalsIgnoreCase( "film_gender")) {
                result = film_gender( filter);
            }
            else if( kind.equalsIgnoreCase( "film_tag")) {
                result = film_tag( filter);
            }
            else if( kind.equalsIgnoreCase( "film_casting")) {
                result = film_casting( filter);
            }
            else if( kind.equalsIgnoreCase( "film_same")) {
                result = film_same( filter);
            }
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

    private String film_gender(String filter) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlHelper = new UrlHelper( PolyspotRestProvider.API_URL + "/rest/v3/movieList");
            urlHelper.addParameter( "partner", PolyspotRestProvider.API_KEY);
            urlHelper.addParameter( "format", "json");
            urlHelper.addParameter( "count", "200");
            urlHelper.addParameter( "filter", filter);
            urlHelper.openConnections();
            JsonNode node = mapper.readTree(urlHelper.getStream());
            for (JsonNode movie : (ArrayNode) node.get("feed").get("movie")) {
                movie_gender( movie, storeHelper);
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeHelper.toJson();
    }
    
    private String film_tag(String filter) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlHelper = new UrlHelper( PolyspotRestProvider.API_URL + "/rest/v3/movieList");
            urlHelper.addParameter( "partner", PolyspotRestProvider.API_KEY);
            urlHelper.addParameter( "format", "json");
            urlHelper.addParameter( "count", "200");
            urlHelper.addParameter( "filter", filter);
            urlHelper.openConnections();
            JsonNode movies = mapper.readTree(urlHelper.getStream());
            for (JsonNode movie : (ArrayNode) movies.get("feed").get("movie")) {
                movie_tag( movie, storeHelper);
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeHelper.toJson();
    }
    
    private String film_casting(String filter) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlHelper = new UrlHelper( PolyspotRestProvider.API_URL + "/rest/v3/movieList");
            urlHelper.addParameter( "partner", PolyspotRestProvider.API_KEY);
            urlHelper.addParameter( "format", "json");
            urlHelper.addParameter( "count", "200");
            urlHelper.addParameter( "filter", filter);
            urlHelper.openConnections();
            JsonNode movies = mapper.readTree(urlHelper.getStream());
            for (JsonNode movie : (ArrayNode) movies.get("feed").get("movie")) {
                movie_casting( movie, storeHelper);
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeHelper.toJson();
    }

    private String film_same( String id) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlHelper = new UrlHelper( PolyspotRestProvider.API_URL + "/rest/v3/movieList");
            urlHelper.addParameter( "partner", PolyspotRestProvider.API_KEY);
            urlHelper.addParameter( "format", "json");
            urlHelper.addParameter( "count", "50");
            urlHelper.addParameter( "filter", "similar:" + id);
            urlHelper.openConnections();
            JsonNode movies = mapper.readTree(urlHelper.getStream());
            for (JsonNode movie : (ArrayNode) movies.get("feed").get("movie")) {
                movie_same( movie, storeHelper);
            }
            movie_same( get_movie( id), storeHelper);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeHelper.toJson();
    }

    private void movie_same( JsonNode movie, StoreHelper storeHelper) throws Exception {
        Attribute attribute = storeHelper.addAttribute( movie.get("code").getValueAsText());
        attribute.addProperty("name", movie.get("title").getTextValue());
        attribute.addProperty("poster", get_poster_url( movie));

        UrlHelper urlHelper = new UrlHelper( PolyspotRestProvider.API_URL + "/rest/v3/movieList");
        urlHelper.addParameter( "partner", PolyspotRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "count", "10");
        urlHelper.addParameter( "filter", "similar:" + attribute.getId());
        urlHelper.openConnections();
        JsonNode movies = mapper.readTree(urlHelper.getStream());
        for (JsonNode samemovie : (ArrayNode) movies.get("feed").get("movie")) {
            Entity entity = storeHelper.addEntity( samemovie.get("code").getValueAsText());
            entity.addProperty("name", samemovie.get("title").getTextValue());
            entity.addProperty("poster", get_poster_url( samemovie));
        }
    }
    
    private void movie_gender( JsonNode movie, StoreHelper storeHelper) throws Exception {
        Attribute attribute = storeHelper.addAttribute( movie.get("code").getValueAsText());
        attribute.addProperty("name", movie.get("title").getTextValue());
        attribute.addProperty("poster", get_poster_url( movie));
        for (JsonNode genre : (ArrayNode) movie.get("genre")) {
            Entity entity = storeHelper.addEntity( genre.get("code").getValueAsText());
            entity.addProperty("name", genre.get("$").getTextValue());
            entity.addAttribute(attribute, 1);
        }
    }
    
    private void movie_tag( JsonNode movie, StoreHelper storeHelper) throws Exception {
        Attribute attribute = storeHelper.addAttribute( movie.get("code").getValueAsText());
        attribute.addProperty("name", movie.get("title").getTextValue());
        attribute.addProperty("poster", get_poster_url( movie));
        
        JsonNode fullMovie = get_movie( attribute.getId());
        for (JsonNode tag : (ArrayNode) fullMovie.get("movie").get("tag")) {
            Entity entity = storeHelper.addEntity( tag.get("code").getValueAsText());
            entity.addProperty("name", tag.get("$").getTextValue());
            entity.addAttribute(attribute, 1);
        }
    }
    
    private void movie_casting( JsonNode movie, StoreHelper storeHelper) {
        Attribute attribute = storeHelper.addAttribute( movie.get("code").getValueAsText());
        attribute.addProperty("name", movie.get("title").getTextValue());
        attribute.addProperty("poster", get_poster_url( movie));
        
        String directors = movie.get("castingShort").get("directors").getTextValue();
        for( String director : directors.split( ",")) {
            director = director.trim();
            Entity entity = storeHelper.addEntity( director);
            entity.addProperty("name", director);
            entity.addAttribute(attribute, 1);
        }
        String actors = movie.get("castingShort").get("actors").getTextValue();
        for( String actor : actors.split( ",")) {
            actor = actor.trim();
            Entity entity = storeHelper.addEntity( actor);
            entity.addProperty("name", actor);
            entity.addAttribute(attribute, 1);
        }
    }
    
    private JsonNode get_movie( String id) throws Exception {
        UrlHelper urlHelper = new UrlHelper( PolyspotRestProvider.API_URL + "/rest/v3/movie");
        urlHelper.addParameter( "partner", PolyspotRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "code", id);
        urlHelper.openConnections();
        return mapper.readTree(urlHelper.getStream()).get("movie");
    }
    
    private String get_poster_url( JsonNode movie) {
        JsonNode poster = movie.get("poster");
        return poster  != null ? poster.get("href").getTextValue() : "";
    }

    private String serializeError( JsonNode result) {
        if( result.get("code") == null)
            return null;
        StoreHelper storeHelper = new StoreHelper();
        JsonNode trace = result.get("trace");
        storeHelper.setError( result.get("code").getValueAsLong(), result.get("message").getTextValue(), trace == null ? "" : trace.getTextValue());
        return storeHelper.toJson();
    }
}
