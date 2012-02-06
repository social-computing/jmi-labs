package com.socialcomputing.labs.edd;

import java.io.File;
import java.io.FilenameFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.jdom.Element;

import com.socialcomputing.wps.server.planDictionnary.connectors.WPSConnectorException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

@Path("/edd")
public class EddRestProvider {
    
    private static final String SYLLABS_API_KEY = "ced225f44e11f48070268fb93d28618f";
    
    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request,  @DefaultValue("") @QueryParam("entities") String ets, @DefaultValue("") @QueryParam("entreprise") String entreprise) {
        HttpSession session = request.getSession(true);
        String key = entreprise + "_" + ets;
        String result = null;//( String)session.getAttribute( key);
        String path = session.getServletContext().getRealPath("/edd/data/");
        if (result == null || result.length() == 0) {
           String[] entities = ets.split( ",");
           result = files( path + File.separator + entreprise, entities);
           session.setAttribute( key, result);
        }
        return result;
    }

    public String files( String path, String[] entities) {
        StoreHelper storeHelper = new StoreHelper();

        File pathFile = new File(path);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jig");
            }
        };        
        File files[] = pathFile.listFiles(filter);
        for( File file : files) {
            try {
                read( file, entities, storeHelper);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return storeHelper.toJson();
    }

    private void read(File file, String[] entities, StoreHelper storeHelper) throws WPSConnectorException {
        try {
            org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder(false);
            org.jdom.Document doc = builder.build( file);
            Element root = doc.getRootElement();
            
            Element document = root.getChild("document");
            int count = 0;
            Attribute attribute = storeHelper.addAttribute( document.getChildText( "docID"));
            attribute.addProperty( "name", file.getCanonicalPath());
                
            count += extractEntities( attribute, entities, document.getChildText("docText"), storeHelper);
        } catch (Exception e) {
            throw new WPSConnectorException( "openConnections", e);
        }
    }
        
    private int extractEntities( Attribute attribute, String[] entities, String content, StoreHelper storeHelper) {
        int count = 0;
        UrlHelper syllabs = new UrlHelper( UrlHelper.Type.POST, "http://api.syllabs.com/v0/entities");
        syllabs.addHeader( "API-Key", SYLLABS_API_KEY);
        syllabs.addHeader( "Accept", "application/json");
        syllabs.addParameter( "indent", "false");
        syllabs.addParameter( "text", content);
        try {
            syllabs.openConnections();
            JsonNode ets = mapper.readTree( syllabs.getStream()).get("response").get("entities");
            for( String namedEntity : entities) {
                ArrayNode persons = (ArrayNode)ets.get( namedEntity);
                if( persons != null) {
                    for (JsonNode person : persons) {
                        Entity entity = storeHelper.addEntity( person.get( "text").getTextValue());
                        entity.addProperty( "name", entity.getId());
                        entity.addAttribute(attribute, person.get( "count").getIntValue());
                        ++count;
                    }
                }
            }
            syllabs.closeConnections();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return count;
    }
    
    
/*    public static void main( String[] args ) {
        EddRestProvider s = new SyllabsRestProvider();
        String url = "http://www.social-computing.com/2012/01/18/just-map-it-days-merci-pour-vos-retours/";
        s.extractTextAndEntities( url, new Attribute(url), null, new StoreHelper());
    }*/
}
