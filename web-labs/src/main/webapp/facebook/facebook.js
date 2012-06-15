JMI.namespace("facebook.Map");

// Mandatory 
JMI.facebook.Map = function(container) {
  this.container = container;
  this.map = JMI.Map({
		  parent: this.container, 
		  server: 'http://localhost:8080/jmi-server',
		  clientUrl: 'http://labs.just-map-it.com/jmi-client/' 
		});
  this.map.facebook = this;
  this.map.addEventListener(JMI.Map.event.ACTION, function(event) {
	  event.map.facebook[event.fn](event.map, event.args);
	} );
  this.map.addEventListener(JMI.Map.event.READY, function(event) {
	} );
  this.map.addEventListener(JMI.Map.event.EMPTY, function(event) {
	} );
  this.map.addEventListener(JMI.Map.event.ERROR, function(event) {
	} );
};
	
JMI.facebook.Map.prototype.draw = function(mode,options) {
  options = options || {};
  
  if( options.breadcrumb) {
	  new JMI.extensions.Breadcrumb(options.breadcrumb,this.map,{'namingFunc':JMI.facebook.Map.breadcrumbTitlesFunc,'thumbnail':{}});
	  this.breadcrumbTitles = { shortTitle: this.mode, longTitle: 'friends according ' + this.mode };
  }
  new JMI.extensions.Slideshow(this.map);

  var parameters = this.getParams(mode);
  parameters.analysisProfile='GlobalProfile';
  this.map.compute(parameters);
};

JMI.facebook.Map.prototype.getParams = function() {
  return { 
	map: 'Facebook',
    //fbserverurl: 'http://localhost:8080/web-labs',
    fbserverurl: 'http://facebook.just-map-it.com',
    jsessionid: this.session,
    access_token: this.accessToken,
    fbuserid: this.fbuserid,
	kind: this.mode
	};
};

JMI.facebook.Map.prototype.compute = function() {
  var parameters = this.getParams();
  parameters.analysisProfile='GlobalProfile';
  this.map.compute(parameters);
};

JMI.facebook.Map.prototype.JMIF_Focus = function(map, args) {
  var parameters = map.getParams();
  parameters.entityId = args[0];
  map.compute( parameters);
  map.facebook.breadcrumbTitles.shortTitle = "Focus";
  map.facebook.breadcrumbTitles.longTitle = "Focus on: " + args[1];
};

JMI.facebook.Map.prototype.Discover = function(map, args) {
  var parameters = this.getParams();
  parameters.attributeId = args[0];
  parameters.analysisProfile = "DiscoveryProfile";
  map.compute( parameters);
  map.facebook.breadcrumbTitles.shortTitle = "Centered";
  map.facebook.breadcrumbTitles.longTitle = "Centered on: " + args[1];
};   

JMI.facebook.Map.prototype.Display=function( map, args) {
	$.ajax({
		  type: "GET",
		  url: "https://graph.facebook.com/" + args[0]
	}).done(function( msg ) {
		var jso = jQuery.parseJSON(msg);
		if( jso && jso.link)
			link = jso.link;
		else
			link = "http://www.facebook.com/profile.php?id=" + args[0];
		window.open( link, "_blank");
	});
};

/*JMI.facebook.Map.prototype.uploadAsPhoto = function( doTag) {
	var name = "My friends sharing " + type.selectedItem.type;
		image = this.getImage();
	var mploader:MultipartURLLoader = new MultipartURLLoader();
	mploader.addEventListener(
		Event.COMPLETE, 
		function onComplete(e:Event):void {
			var jso:Object = decodeJson( mploader.getData());
			if( doTag && jso.hasOwnProperty( "id")) {
				map.tagUsersInPhoto( jso["id"]);
			}
			photo.enabled = true;
			tag.enabled = true;
		});
	mploader.addVariable( "access_token", this.getProperty( "$access_token"));
	mploader.addFile( image, "just-map-it.png", "filedata", "image/png");
	mploader.load( "https://graph.facebook.com/" + this.getProperty("$MY_FB_ID") + "/photos");
};

JMI.facebook.Map.prototype.tagUsersInPhoto = function(photo) {
	for each( var user:Attribute in map.attributes) {
		if( user._VERTICES) {
			var x = user._VERTICES[0].x - 10,
				y = user._VERTICES[0].y - 10;
			var mploader:MultipartURLLoader = new MultipartURLLoader();
			mploader.addEventListener(
				Event.COMPLETE, 
				function onReady(e:Event):void {
					//Alert.show( "ok");
				});
			mploader.addVariable( "access_token", this.getProperty( "$access_token"));
			mploader.addVariable( "x", int( x * 100 /map.width));
			mploader.addVariable( "y", int( y * 100 /map.height));
			mploader.load( "https://graph.facebook.com/" + photo + "/tags/" + user.ID);
		}
	}
};*/

JMI.facebook.Map.breadcrumbTitlesFunc = function(event) {
  if( event.type === JMI.Map.event.EMPTY) {
	return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
  }
  if( event.type === JMI.Map.event.ERROR) {
	if(event.track) {
		return {shortTitle: 'Sorry, an error occured. If you want to be informed about it, please <a title="Fill the form" href="http://www.just-map-it.com/p/report.html?track='+ event.track +'" target="_blank">fill the form</a>', longTitle: 'Sorry, an error occured. Error: ' + event.message};
	}
	else {
		return {shortTitle: 'Sorry, an error occured. ' + event.message, longTitle: 'Sorry, an error occured. ' + event.message};
	}
  }
  return event.map.facebook.breadcrumbTitles;
};

