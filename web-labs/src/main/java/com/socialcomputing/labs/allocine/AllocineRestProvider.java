package com.socialcomputing.labs.allocine;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.socialcomputing.wps.server.planDictionnary.connectors.WPSConnectorException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

@Path("/allocine/maps")
public class AllocineRestProvider {

    public static final String API_KEY = "U29jaWFsQ29tcHV0aW5n";
    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("{kind}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @PathParam("kind") String kind) {
        HttpSession session = request.getSession(true);
        String result = ( String)session.getAttribute(kind);
        if (true) { //result == null || result.length() == 0) {
            result = kind(kind);
            session.setAttribute(kind, result);
        }
        return result;
    }

    private String kind(String kind) {
        StoreHelper storeHelper = new StoreHelper();

        try {
            UrlHelper urlHelper = new UrlHelper( "http://api.allocine.fr/rest/v3/movieList");
            urlHelper.addParameter( "partner", AllocineRestProvider.API_KEY);
            urlHelper.addParameter( "format", "json");
            urlHelper.addParameter( "count", "200");
            urlHelper.addParameter( "filter", "top:month");//"outthisweek");
            urlHelper.openConnections();
    
            JsonNode node = mapper.readTree(urlHelper.getStream());
            for (JsonNode movie : (ArrayNode) node.get("feed").get("movie")) {
                Attribute attribute = storeHelper.addAttribute( movie.get("code").getValueAsText());
                attribute.addProperty("name", movie.get("title").getTextValue());
                for (JsonNode genre : (ArrayNode) movie.get("genre")) {
                    Entity entity = storeHelper.addEntity( genre.get("code").getValueAsText());
                    entity.addProperty("name", genre.get("$").getTextValue());
                    entity.addAttribute(attribute, 1);
                }
            }
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return storeHelper.toJson();
    }
    
}
