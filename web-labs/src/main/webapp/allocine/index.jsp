<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Allocine</title>
<meta name="robots" content="noindex,nofollow" /> 
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="target- densitydpi=device-dpi, width=device-width, user-scalable=no"/>
<style type="text/css" media="screen">
html, body {
	height: 100%;
}
#map {
	width: 100%;
	height: 80%;
	background-color: #FFFFFF;
}
img {
	border: 0;
}
</style>
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript" src="../js/jquery-1.7.1.min.js"></script>
<script type="text/javascript"> 
var breadcrumbTitles = { shortTitle: '', longTitle: '' };
function JMIF_breadcrumbTitlesFunc(event) {
	if( event.type === JMI.Map.event.EMPTY) {
		return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
	}
	if( event.type === JMI.Map.event.ERROR) {
		return {shortTitle: 'Sorry, an error occured.', longTitle: 'Sorry, an error occured. Error: ' + event.message};
	}
	return breadcrumbTitles;
}
$(document).ready(function() {
	var parameters = {};
	completeParameters( parameters);
	parameters.analysisProfile = "GlobalProfile";
	var map = JMI.Map({
				parent: 'map', 
				swf: '../jmi-client/swf/jmi-flex-1.0-SNAPSHOT.swf', 
				server: 'http://server.just-map-it.com' 
				//server: 'http://localhost:8080/jmi-server/'
				//client: JMI.Map.SWF
			});
	map.addEventListener(JMI.Map.event.READY, function(event) {
	} );
	map.addEventListener(JMI.Map.event.ACTION, function(event) {
		window[event.fn](event.map, event.args);
	} );
	map.addEventListener(JMI.Map.event.EMPTY, function(event) {
	} );
	map.addEventListener(JMI.Map.event.ERROR, function(event) {
	} );
	breadcrumbTitles.shortTitle = $('#filter option:selected')[0].label;
	breadcrumbTitles.longTitle = $('#filter option:selected')[0].label + ' - ' + $('#kind option:selected')[0].label;
	breadcrumb = new JMI.extensions.Breadcrumb('breadcrumb',map,{'namingFunc':JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
	map.compute( parameters);
	
	$('#kind').change(function(){
		 var parameters = {};
		 completeParameters( parameters);
		 parameters.analysisProfile = "GlobalProfile";
		 breadcrumb.flush();
		 breadcrumbTitles.shortTitle = $('#filter option:selected')[0].label;
		 breadcrumbTitles.longTitle = $('#filter option:selected')[0].label + ' - ' + $('#kind option:selected')[0].label;
		 $('#map')[0].JMI.compute( parameters);
	});
	$('#filter').change(function(){
		 var parameters = {};
		 completeParameters( parameters);
		 parameters.analysisProfile = "GlobalProfile";
		 breadcrumb.flush();
		 breadcrumbTitles.shortTitle = $('#filter option:selected')[0].label;
		 breadcrumbTitles.longTitle = $('#filter option:selected')[0].label + ' - ' + $('#kind option:selected')[0].label;
		 $('#map')[0].JMI.compute( parameters);
	}); 
});
function navigate(map, id) {
	 window.open( "http://www.allocine.fr/film/fichefilm_gen_cfilm=" + id + ".html", "_blank");
}
function focus(map, args) {
	var parameters = {};
	parameters["entityId"] = args[0];
	completeParameters( parameters);
	breadcrumbTitles.shortTitle = "Focus";
	breadcrumbTitles.longTitle = "Focus on: " + args[1];
	map.compute( parameters);
}
function center(map, args) {
	var parameters = {};
	parameters["attributeId"] = args[0];
	parameters["analysisProfile"] = "DiscoveryProfile";
	completeParameters( parameters);
	breadcrumbTitles.shortTitle = "Centered";
	breadcrumbTitles.longTitle = "Centered on: " + args[1];
	map.compute( parameters);
}
function same(map, args) {
	var parameters = {};
	parameters["attributeId"] = args[0];
	parameters["analysisProfile"] = "DiscoveryProfile";
	completeParameters( parameters);
	parameters.kind = 'film_same';
	parameters.filter = args[0];
	map.compute( parameters);
}
function completeParameters(parameters) {
	parameters.allowDomain = "*";
	//parameters.allocineserverurl = "http://localhost:8080/web-labs";
	parameters.allocineserverurl = "http://labs.just-map-it.com";
	parameters.map = "Allocine";
	parameters.kind = $('#kind option:selected')[0].value;
	parameters.filter = $('#filter option:selected')[0].value;
	parameters.jsessionid = '<%=session.getId()%>';
}
</script>
<jsp:include page="../ga.jsp" />
</head>
<body>
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
		<select id="filter">
		<option value="outthisweek" >Sorties de la semaine</option>
		<option value="nowshowing" >A l'écran</option>
		<option value="comingsoon" >Bientôt à l'affiche</option>
		<option value="top:week" >Meilleurs films de la semaine</option>
		<option value="top:month" >Meilleurs films du mois</option>
		<option value="top:alltimes" >Meilleurs films</option>
		<option value="worst" >Les pires films</option>
		</select>
		<select id="kind">
		<option value="film_gender" >Film / genre</option>
		<option value="film_tag" >Film / tag</option>
		<option value="film_casting" >Film / casting</option>
		</select>
		</td>
		<td align="right"><a title="Just Map It! Allocine" href="./"><img alt="Just Map It! Allocine" src="../images/justmapit_allocine.png" /></a></td>
	</tr>
</table>
<div id="message">&nbsp;</div>
<div id="breadcrumb">&nbsp;</div>
<div id="map"></div>
</body>
</html>
