package com.socialcomputing.labs.template;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

@Path("/template")
public class RestProvider {

    private static final ObjectMapper mapper = new ObjectMapper();
    
    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @QueryParam("query") String query) {
        HttpSession session = request.getSession(true);
        String key = query;
        String result = ( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
            result = extract(query);
            session.setAttribute( key, result);
        }
        return result;
    }
    
    private String extract(String query) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            UrlHelper urlNodes = new UrlHelper( "REST_URL");
            urlNodes.addParameter( "param1", "value1");
            urlNodes.addParameter( "param2", "value2");
            urlNodes.openConnections();
            JsonNode nodes = mapper.readTree(urlNodes.getStream());
            for (JsonNode node : (ArrayNode) nodes) {
                Attribute att = storeHelper.addAttribute(node.get("id").getTextValue());
                att.addProperty("name", node.get("title").getTextValue());
                att.addProperty("url", node.get("link").getTextValue());
                
                Entity ent = storeHelper.addEntity(node.get("xxxx").getTextValue());
                ent.addAttribute( att, 1);
                /// ....
            }
            urlNodes.closeConnections();
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }
    
    /*
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
*/
}
