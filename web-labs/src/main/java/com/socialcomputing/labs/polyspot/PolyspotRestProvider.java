package com.socialcomputing.labs.polyspot;

import java.net.HttpURLConnection;
import java.net.URLDecoder;
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

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

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
    public String kind(@Context HttpServletRequest request,  
                       @QueryParam("sources") String sources,
                       @QueryParam("fields") String fields,
                       @QueryParam("query") String query) {
        HttpSession session = request.getSession(true);
        String plsptSession = ( String)session.getAttribute( "plsptSession");
        String cookie = ( String)session.getAttribute( "Cookie");
        StoreHelper data = new StoreHelper();
        try {
            if (plsptSession == null) {
                UrlHelper s = new UrlHelper(API_URL);
                s.addParameter("command", "createsession");
                s.addParameter("r_format", "json");
                s.addParameter("login", "admin");
                s.addParameter("password", "polyspotdemo");
                s.openConnections();
                JsonNode resNode = mapper.readTree(s.getStream());
                cookie = s.getConnection().getHeaderField("Set-Cookie");
                cookie = cookie.substring(0, cookie.indexOf(';'));
                String error = serializeError(resNode);
                if (error != null)
                    return error;
                plsptSession = resNode.get("sessionid").getTextValue();
                s = new UrlHelper(API_URL);
                s.addHeader("Cookie", cookie);
                s.addParameter("command", "initusersecuritycontext");
                s.addParameter("r_format", "json");
                s.addParameter("sessionid", plsptSession);
                s.addParameter("user_uid", "admin");
                s.openConnections();
                resNode = mapper.readTree(s.getStream());
                error = serializeError(resNode);
                if (error != null)
                    return error;
                plsptSession = resNode.get("sessionid").getTextValue();
                session.setAttribute( "plsptSession", plsptSession);
                session.setAttribute( "Cookie", cookie);
            }
            UrlHelper s = new UrlHelper(API_URL);
            s = new UrlHelper(API_URL);
            s.addHeader("Cookie", cookie);
            s.addParameter("command", "search");
            s.addParameter("sessionid", plsptSession);
            s.addParameter("r_format", "json");
            s.addParameter("q_usr", query);
            s.addParameter("q_usr_flags", "1");
            s.addParameter("q_usr_flags", "2");
            for( String source : sources.split( ",")) {
                if( source.length() > 0)
                    s.addParameter("srcid", source);
            }       
            for(String fname : fields.split( ",")) {
                s.addParameter("r_fl", fname);
            }
            s.addParameter("r_fl", "_title");
            s.addParameter("r_nbfetch", "100");
            s.openConnections();
            JsonNode resNode = mapper.readTree(s.getStream());
            String error = serializeError(resNode);
            if (error != null)
                return error;
            ArrayNode items = (ArrayNode) resNode.get( "itemSet");
            for (JsonNode item : items) {
                Attribute attribute = data.addAttribute(item.get("_uid").get(0).getTextValue());
                //attribute.addProperty("name", item.get("_title").get(0).getTextValue());
                attribute.addProperty("name", StringEscapeUtils.unescapeHtml(item.get("_title").get(0).getTextValue()));
                for(String fname : fields.split( ",")) {
                    ArrayNode fieldsa = (ArrayNode) item.get(fname);
                    if( fieldsa != null) {
                        for( JsonNode ngram : (ArrayNode) item.get(fname)) {
                            //Entity entity = data.addEntity( ngram.getTextValue());
                            Entity entity = data.addEntity( StringEscapeUtils.unescapeHtml(ngram.getTextValue()));
                            entity.addProperty("name", entity.getId());
                            entity.addAttribute(attribute, 1);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return data.toJson();
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

    private String serializeError( JsonNode result) {
        if( result.get("code") == null)
            return null;
        JsonNode trace = result.get("trace");
        return StoreHelper.ErrorToJson( result.get("code").getValueAsLong(), result.get("message").getTextValue(), trace == null ? "" : trace.getTextValue());
    }
}
