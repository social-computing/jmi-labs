JMI.namespace("deezer.events");
    
JMI.deezer.map = function(container, breadcrumb_container, parameters) {
    var self = this;
    self.container = container;
    self.breadcrumb_container = breadcrumb_container;
    
    // Initialised the map with requiered parameters
    self.map = JMI.Map({
        parent: self.container, 
        clientUrl: parameters.clientURL, 
        server: parameters.serverURL
    });
    
    // Cross reference
	  // Needed so that this instance is available in the map obj returned when an event is fired
	  self.map.deezer = this;
    
    // Breadcrumb titles are also required
    self.map.breadcrumbTitles = {
      shortTitle: 'Artists map',
      longTitle: 'Favorite artists map'
    };
    
    // Storing other parameters
    self.parameters = parameters.map;
       
    // Attaching event listeners
    self.map.addEventListener(JMI.Map.event.ACTION, function(event) {
        JMI.deezer.events[event.fn](event.map, event.args);
    });
    
    // Init extensions 
    new JMI.extensions.Breadcrumb(self.breadcrumb_container, self.map, {'namingFunc':JMI.deezer.breadcrumb, 'thumbnail':{}});
    new JMI.extensions.Slideshow(self.map);
};

JMI.deezer.map.prototype.draw = function(parameters) {
    this.map.compute($.extend({}, this.parameters, parameters));
};


JMI.deezer.breadcrumb = function(event) {
    if (event.type === JMI.Map.event.EMPTY) {
        return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
    }
    else if (event.type === JMI.Map.event.ERROR) {
        // Quota error
        if (event.code === 1000) {
            // Only display the error message
            return {shortTitle: event.message, longTitle: event.message};
        }
        if(event.track) {
            return {shortTitle: 'Sorry, an error occured. If you want to be informed about it, please <a title="Fill the form" href="http://www.just-map-it.com/p/report.html?track='+ event.track +'" target="_blank">fill the form</a>', longTitle: 'Sorry, an error occured. Error: ' + event.message};
        }
        else {
            return {shortTitle: 'Sorry, an error occured. ' + event.message, longTitle: 'Sorry, an error occured. ' + event.message};
        }
    }
    
    return event.map.breadcrumbTitles;
};




/*
 * ******************************* *
 * * Define map events functions * *
 * ******************************* *
 */
/**
 * Focus
 * Called when an entity is selected (link)
 * 
 * @param map
 * @param args
 */
JMI.deezer.events.Focus = function(map, args) {
    map.deezer.draw(
        {
			    'entityId': args[0],
			    'analysisProfile': 'AnalysisProfile'
        }
    );
    map.deezer.map.breadcrumbTitles.shortTitle = "Focus";
    map.deezer.map.breadcrumbTitles.longTitle = "Focus on: " + args[1];
};

/**
 * Center
 * Called when an attribute is selected (node)
 * 
 * @param map
 * @param args 
 */
JMI.deezer.events.Center = function(map, args) {
    map.deezer.draw(
        {
            'attributeId': args[0],
            'analysisProfile': 'DiscoveryProfile'
        }	
    );	
    // ??? seriously ?
    map.deezer.map.breadcrumbTitles.shortTitle = "Centered";
    map.deezer.map.breadcrumbTitles.longTitle = "Centered on: " + args[1];
};

/**
 * Navigate
 * Open a new window with specified URL as location
 * 
 * @param map
 * @param args
 */
JMI.deezer.events.Navigate = function(map, url) {
	window.open(url, "_blank");
};

/**
 * Add / Remove an artist to the list of user's favorite artists
 * TODO: optimize this
 * @param map
 * @param args
 */
JMI.deezer.events.AddFavorites = function(map, args) {
    DZ.getLoginStatus(function(response) {
        if (response.authResponse) {
            var userID       = response.userID,
                id           = args[0],
                service_uri  = "/user/" +  userID + "/artists";
                query_params = { 'access_token': response.authResponse.accessToken},
                attribute = map.attributes.match(new RegExp('\\b' + id + '\\b'), ['ID']),
                action = attribute[0].INFAVLIST ? 'delete' : 'post';

                query_params['artist_id'] = args[0];

            //console.log("calling deezer api %s service with %s request method and %s parameters", service_uri, action, query_params);
            DZ.api(service_uri,
                   action,
                   query_params,
                   function(response) {
                       var msg = "this artist";
                       switch(response) {
                       case true:
                           if(action == "post") {
                               msg += "was sucessfully added to your favorite list";
                           } else {
                               msg += "was sucessfully removed from your favorite list";
                           }
                           //console.debug("Attribute infavlist value before setProperty call : %s", attribute[0].INFAVLIST);
                           attribute[0].setProperty('INFAVLIST', !attribute[0].INFAVLIST);
                           //console.debug("Attribute infavlist value after setProperty call : %s", attribute[0].INFAVLIST);
                           alert(msg);
                           break;
                       case false:
                           if(action == "post") {
                               msg += "is already in your favorite list";
                           } else {
                               msg += "is not in your favorite list";
                           }
                           alert(msg);
                           break;
                       default:
                           //console.debug(response);
                           var err_msg = "sorry an error occured";
                           
                           if(response.error) err_msg += ': ' + response.error.type + ', ' + response.error.message;
                           alert(err_msg);
                       }
                       
                       // #http://tools.ietf.org/html/rfc2616#section-10.4.1
                   }
            );
            // logged in and connected user, someone you know
        } 
        else {
            // no user session available, someone you dont know
            alert("Sorry, you are not logged in on deezer");
        }
    });
    
};    

