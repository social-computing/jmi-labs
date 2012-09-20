package com.socialcomputing.labs.allocine;

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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
import com.sun.jersey.api.Responses;

@Path("/allocine")
public class AllocineRestProvider {

    public static final String API_URL = "http://api.allocine.fr";
    //public static final String API_URL = "http://ext.api.allocine.fr";
    public static final String API_KEY = "U29jaWFsQ29tcHV0aW5n";
    private static final ObjectMapper mapper = new ObjectMapper();

    protected class Movie {
        public Movie( String id, String title, String image) {
            this.id = id;
            this.title = title;
            this.image = image;
        }
        public String id, title, image;
    }
    
    @GET
    @Path("maps/{kind}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @PathParam("kind") String kind, @DefaultValue("top:month") @QueryParam("filter") String filter) {
        HttpSession session = request.getSession(true);
        String key = kind + "_" + filter;
        String result = ( String)session.getAttribute( key);
        if (result == null || result.length() == 0 || kind.equalsIgnoreCase( "film_same")) {
            StoreHelper storeHelper = new StoreHelper();
            try {
                if( kind.equalsIgnoreCase( "film_gender")) {
                    film_gender( storeHelper, filter);
                }
                else if( kind.equalsIgnoreCase( "film_tag")) {
                    film_tag( storeHelper, filter);
                }
                else if( kind.equalsIgnoreCase( "film_casting")) {
                    film_casting( storeHelper, filter);
                }
                else if( kind.equalsIgnoreCase( "film_same")) {
                    //film_same( storeHelper, filter);
                    film_similarity( storeHelper, filter);
                }
                result = storeHelper.toJson();
                session.setAttribute( key, result);
            }
            catch (Exception e) {
                return StoreHelper.ErrorToJson(e);
            }
        }
        return result;
    }

    @GET
    @Path("image-proxy")
    @Produces("image/*")
    public Response getThumbnail( @HeaderParam("Accept-Encoding") String encoding, @HeaderParam("If-Modified-Since") String cache, @HeaderParam("If-Modified-Since") String modified, @HeaderParam("If-None-Match") String match, @QueryParam("url") String url) {
        if( url.length() == 0) {
            Response.status( Responses.NOT_FOUND);
            return null;
        }
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

    private void film_gender(StoreHelper storeHelper, String filter) throws Exception {
        UrlHelper urlHelper = new UrlHelper( AllocineRestProvider.API_URL + "/rest/v3/movieList");
        urlHelper.addParameter( "partner", AllocineRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "count", "200");
        urlHelper.addParameter( "filter", filter);
        urlHelper.openConnections();
        JsonNode node = mapper.readTree(urlHelper.getStream());
        for (JsonNode movie : (ArrayNode) node.get("feed").get("movie")) {
            movie_gender( movie, storeHelper);
        }
    }
    
    private void film_tag(StoreHelper storeHelper, String filter) throws Exception {
        UrlHelper urlHelper = new UrlHelper( AllocineRestProvider.API_URL + "/rest/v3/movieList");
        urlHelper.addParameter( "partner", AllocineRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "count", "200");
        urlHelper.addParameter( "filter", filter);
        urlHelper.openConnections();
        JsonNode movies = mapper.readTree(urlHelper.getStream());
        for (JsonNode movie : (ArrayNode) movies.get("feed").get("movie")) {
            movie_tag( movie, storeHelper);
        }
    }
    
    private void film_casting(StoreHelper storeHelper, String filter) throws Exception {
        UrlHelper urlHelper = new UrlHelper( AllocineRestProvider.API_URL + "/rest/v3/movieList");
        urlHelper.addParameter( "partner", AllocineRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "count", "200");
        urlHelper.addParameter( "filter", filter);
        urlHelper.openConnections();
        JsonNode movies = mapper.readTree(urlHelper.getStream());
        for (JsonNode movie : (ArrayNode) movies.get("feed").get("movie")) {
            movie_casting( movie, storeHelper);
        }
    }

    private void film_similarity(StoreHelper storeHelper, String id) throws Exception {
        UrlHelper urlHelper = new UrlHelper( AllocineRestProvider.API_URL + "/rest/v3/similarities");
        urlHelper.addParameter( "partner", AllocineRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "count", "7");
        urlHelper.addParameter( "code", id);
        urlHelper.openConnections();
        JsonNode similarities = mapper.readTree(urlHelper.getStream());

        JsonNode movie = similarities.get("similarities");
        Attribute reference = storeHelper.addAttribute( String.valueOf(movie.get("movieid").getLongValue()));
        reference.addProperty("name", movie.get("title").getTextValue());
        reference.addProperty("poster", movie.get("href") != null ? movie.get("href").getTextValue() : "");
        for (JsonNode similarity : (ArrayNode) movie.get("similar")) {
            Entity entity = storeHelper.addEntity( String.valueOf(similarity.get("movieid").getLongValue()));
            entity.addProperty("name", similarity.get("title").getTextValue());
            entity.addProperty("poster", similarity.get("href") != null ? similarity.get("href").getTextValue() : "");
            entity.addAttribute(reference, 1);

            Attribute attribute = storeHelper.addAttribute(entity.getId());
            attribute.addProperty("name", entity.getProperties().get("name"));
            attribute.addProperty("poster", entity.getProperties().get("poster"));
            entity.addAttribute(attribute, 1);//tricky
            
            for (JsonNode samemovie : (ArrayNode) similarity.get("child")) {
                Entity entity2 = storeHelper.addEntity(String.valueOf(samemovie.get("movieid").getLongValue()));
                entity2.addProperty("name", samemovie.get("title").getTextValue());
                entity2.addProperty("poster", samemovie.get("href") != null ? samemovie.get("href").getTextValue() : "");
                entity2.addAttribute(attribute, 1);

                Attribute attribute2 = storeHelper.addAttribute(entity2.getId());
                attribute2.addProperty("name", entity2.getProperties().get("name"));
                attribute2.addProperty("poster", entity2.getProperties().get("poster"));
                entity.addAttribute(attribute2, 1);
            }
        }
        urlHelper.closeConnections();
    }
    
    private void film_same( StoreHelper storeHelper, String id) throws Exception {
        UrlHelper urlHelper = new UrlHelper( AllocineRestProvider.API_URL + "/rest/v3/movieList");
        urlHelper.addParameter( "partner", AllocineRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "count", "50");
        urlHelper.addParameter( "filter", "similar:" + id);
        urlHelper.openConnections();
        JsonNode movies = mapper.readTree(urlHelper.getStream());
        for (JsonNode movie : (ArrayNode) movies.get("feed").get("movie")) {
            movie_same( movie, storeHelper);
            Thread.sleep( 3000);
        }
        movie_same( get_movie( id), storeHelper);
    }

    private void movie_same( JsonNode movie, StoreHelper storeHelper) throws Exception {
        Attribute attribute = storeHelper.addAttribute( movie.get("code").getValueAsText());
        attribute.addProperty("name", movie.get("title").getTextValue());
        attribute.addProperty("poster", get_poster_url( movie));

        UrlHelper urlHelper = new UrlHelper( AllocineRestProvider.API_URL + "/rest/v3/movieList");
        urlHelper.addParameter( "partner", AllocineRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "count", "20");
        urlHelper.addParameter( "filter", "similar:" + attribute.getId());
        urlHelper.openConnections();
        JsonNode movies = mapper.readTree(urlHelper.getStream());
        for (JsonNode samemovie : (ArrayNode) movies.get("feed").get("movie")) {
            Entity entity = storeHelper.addEntity( samemovie.get("code").getValueAsText());
            entity.addProperty("name", samemovie.get("title").getTextValue());
            entity.addProperty("poster", get_poster_url( samemovie));
            entity.addAttribute(attribute, 1);
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
        if( fullMovie.get("tag") != null) {
            for (JsonNode tag : (ArrayNode) fullMovie.get("tag")) {
                Entity entity = storeHelper.addEntity( tag.get("code").getValueAsText());
                entity.addProperty("name", tag.get("$").getTextValue());
                entity.addAttribute(attribute, 1);
            }
        }
    }
    
    private void movie_casting( JsonNode movie, StoreHelper storeHelper) {
        Attribute attribute = storeHelper.addAttribute( movie.get("code").getValueAsText());
        attribute.addProperty("name", movie.get("title").getTextValue());
        attribute.addProperty("poster", get_poster_url( movie));
        
        JsonNode directors = movie.get("castingShort").get("directors");
        if( directors != null) {
            for( String director : directors.getTextValue().split( ",")) {
                director = director.trim();
                Entity entity = storeHelper.addEntity( director);
                entity.addProperty("name", director);
                entity.addAttribute(attribute, 1);
            }
        }
        JsonNode actors = movie.get("castingShort").get("actors");
        if( actors != null) {
            for( String actor : actors.getTextValue().split( ",")) {
                actor = actor.trim();
                Entity entity = storeHelper.addEntity( actor);
                entity.addProperty("name", actor);
                entity.addAttribute(attribute, 1);
            }
        }
    }
    
    private JsonNode get_movie( String id) throws Exception {
        UrlHelper urlHelper = new UrlHelper( AllocineRestProvider.API_URL + "/rest/v3/movie");
        urlHelper.addParameter( "partner", AllocineRestProvider.API_KEY);
        urlHelper.addParameter( "format", "json");
        urlHelper.addParameter( "code", id);
        urlHelper.openConnections();
        return mapper.readTree(urlHelper.getStream()).get("movie");
    }

    private Movie getMovie( JsonNode movie) throws Exception {
        String j = String.valueOf(movie.get("movieid").getLongValue());
        return new Movie(String.valueOf(movie.get("movieid").getLongValue()), 
                         movie.get("title").getTextValue(), 
                         movie.get("href").getTextValue());
    }
    
    private String get_poster_url( JsonNode movie) {
        JsonNode poster = movie.get("poster");
        return poster  != null ? poster.get("href").getTextValue() : "";
    }
}
