package com.socialcomputing.labs.gvisualization;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;

import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;

@Path("/googlevisualization")
public class GVisualizationRestProvider {

    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @QueryParam("source") String source, @QueryParam("sourceId") String sourceId) {
        return checkSource(source, sourceId);
    }

    private String checkSource(String source, String sourceId) {
        StoreHelper storeHelper = new StoreHelper();
        try {
            Session session = com.socialcomputing.labs.utils.HibernateUtil.getSessionFactory().getCurrentSession();
            Source s = (Source) session.get(Source.class, source);
            if( s == null) {
                s = new Source( source, sourceId);
                session.save( s);
            }
            else {
                s.incrementUpdate();
                session.update( s);
            }
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }
}
