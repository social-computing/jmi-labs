JMI.namespace("facebook.Map");

// Mandatory 
JMI.facebook.Map = function(container) {
  this.container = container;
  this.map = JMI.Map({
		  parent: this.container, 
		  //server: 'http://localhost:8080/jmi-server',
		  clientUrl: 'http://labs.just-map-it.com/jmi-client/' 
		});
  this.map.facebook = this;
  this.map.addEventListener(JMI.Map.event.ACTION, function(event) {
	  event.map.facebook[event.fn](event.map, event.args);
	} );
  this.map.addEventListener(JMI.Map.event.READY, function(event) {
	  event.map.facebook.toolbarEnable( true, true);
	} );
  this.map.addEventListener(JMI.Map.event.EMPTY, function(event) {
	  event.map.facebook.toolbarEnable( true, false);
	} );
  this.map.addEventListener(JMI.Map.event.ERROR, function(event) {
	  event.map.facebook.toolbarEnable( true, false);
	} );
};
	
JMI.facebook.Map.prototype.draw = function(mode,options) {
  options = options || {};
  
  if( options.breadcrumb) {
	  new JMI.extensions.Breadcrumb(options.breadcrumb,this.map,{'namingFunc':JMI.facebook.Map.breadcrumbTitlesFunc,'thumbnail':{}});
	  this.breadcrumbTitles = { shortTitle: this.mode, longTitle: 'friends according ' + this.mode };
  }
  new JMI.extensions.Slideshow(this.map, 'slideshow', 500, 300, 5000);

  var parameters = this.getParams(mode);
  parameters.analysisProfile='GlobalProfile';
  this.map.compute(parameters);
};

JMI.facebook.Map.prototype.getParams = function() {
  return { 
	map: 'FacebookLabs',
    //fbserverurl: 'http://localhost:8080/web-labs',
    fbserverurl: 'http://labs.just-map-it.com',
    jsessionid: this.session,
    access_token: this.accessToken,
    fbuserid: this.fbuserid,
	kind: this.mode
	};
};

JMI.facebook.Map.prototype.compute = function() {
  var parameters = this.getParams();
  parameters.analysisProfile='GlobalProfile';
  this.toolbarEnable( false, false);
  this.map.compute(parameters);
};

JMI.facebook.Map.prototype.toolbarEnable = function(mode, photo) {
	$( "#mode" ).buttonset(  { disabled: !mode} );
	$( "#upload, #tag" ).button(  { disabled: !photo} );
};

/*JMI.facebook.Map.prototype.JMIF_Focus = function(map, args) {
  var parameters = map.getParams();
  parameters.entityId = args[0];
  map.compute( parameters);
  map.facebook.breadcrumbTitles.shortTitle = "Focus";
  map.facebook.breadcrumbTitles.longTitle = "Focus on: " + args[1];
};*/

JMI.facebook.Map.prototype.Discover = function(map, args) {
  var parameters = this.getParams();
  parameters.attributeId = args[0];
  parameters.analysisProfile = "DiscoveryProfile";
  this.toolbarEnable( false, false);
  map.compute( parameters);
  map.facebook.breadcrumbTitles.shortTitle = map.facebook.mode+', centered';
  map.facebook.breadcrumbTitles.longTitle = 'friends according ' + map.facebook.mode + ', centered on ' + args[1];
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

JMI.facebook.Map.prototype.uploadAsPhoto = function( doTag, mode) {
	var map = this;
	$.ajax({
		  type: 'POST',
		  url: this.getParams().fbserverurl + '/rest/facebook/upload-photo',
		  //contentType: false,
		  //processData: false,
		  data: {
			  token: this.map.getProperty( '$access_token'),
			  id: this.map.getProperty( '$MY_FB_ID'),
			  image: this.map.getImage().substring(22),
			  title: 'Just Map It!'
		  }
	}).done(function( msg ) {
		if( doTag && msg.id) {
			map.tagUsersInPhoto( msg.id);
		};
	}).fail(function() { 
		alert("error while uploading photo"); 
	});
};

JMI.facebook.Map.prototype.tagUsersInPhoto = function(photo) {
	var i, user;
	for( i = 0; i < this.map.attributes.length; ++i) {
		user = this.map.attributes[i];
		if( user._VERTICES) {
			var x = user._VERTICES[0].x - 10,
				y = user._VERTICES[0].y - 10;
			$.ajax({
				  type: 'GET',
				  url: 'https://graph.facebook.com/' + photo + '/tags/' + user.ID,
				  data: {
					  access_token: this.map.getProperty( '$access_token'),
					  x: Math.round( x * 100 /this.map.size.width),
					  y: Math.round( y * 100 /this.map.size.height)
				  }
			}).done(function( msg ) {
			}).fail(function() { 
				alert("error while tagging photo"); 
			});
		}
		//break;
	}
};

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

