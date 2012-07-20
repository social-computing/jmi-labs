JMI.namespace("deezer");
	
/**
 * Just Map It! Map component
 * This object exposes properties and methods to control the interactions with map elements
 * 
 * @param container string  id of the map container in which the map is rendered
 * @param options   object  properties that will be passed to the JMI server by default 
 * @returns {JMI.deezer.map} a map instance
 */
JMI.deezer.map = function(container, options) {
	this.container = container;
	
	// Initialize map default parameters
	// And override them with values given in parameter
	this.options = JMI.deezer.Merge(
			{
				'clientUrl': '../jmi-client/',
				'map': {
					'analysisProfile': 'GlobalProfile',
				},
			},
			options
	);
	
	this.map = JMI.Map({
		parent: this.container,
		clientUrl: this.options.clientUrl
	});
	
	// Cross reference
	// Needed so that this instance is available in the map obj returned when an event is fired
	this.map.deezer = this;
	this.map.addEventListener(JMI.Map.event.ACTION, function(event) {
		event.map.deezer[event.fn](event.map, event.args);
	});
	this.map.addEventListener(JMI.Map.event.READY, function(event) {
	});
	this.map.addEventListener(JMI.Map.event.EMPTY, function(event) {
	});
	this.map.addEventListener(JMI.Map.event.ERROR, function(event) {
	});
	if (this.options.breadcrumb) {
		new JMI.extensions.Breadcrumb(this.options.breadcrumb, this.map, {'namingFunc': JMI.deezer.breadcrumb, 'thumbnail': {}});
		this.breadcrumbTitles = {shortTitle: 'Artists map', longTitle: 'Favorite artists map'};
	}
	new JMI.extensions.Slideshow(this.map);
};

JMI.deezer.map.prototype.draw = function(options) {
	var parameters = JMI.deezer.Merge(this.options.map, options);	
    this.map.compute(parameters);
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
JMI.deezer.map.prototype.Focus = function(map, args) {
	map.deezer.draw(
		{
			'entityId': args[0],
			'analysisProfile': 'AnalysisProfile',
		}	
	);
    map.deezer.breadcrumbTitles.shortTitle = "Focus";
    map.deezer.breadcrumbTitles.longTitle = "Focus on: " + args[1];
};

/**
 * Center
 * Called when an attribute is selected (node)
 * 
 * @param map
 * @param args 
 */
JMI.deezer.map.prototype.Center = function(map, args) {
	map.deezer.draw(
		{
			'attributeId': args[0],
			'analysisProfile': 'DiscoveryProfile',
			
		}	
	);	
    map.deezer.breadcrumbTitles.shortTitle = "Centered";
    map.deezer.breadcrumbTitles.longTitle = "Centered on: " + args[1];
};

/**
 * Navigate
 * Open a new window with specified URL as location
 * 
 * @param map
 * @param args
 */
JMI.deezer.map.prototype.Navigate = function(map, url) {
	window.open(url, "_blank");
};

/**
 * Add / Remove an artist to the list of user's favorite artists
 * 
 * @param map
 * @param args
 */
JMI.deezer.map.prototype.AddFavorites = function(map, args) {
    DZ.getLoginStatus(function(response) {
        if (response.authResponse) {
            var userID       = response.userID,
                id           = args[0],
                service_uri  = "/user/" +  userID + "/artists";
                query_params = { 'access_token': response.authResponse.accessToken},
                attribute = map.attributes.match(new RegExp('\\b' + id + '\\b'), ['ID']),
                action = attribute[0].INFAVLIST ? 'delete' : 'post';

                query_params['artist_id'] = args[0];

            console.log("calling deezer api %s service with %s request method and %s parameters", service_uri, action, query_params);
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
                           console.debug("Attribute infavlist value before setProperty call : %s", attribute[0].INFAVLIST);
                           attribute[0].setProperty('INFAVLIST', !attribute[0].INFAVLIST);
                           console.debug("Attribute infavlist value after setProperty call : %s", attribute[0].INFAVLIST);
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
                           console.debug(response);
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


/*
 * ******************************* *
 * * Define map breadcrumb       * *
 * ******************************* *
 */
/**
 * Breadcrumb display function
 * 
 * @param event  event fired that triggered this function call
 */
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
    
    return event.map.deezer.breadcrumbTitles;
};


/*
 * ******************************* *
 * * Utility functions           * *
 * ******************************* *
 */
/**
 * Merge 2 objects properties
 * 
 * @param obj1 object  
 * @param obj1 object
 * @returns an object resulting of the merge of the 2 given objects
 *                    The properties of obj1 are taken as default values and overriden by obj2 propertie
 */
// TODO: Copy some elements from jquery extend function
// TODO: Make this recursive ?
JMI.deezer.Merge = function(obj1, obj2) {
	 obj1 = obj1 || {};
	 obj2 = obj2 || {};
	 
	 for (var a in obj2) { 
		 obj1[a] = obj2[a];
	 }
	 return obj1;
};