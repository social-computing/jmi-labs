<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Adisseo</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
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
<link rel="stylesheet" type="text/css" href="../jmi-client/jmi-client.css" />
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript">
function getParams() {
	var p = {
		map: 'dimeloserverurl',
    	dimeloserverurl: 'https://feeds.just-map-it.com',
		jsessionid: '<%=session.getId()%>'
    };
    return p;
};
function GoMap() {
	var parameters = getParams();
	parameters.analysisProfile='GlobalProfile';
	var map = JMI.Map({
				parent: 'map', 
				swf: '../jmi-client/jmi-flex-1.0-SNAPSHOT.swf', 
				//server: 'http://server.just-map-it.com', 
				server: 'http://localhost:8080/jmi-server/', 
				//client: JMI.Map.SWF,
				parameters: parameters
			});
	map.addEventListener(JMI.Map.event.READY, function(event) {
	} );
	map.addEventListener(JMI.Map.event.ACTION, function(event) {
		window[event.fn](event.map, event.args);
	} );
	map.addEventListener(JMI.Map.event.EMPTY, function(event) {
		document.getElementById("status").innerHTML = 'Map is empty.';
	} );
	map.addEventListener(JMI.Map.event.ERROR, function(event) {
		document.getElementById("status").innerHTML = event.message;
	} );
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
</script>
<jsp:include page="../ga.jsp" />
</head>
<body onload="GoMap()">
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
		<select id="filter">
		<option value="outthisweek" >Sorties de la semaine</option>
		<option value="nowshowing" >A l'écran</option>
		<option value="comingsoon" >Bientôt Ã  l'affiche</option>
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
<div id="status">&nbsp;</div>
<div id="map"></div>
</body>
</html>
