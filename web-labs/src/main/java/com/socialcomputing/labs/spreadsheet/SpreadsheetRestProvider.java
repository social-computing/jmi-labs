package com.socialcomputing.labs.spreadsheet;

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

@Path("/spreadsheet")
public class SpreadsheetRestProvider {

    public static final String CLIENT_ID = "849537412505.apps.googleusercontent.com";
    public static final String CLIENT_SECRET = "849537412505@developer.gserviceaccount.com";
    public static final String REDIRECT = "https://code.google.com/apis/console";//"http://labs.just-map-it.com/spreadsheet";
    public static final String SCOPE = "https://spreadsheets.google.com/feeds";
    //"https://spreadsheets.google.com/feeds/worksheets/key/private/full";
    
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

    private String extract(String query) {
        StoreHelper storeHelper = new StoreHelper();
        
 /*       SpreadsheetService service =
                new SpreadsheetService("MySpreadsheetIntegration-v1");

            // TODO: Authorize the service object for a specific user (see other sections)

            // Define the URL to request.  This should never change.
            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");

            // Make a request to the API and get all spreadsheets.
            SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL,
                SpreadsheetFeed.class);
            List<SpreadsheetEntry> spreadsheets = feed.getEntries();

            if (spreadsheets.size() == 0) {
              // TODO: There were no spreadsheets, act accordingly.
            }

            // TODO: Choose a spreadsheet more intelligently based on your
            // app's needs.
            SpreadsheetEntry spreadsheet = spreadsheets.get(0);
            System.out.println(spreadsheet.getTitle().getPlainText());

            // Get the first worksheet of the first spreadsheet.
            // TODO: Choose a worksheet more intelligently based on your
            // app's needs.
            WorksheetFeed worksheetFeed = service.getFeed(
                spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
            List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
            WorksheetEntry worksheet = worksheets.get(0);

            // Fetch the list feed of the worksheet.
            URL listFeedUrl = worksheet.getListFeedUrl();
            ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);

            // Iterate through each row, printing its cell values.
            for (ListEntry row : feed.getEntries()) {
              // Print the first column's cell value
              System.out.print(row.getTitle().getPlainText() + "\t");
              // Iterate over the remaining columns, and print each cell value
              for (String tag : entry.getCustomElements().getTags()) {
                System.out.print(row.getCustomElements().getValue(tag) + "\t");
              }
              System.out.println();
            }
          }*/

/*

 
        try {
            UrlHelper urlIdeas = new UrlHelper( SpreadsheetRestProvider.IDEA_API_URL + "/1.0/feedbacks");
            urlIdeas.addParameter( "access_token", SpreadsheetRestProvider.ACCESS_TOKEN);
            urlIdeas.addParameter( "limit", "50");
            urlIdeas.addParameter( "search", query);
            urlIdeas.openConnections();
            JsonNode ideas = mapper.readTree(urlIdeas.getStream());
            for (JsonNode idea : (ArrayNode) ideas) {
                Attribute att = storeHelper.addAttribute(String.valueOf(idea.get("id").getIntValue()));
                att.addProperty("name", idea.get("title").getTextValue());
                att.addProperty("url", idea.get("permalink").getTextValue());
                
                // Author
                Entity ent = storeHelper.addEntity(String.valueOf(idea.get("user_id").getIntValue()));
                ent.addAttribute( att, 1);
                // Comments
                UrlHelper urlComments = new UrlHelper( SpreadsheetRestProvider.IDEA_API_URL + "/1.0/feedbacks/" + att.getId() + "/comments");
                urlComments.addParameter( "access_token", SpreadsheetRestProvider.ACCESS_TOKEN);
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
                UrlHelper urlVotes = new UrlHelper( SpreadsheetRestProvider.IDEA_API_URL + "/1.0/feedbacks/" + att.getId() + "/comments");
                urlVotes.addParameter( "access_token", SpreadsheetRestProvider.ACCESS_TOKEN);
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
            idUsers.append(']');
            for( Entity ent : storeHelper.getEntities().values()) {
                UrlHelper urlUsers = new UrlHelper( SpreadsheetRestProvider.USER_API_URL + "/1.0/users");
                urlUsers.addParameter( "access_token", SpreadsheetRestProvider.ACCESS_TOKEN);
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
        }*/
        return storeHelper.toJson();
    }
}
