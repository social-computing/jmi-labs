package com.socialcomputing.labs.gspreadsheet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;

@Path("/spreadsheet")
public class SpreadsheetRestProvider {

    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request, @QueryParam("sheetUrl") String sheetUrl) {
        HttpSession session = request.getSession(true);
        String key = sheetUrl;
        String result = null; //( String)session.getAttribute( key);
        if (result == null || result.length() == 0) {
            result = checkGoogleSheet(sheetUrl);
            //session.setAttribute( key, result);
        }
        return result;
    }

    private String checkGoogleSheet(String sheetUrl) {
        StoreHelper storeHelper = new StoreHelper();
        try {
        }
        catch (Exception e) {
            return StoreHelper.ErrorToJson(e);
        }
        return storeHelper.toJson();
    }
}
