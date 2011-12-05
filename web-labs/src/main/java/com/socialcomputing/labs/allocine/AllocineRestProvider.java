package com.socialcomputing.labs.allocine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;

@Path("/allocine/maps")
public class AllocineRestProvider {

    public static final String API_KEY = "108710779211353";

    @GET
    @Path("{kind}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @PathParam("kind") String kind) {
        HttpSession session = request.getSession(true);
        String result = ( String)session.getAttribute(kind);
        if (result == null || result.length() == 0) {
            result = kind(kind);
            session.setAttribute(kind, result);
        }
        return result;
    }

    private String kind(String kind) {
        StoreHelper storeHelper = new StoreHelper();
        return storeHelper.toJson();
    }
    
}
