JMI.namespace("google.Visualization");

// Mandatory 
JMI.google.Visualization = function(container) {
  this.container = container;
  this.map = JMI.Map({
		  parent: this.container, 
		  clientUrl: 'http://labs.just-map-it.com/jmi-client/', 
		  //server: 'http://localhost:8080/jmi-server/',
		  method: 'POST'
		});
  this.map.addEventListener(JMI.Map.event.ACTION, function(event) {
	  JMI.google.Visualization[event.fn](event.map, event.args);
	} );
};
	
// Mandatory 
JMI.google.Visualization.prototype.draw = function(data, options) {
  if( options.breadcrumb) {
	  new JMI.extensions.Breadcrumb(options.breadcrumb,this.map,{'namingFunc':JMI.google.Visualization.JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
	  this.map.breadcrumbTitles = { shortTitle: 'Initial map', longTitle: 'Initial map' };
  }
  new JMI.extensions.Slideshow(this.map);
  
  this.map.source = options.source;
  this.map.sourceId = options.sourceId;
  this.map.visualizationData = data;
  this.map.invert = options.invert;
  
  var parameters = JMI.google.Visualization.getParams(this.map);
  parameters.analysisProfile='GlobalProfile';
  
  this.map.compute( parameters);
};

JMI.google.Visualization.JMIF_breadcrumbTitlesFunc = function(event) {
  if( event.type === JMI.Map.event.EMPTY) {
	return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
  }
  if( event.type === JMI.Map.event.ERROR) {
	return {shortTitle: 'Sorry, an error occured.' + event.message, longTitle: 'Sorry, an error occured. Error: ' + event.message};
  }
  return event.map.breadcrumbTitles;
};

JMI.google.Visualization.getParams = function(map) {
  return { 
	map: 'GoogleVisualization',
	source: map.source,
	sourceId: map.sourceId,
	data: map.visualizationData,
	inverted: map.invert
	};
};

JMI.google.Visualization.JMIF_Navigate = function(map, url) {
  window.open( url, "_blank");
};

JMI.google.Visualization.JMIF_Focus = function(map, args) {
  var parameters = JMI.google.Visualization.getParams(map);
  parameters.entityId = args[0];
  map.compute( parameters);
  map.breadcrumbTitles.shortTitle = "Focus";
  map.breadcrumbTitles.longTitle = "Focus on: " + args[1];
};

JMI.google.Visualization.JMIF_Center = function(map, args) {
  var parameters = JMI.google.Visualization.getParams(map);
  parameters.attributeId = args[0];
  parameters.analysisProfile = "DiscoveryProfile";
  map.compute( parameters);
  map.breadcrumbTitles.shortTitle = "Centered";
  map.breadcrumbTitles.longTitle = "Centered on: " + args[1];
};   
