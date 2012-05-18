JMI.namespace("google.Visualization");

// Mandatory 
JMI.google.Visualization = function(container) {
  this.container = container;
  this.selection = [];
  this.map = JMI.Map({
		  parent: this.container, 
		  clientUrl: 'http://labs.just-map-it.com/jmi-client/', 
		  //server: 'http://localhost:8080/jmi-server',
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
	
// Mandatory 
JMI.google.Visualization.prototype.draw = function(data, options) {
  if( options.breadcrumb) {
	  new JMI.extensions.Breadcrumb(options.breadcrumb,this.map,{'namingFunc':JMI.google.Visualization.JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
	  this.breadcrumbTitles = { shortTitle: 'Initial map', longTitle: 'Initial map' };
  }
  new JMI.extensions.Slideshow(this.map);

  // Convert google.visualization.DataTable to JSon Object
  if( typeof data !== 'object') {
	  throw 'data is not a DataTable or a DataView';
  }
  var row, col, entities = [], attributes = [], firstrow = data.getColumnLabel(0) ? 0 : 1;
  for (col = 1; col < data.getNumberOfColumns(); col++) {
      entities.push({ id: col, name: (firstrow == 0 ? data.getColumnLabel(col) : data.getFormattedValue(0, col)), attributes: []});
  }
  
  for (row = firstrow; row < data.getNumberOfRows(); row++) {
      attributes.push({id: row, name: data.getFormattedValue(row, 0)});
  }
  
  for ( row = firstrow; row < data.getNumberOfRows(); row++) {
      for ( col = 1; col < data.getNumberOfColumns(); col++) {
          var value = data.getValue(row, col);
          if(value !== null) {
              var entity = entities[col-1];
              if(typeof value === 'number') {
            	  entity.attributes.push({id: row, p: value});
              } else if(typeof value === 'boolean') {
            	  if( value) {
            		  entity.attributes.push({id: row});
            	  }
	          } else {
            	  if( value !== '') {
            		  entity.attributes.push({id: row});
    	          }
	          }
          }
      }
  }
 
  this.source = options.source || 'GSPREADSHEET';
  this.sourceId = options.sourceId || window.location.href;
  this.visualizationData = JSON.stringify({"entities": entities, "attributes": attributes});
  this.invert = options.invert || false;
  
  var parameters = JMI.google.Visualization.getParams(this.map);
  parameters.analysisProfile='GlobalProfile';
  
  this.map.compute(parameters);
};

JMI.google.Visualization.prototype.getSelection = function() {
	return this.selection;
};

JMI.google.Visualization.prototype.setSelection = function(sel) {
	if( !this.map.isReady()) return;
	if( sel && sel.length > 0) {
		var selection = [];
		for(var i = 0; i < sel.length; ++i) {
			if( sel[i].row) {
				var pattern= new RegExp('\\b' + sel[i].row + '\\b'); 
				if( !this.invert) {
					selection = selection.concat(this.map.attributes.match(pattern,['ID']));	
				}
				else {
					selection = selection.concat(this.map.attributes.match(pattern,['POSS_ID']));	
				}
			}
		}
		this.map.selections['search'].set( selection);
		this.map.selections['search'].show();
	}
	else {
		this.map.selections['search'].clear();
		this.map.selections['search'].hide();
	}
};

JMI.google.Visualization.JMIF_breadcrumbTitlesFunc = function(event) {
  if( event.type === JMI.Map.event.EMPTY) {
	return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
  }
  if( event.type === JMI.Map.event.ERROR) {
	return {shortTitle: 'Sorry, an error occured.' + event.message, longTitle: 'Sorry, an error occured. Error: ' + event.message};
  }
  return event.map.gvisualization.breadcrumbTitles;
};

JMI.google.Visualization.getParams = function(map) {
  return { 
	map: 'GoogleVisualization',
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
  map.gvisualization.breadcrumbTitles.shortTitle = "Focus";
  map.gvisualization.breadcrumbTitles.longTitle = "Focus on: " + args[1];
  map.gvisualization.selection = [ (map.gvisualization.invert ? {row: args[0]} : {column: args[0]}) ];
  google.visualization.events.trigger(map.gvisualization, 'select', {});
};

JMI.google.Visualization.JMIF_Center = function(map, args) {
  var parameters = JMI.google.Visualization.getParams(map);
  parameters.attributeId = args[0];
  parameters.analysisProfile = "DiscoveryProfile";
  map.compute( parameters);
  map.gvisualization.breadcrumbTitles.shortTitle = "Centered";
  map.gvisualization.breadcrumbTitles.longTitle = "Centered on: " + args[1];
  map.gvisualization.selection = [ (map.gvisualization.invert ? {column: args[0]} : {row : args[0]}) ];
  google.visualization.events.trigger(map.gvisualization, 'select', {});
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