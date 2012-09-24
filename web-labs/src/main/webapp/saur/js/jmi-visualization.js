JMI.namespace("google.Visualization");

// Mandatory 
JMI.google.Visualization = function(container) {
  this.container = container;
  this.selection = [];
  this.map = JMI.Map({
		  parent: this.container, 
		  clientUrl: '../jmi-client/', 
		  //server: 'https://server.just-map-it.com',
		  //server: 'http://localhost:8080/jmi-server',
		  //client: JMI.Map.SWF,
		  method: 'POST'
		});
  this.map.gvisualization = this;
  this.map.addEventListener(JMI.Map.event.ACTION, function(event) {
	  JMI.google.Visualization[event.fn](event.map, event.args);
	} );
  this.map.addEventListener(JMI.Map.event.READY, function(event) {
	  google.visualization.events.trigger(event.map.gvisualization, 'ready', {});
	} );
  this.map.addEventListener(JMI.Map.event.EMPTY, function(event) {
	  google.visualization.events.trigger(event.map.gvisualization, 'ready', {});
	} );
  this.map.addEventListener(JMI.Map.event.ERROR, function(event) {
	  google.visualization.events.trigger(event.map.gvisualization, 'error', {id:event.map.gvisualization.container.ID, message:event.message});
	} );
};
	
String.prototype.trim = function() {
    return this.replace(/^\s+|\s+$/g, "");
};

// Mandatory 
JMI.google.Visualization.prototype.draw = function(data, options) {
  options = options || {};
  
  if( options.breadcrumb) {
	  new JMI.extensions.Breadcrumb(options.breadcrumb,this.map,{'namingFunc':JMI.google.Visualization.JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
	  this.breadcrumbTitles = { shortTitle: 'Carte initiale', longTitle: 'Carte initiale' };
  }
  new JMI.extensions.Slideshow(this.map);

  // Convert google.visualization.DataTable to JSon Object
  if( typeof data !== 'object') {
	  throw 'data is not a DataTable or a DataView';
  }
  this.dataFormat = options.dataFormat || 'matrix';
  var entities = [], entities0 = {}, attributes = [], attributes0 = {};

  var row, firstrow = data.getColumnLabel(0) ? 0 : 1, id, id2, val;

  for (row = firstrow; row < data.getNumberOfRows(); row++) {
	  id = data.getFormattedValue(row, 2) + ' ' + data.getFormattedValue(row, 3);
	  id = id.trim();
	  if( id.length > 0) {
		  if( !attributes0[id]) {
			  attributes0[id] = {id: id, name: id, fonction: []};
		  }
		  val = data.getFormattedValue(row, 9);
		  attributes0[id].couleur = '#ffb400';
		  if( val && val.length > 0) {
			  attributes0[id].couleur = val === 'g' ? '#F51152' : '#09589D';
		  } 
		  attributes0[id].fonction.push(data.getFormattedValue(row, 4) + ', ' + data.getFormattedValue(row, 1));
		  attributes0[id].appartenance = data.getFormattedValue(row, 8);
		  id2 = data.getFormattedValue(row, 1);
		  id2 = id2.trim();
		  if( id2.length > 0) {//} && id2 !== 'Ville de Marseille') {
			  if( !entities0[id2]) {
				  entities0[id2] = {id: row, name: id2, attributes: [], fonction: []};
			  }
			  entities0[id2].attributes.push({id: attributes0[id].id});
			  entities0[id2].couleur = '#ffb400';
			  //entities0[id2].fonction.push(id + ', ' + data.getFormattedValue(row, 4));
		  }
		  id2 = data.getFormattedValue(row, 8);
		  id2 = id2.trim();
		  if( id2.length > 0) {
			  if( !entities0[id2]) {
				  entities0[id2] = {id: 'p'+ row, name: id2, attributes: []};
			  }
			  entities0[id2].attributes.push({id: attributes0[id].id});
			  entities0[id2].couleur = '#ffb400';
		  }
	  }
  }
  for( id in attributes0) {
	  attributes.push(attributes0[id]);
  }
  for( id in entities0) {
	  entities.push(entities0[id]);
  }
	  
  this.source = options.source || 'GSPREADSHEET';
  if( !options.sourceId) {
	  this.source = 'LOCATION'; 
	  options.sourceId = window.location.href;
  }
  this.sourceId = options.sourceId;
  this.invert = options.invert || false;
  this.visualizationDataSaved = {"entities": entities, "attributes": attributes};
  this.visualizationData = JSON.stringify({"entities": entities, "attributes": attributes});
  
  var parameters = JMI.google.Visualization.getParams(this.map);
  parameters.analysisProfile='GlobalProfile';
  
  this.map.compute(parameters);
};

JMI.google.Visualization.prototype.getSelection = function() {
	return this.selection;
};

JMI.google.Visualization.prototype.setSelection = function(sel) {
	if( !this.map.isReady()) {
		return;
	}
	if( sel && sel.length > 0 && this.map.gvisualization.dataFormat!=='columnLists') {
		var selection = [];
		for(var i = 0; i < sel.length; ++i) {
			if( sel[i].row) {
				var pattern= new RegExp('^' + sel[i].row + '$'); 
				if( !this.invert) {
					selection = selection.concat(this.map.attributes.match(pattern,['ID']));	
				}
				else {
					selection = selection.concat(this.map.attributes.match(pattern,['POSS_ID']));	
				}
			}
		}
		this.map.selections.search.set( selection);
		this.map.selections.search.show();
	}
	else {
		this.map.selections.search.clear();
		this.map.selections.search.hide();
	}
};

JMI.google.Visualization.JMIF_breadcrumbTitlesFunc = function(event) {
  if( event.type === JMI.Map.event.EMPTY) {
	return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
  }
  if( event.type === JMI.Map.event.ERROR) {
	if( event.code === 1000) {
		// Quota error
		return {shortTitle: event.message, longTitle: event.message};
	}
	if(event.track) {
		return {shortTitle: 'Sorry, an error occured. If you want to be informed about it, please <a title="Fill the form" href="http://www.just-map-it.com/p/report.html?track='+ event.track +'" target="_blank">fill the form</a>', longTitle: 'Sorry, an error occured. Error: ' + event.message};
	}
	else {
		return {shortTitle: 'Sorry, an error occured. ' + event.message, longTitle: 'Sorry, an error occured. ' + event.message};
	}
  }
  return event.map.gvisualization.breadcrumbTitles;
};

JMI.google.Visualization.getParams = function(map) {
  return { 
	map: 'Saur',
	source: map.gvisualization.source,
	sourceId: map.gvisualization.sourceId,
	data: map.gvisualization.visualizationData,
	inverted: map.gvisualization.invert
	};
};

JMI.google.Visualization.JMIF_Navigate = function(map, url) {
  window.open( url, "_blank");
};

JMI.google.Visualization.JMIF_Focus = function(map, args) {
  var parameters = JMI.google.Visualization.getParams(map);
  parameters.entityId = args[0];
  map.compute( parameters);
  map.gvisualization.breadcrumbTitles.shortTitle = args[1];
  map.gvisualization.breadcrumbTitles.longTitle = "Focus sur : " + args[1];
  map.gvisualization.selection = [ (map.gvisualization.invert || map.gvisualization.dataFormat==='columnLists'  ? {row: args[0]} : {column: args[0]}) ];
  google.visualization.events.trigger(map.gvisualization, 'select', {});
};

JMI.google.Visualization.JMIF_Center = function(map, args) {
  var parameters = JMI.google.Visualization.getParams(map);
  parameters.attributeId = args[0];
  parameters.analysisProfile = "DiscoveryProfile";
  map.compute( parameters);
  map.gvisualization.breadcrumbTitles.shortTitle = args[1];
  map.gvisualization.breadcrumbTitles.longTitle = "Centré sur : " + args[1];
  map.gvisualization.selection = [ (map.gvisualization.invert || map.gvisualization.dataFormat==='columnLists' ? {column: args[0]} : {row : args[0]}) ];
  google.visualization.events.trigger(map.gvisualization, 'select', {});
};   

JMI.google.Visualization.JMIF_InvertAndCenter = function(map, args) {
	map.gvisualization.invert = !map.gvisualization.invert;
	JMI.google.Visualization.JMIF_Center( map, args);
};   

var JSON = JSON || {};
JSON.stringify = JSON.stringify || function (obj) {
    var t = typeof (obj);
    if (t != "object" || obj === null) {
        // simple data type
        if (t == "string") obj = '"' + obj + '"';
        return String(obj);
    }
    else {
        // recurse array or object
        var n, v, json = [], arr = (obj && obj.constructor == Array);
        for (n in obj) {
            v = obj[n]; t = typeof (v);
            if (t == "string") v = '"' + v + '"';
            else if (t == "object" && v !== null) v = JSON.stringify(v);
            json.push((arr ? "" : '"' + n + '":') + String(v));
        }
        return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
    }
};