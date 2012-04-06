<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Just Map It! clients</title>
<meta name="robots" content="index,follow" /> 
<meta name="viewport" content="target- densitydpi=device-dpi, width=device-width, user-scalable=yes"/>
<meta property="og:image" content="http://labs.just-map-it.com/images/html5-logo.png" />
<link rel="stylesheet" type="text/css" href="../jmi-client/jmi-client.css" />
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript">
var mapHtml5, mapFlex;
function getParams() {
	var p = {
		map: 'Feeds',
    	feedsserverurl: 'http://feeds.just-map-it.com',
		invert: 'false',
		feed: 'http://www.social-computing.com/',
		jsessionid: '<%=session.getId()%>',
		track: '',
		site: ''
    };
    return p;
};
function GoMap() {
	var parameters = getParams();
	parameters.analysisProfile='GlobalProfile';
	mapHtml5 = JMI.Map({
				parent: 'mapHtml5', 
				swf: '../jmi-client/jmi-flex-1.0-SNAPSHOT.swf', 
				server: 'http://server.just-map-it.com' 
				//client: JMI.Map.SWF
			});
	mapHtml5.addEventListener(JMI.Map.event.READY, function(event) {
		document.getElementById('titleHtml5').innerHTML = event.map.type;
	} );
/*	mapHtml5.addEventListener(JMI.Map.event.STATUS, function(event) {
		document.getElementById('statusHtml5').innerHTML = event.message;
	} );*/
	mapHtml5.addEventListener(JMI.Map.event.ACTION, function(event) {
		window[event.fn](event.map, event.args);
	} );
	mapHtml5.addEventListener(JMI.Map.event.EMPTY, function(event) {
		document.getElementById("statusHtml5").innerHTML = 'Map is empty.';
	} );
	mapHtml5.addEventListener(JMI.Map.event.ERROR, function(event) {
		document.getElementById("statusHtml5").innerHTML = event.message;
	} );
	mapHtml5.addEventListener(JMI.Map.event.ATTRIBUTE_HOVER, function(event) {
		document.getElementById('statusHtml5').innerHTML = event.attribute.NAME;
	} );
	mapHtml5.addEventListener(JMI.Map.event.LINK_HOVER, function(event) {
		//document.getElementById('statusHtml5').innerHTML = event.link._index;
	} );
	var breadcrumbHtml5 = new JMI.extensions.Breadcrumb('breadcrumbHtml5',mapHtml5,{'thumbnail':{}});
	mapHtml5.compute(parameters);
	mapFlex = JMI.Map({
				parent: 'mapFlex', 
				swf: '../jmi-client/jmi-flex-1.0-SNAPSHOT.swf', 
				server: 'http://server.just-map-it.com', 
				client: JMI.Map.SWF
			});
	mapFlex.addEventListener(JMI.Map.event.READY, function(event) {
		document.getElementById('titleFlex').innerHTML = event.map.type;
	} );
	/*mapFlex.addEventListener(JMI.Map.event.STATUS, function(event) {
		document.getElementById('statusFlex').innerHTML = event.message;
	} );*/
	mapFlex.addEventListener(JMI.Map.event.ACTION, function(event) {
		window[event.fn](event.map, event.args);
	} );
	mapFlex.addEventListener(JMI.Map.event.EMPTY, function(event) {
		document.getElementById("statusFlex").innerHTML = 'Map is empty.';
	} );
	mapFlex.addEventListener(JMI.Map.event.ERROR, function(event) {
		document.getElementById("statusFlex").innerHTML = event.message;
	} );
	mapFlex.addEventListener(JMI.Map.event.ATTRIBUTE_HOVER, function(event) {
		document.getElementById('statusFlex').innerHTML = event.attribute.NAME;
	} );
	mapFlex.addEventListener(JMI.Map.event.LINK_HOVER, function(event) {
		//document.getElementById('statusFlex').innerHTML = event.link._index;
	} );
	var breadcrumbFlex = new JMI.extensions.Breadcrumb('breadcrumbFlex',mapFlex,{'thumbnail':{}});
	mapFlex.compute(parameters);
};
function JMIF_Navigate(map, url) {
	window.open( url, "_blank");
}
function JMIF_Focus(map, args) {
	var parameters = getParams();
	parameters.entityId = args[0];
	parameters.feed = args[2];
	map.compute( parameters);
}
function JMIF_Center(map, args) {
	var parameters = getParams();
	parameters.attributeId = args[0];
	parameters.feed = args[2];
	parameters.analysisProfile = "DiscoveryProfile";
	map.compute( parameters);
}
function search(value) {
	if( mapHtml5) {
		var res1 = mapHtml5.attributes.match(value,['NAME']);	
		var res2 = mapHtml5.links.match(value,['REC_NAME']);	
		mapHtml5.selections['search'].set( res1.concat(res2));
		mapHtml5.selections['search'].show();
		document.getElementById('statusHtml5').innerHTML = res1.length + ' objects(s) found / ' + res2.length + ' link(s) found'; 
	}
	if( mapFlex) {
		var res1 = mapFlex.attributes.match(value,['NAME']);	
		var res2 = mapFlex.links.match(value,['REC_NAME']);	
		mapFlex.selections['search'].set( res1.concat(res2));
		mapFlex.selections['search'].show();
		document.getElementById('statusFlex').innerHTML = res1.length + ' objects(s) found / ' + res2.length + ' link(s) found'; 
	}
};
</script>
<jsp:include page="../ga.jsp" />
</head>
<body onload="GoMap()">
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Labs" href="../"><img border="0" alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td align="right"><a title="Social Computing" href="http://www.social-computing.com" target="_blank"><img border="0" alt="Social Computing" src="../images/logo-sc-white.jpg" /></a></td>
	</tr>
</table>
<div>Search: <input onkeyup="search(this.value)"/><span id="res"></span></div>
<div style="position: relative; float: left; ">
<h1 id="titleHtml5">Auto</h1>
<h1 id="breadcrumbHtml5">&nbsp;</h1>
<div id="mapHtml5" style="border:1px solid black ;  width: 640px; height: 480px" ></div>
<div id="statusHtml5" style="width: 600px; overflow:hidden;">&nbsp;</div>
</div>

<div style="position: relative; float: left; padding-left:20px ">
<h1 id="titleFlex">Flash (forced)</h1>
<h1 id="breadcrumbFlex">&nbsp;</h1>
<div id="mapFlex" style="border:1px solid black ; width: 640px; height: 480px" ></div>
<div id="statusFlex" style="width: 600px; overflow:hidden;">&nbsp;</div>
</div>
</body>
</html>