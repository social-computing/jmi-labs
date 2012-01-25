package com.socialcomputing.labs.syllabs;

import java.util.ArrayList;
import java.util.List;

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
import org.jdom.Text;

import au.id.jericho.lib.html.Segment;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;

import com.socialcomputing.wps.server.planDictionnary.connectors.WPSConnectorException;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.StoreHelper;
import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

@Path("/syllabs")
public class SyllabsRestProvider {
    
    private static final String SYLLABS_API_KEY = "ced225f44e11f48070268fb93d28618f";
    private static final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Path("maps/map.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String kind(@Context HttpServletRequest request,  @DefaultValue("") @QueryParam("url") String urls, @DefaultValue("") @QueryParam("entities") String ets) {
        HttpSession session = request.getSession(true);
        String key = urls + "_" + ets;
        String result = ( String)session.getAttribute( key);
        String[] entities = ets.split( ",");
        if (result == null || result.length() == 0) {
           result = feeds( urls, entities);
           session.setAttribute( key, result);
        }
        return result;
    }

    public String feeds( String urlLst, String[] entities) {
        StoreHelper storeHelper = new StoreHelper();
        List<String> titles = new ArrayList<String>();
        List<String> urls = new ArrayList<String>();
        List<Integer> counts = new ArrayList<Integer>();
        if( entities.length > 0) {
            try {
                for( String url : urlLst.split( ",")) {
                    url = url.trim();
                    if( !url.startsWith( "http://") && !url.startsWith( "http://")) {
                        url = "http://" + url;
                    }
                    UrlHelper feed = new UrlHelper( url);
                    feed.openConnections();
                    read( feed, entities, storeHelper, titles, urls, counts);
                    feed.closeConnections();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        storeHelper.addGlobal( "FEEDS_TITLES",  titles.toArray());
        storeHelper.addGlobal( "FEEDS_URLS",    urls.toArray());
        storeHelper.addGlobal( "FEEDS_COUNTS",  counts.toArray());
        return storeHelper.toJson();
    }

    private void read(UrlHelper feed, String[] entities, StoreHelper storeHelper, List<String> titles, List<String> urls, List<Integer> counts) throws WPSConnectorException {
        String content = feed.getContentType();
        if( content.contains( "text/html")) {
            // HTML ?
            Source source;
            try {
                source = new Source( feed.getStream());
            } catch (Exception e) {
                throw new WPSConnectorException( "openConnections", e);
            }
            List<StartTag> tags = ((Segment)source.findAllElements( "head").get( 0)).findAllStartTags( "link");
            for( StartTag tag : tags) {
                String type = tag.getAttributeValue( "type");
                if( type != null && type.equalsIgnoreCase( "application/rss+xml")) {
                    UrlHelper curFeed = new UrlHelper();
                    String url = tag.getAttributeValue( "href");
                    curFeed.setUrl( url.startsWith( "/") ? feed.getUrl() + url : url);
                    curFeed.openConnections( );
                    readXml( curFeed, entities, storeHelper, titles, urls, counts);
                    curFeed.closeConnections();
                }
            }
        }
        else {
            readXml( feed, entities, storeHelper, titles, urls, counts);
        }
    }
    
    private void readXml(UrlHelper feed, String[] entities, StoreHelper storeHelper, List<String> titles, List<String> urls, List<Integer> counts) throws WPSConnectorException {
        try {
            org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder(false);
            org.jdom.Document doc = builder.build( feed.getStream());
            Element root = doc.getRootElement();
            
            Element top = root.getChild( "channel");
            if( top != null) {
                parseRss2( feed.getUrl(), entities, top, storeHelper, urls, titles, counts);
            }
            else {
                parseAtom( feed.getUrl(), entities, root, storeHelper, urls, titles, counts);
            }
        } catch (Exception e) {
            throw new WPSConnectorException( "openConnections", e);
        }
    }
    
    private void parseAtom( String url, String[] entities, Element feed, StoreHelper storeHelper, List<String> urls, List<String> titles, List<Integer> counts) {
        String title = "";
        int count = 0;
        for( Object o : (List<Object>)feed.getContent()) {
            if( o instanceof Element) {
                Element item = (Element) o;
                if( item.getName().equalsIgnoreCase( "title")) {
                    title = getAtomContent( item);
                }
                if( item.getName().equalsIgnoreCase( "entry")) {
                    List<Object> lo = item.getContent();
                    Attribute attribute = storeHelper.addAttribute( getAtomId( lo));
                    for( Object co : lo) {
                        if( co instanceof Element) {
                            Element contentItem = (Element) co;
                            if( contentItem.getName().equalsIgnoreCase( "title"))
                                attribute.addProperty( "name", getAtomContent( contentItem));
                        }
                    }
                    count += extractTextAndEntities( attribute.getId(), attribute, entities, storeHelper);
                }
            }
        }
        urls.add( url);
        counts.add( count);
        titles.add( title);
    }

    private String getAtomId( List<Object> content) {
        for( Object o : content) {
            if( o instanceof Element) {
                Element item = (Element) o;
                if( item.getName().equalsIgnoreCase( "link") && item.getAttributeValue( "rel").equalsIgnoreCase( "alternate"))
                    return item.getAttributeValue( "href");
            }
        }
        return null;
    }
    
    private String getAtomContent( Element item) {
        return ((Text)item.getContent().get( 0)).getText();
    }
    
    private void parseRss2( String url, String[] entities, Element channel, StoreHelper storeHelper, List<String> urls, List<String> titles, List<Integer> counts) {
        String title = channel.getChildText( "title");
        int count = 0;
        for( Element item : (List<Element>)channel.getChildren( "item")) {
            Attribute attribute = storeHelper.addAttribute( item.getChildText( "link"));
            attribute.addProperty( "name", item.getChildText( "title"));
            
            count += extractTextAndEntities( attribute.getId(), attribute, entities, storeHelper);
        }
        urls.add( url);
        counts.add( count);
        titles.add( title);
    }
        
    private int extractEntities( String url, Attribute attribute, String[] entities, StoreHelper storeHelper) {
        int count = 0;
        UrlHelper syllabs = new UrlHelper( "http://api.syllabs.com/v0/entities");
        syllabs.addHeader( "API-Key", SYLLABS_API_KEY);
        syllabs.addHeader( "Accept", "application/json");
        syllabs.addParameter( "indent", "false");
        syllabs.addParameter( "url", url);
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
    
    private int extractTextAndEntities( String url, Attribute attribute, String[] entities, StoreHelper storeHelper) {
        int count = 0;
        UrlHelper syllabsText = new UrlHelper( "http://api.syllabs.com/v0/extract");
        syllabsText.addHeader( "API-Key", SYLLABS_API_KEY);
        syllabsText.addHeader( "Accept", "application/json");
        syllabsText.addParameter( "indent", "false");
        syllabsText.addParameter( "url", url);
        try {
            syllabsText.openConnections();
            JsonNode textNode = mapper.readTree( syllabsText.getStream()).get("response").get("text");
            
            UrlHelper syllabs = new UrlHelper( UrlHelper.Type.POST, "http://api.syllabs.com/v0/entities");
            syllabs.addHeader( "API-Key", SYLLABS_API_KEY);
            syllabs.addHeader( "Accept", "application/json");
            syllabs.addParameter( "indent", "false");
            syllabs.addParameter( "text", textNode.getTextValue());
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
        SyllabsRestProvider s = new SyllabsRestProvider();
        String url = "http://www.social-computing.com/2012/01/18/just-map-it-days-merci-pour-vos-retours/";
        s.extractTextAndEntities( url, new Attribute(url), null, new StoreHelper());
    }*/
}
