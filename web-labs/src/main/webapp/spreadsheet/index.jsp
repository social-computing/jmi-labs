<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Spreadsheet</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="robots" content="index,follow" /> 
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
<%Boolean inverse = request.getParameter("Inverse") != null;
String sheetUrl = request.getParameter("sheetUrl");
if( sheetUrl == null) {
    sheetUrl = "";
 }%>
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript">
var breadcrumbTitles = { shortTitle: 'Initial sheetUrl', longTitle: 'sheetUrl: <%=sheetUrl.replace("'", "\\'")%>' };
function JMIF_breadcrumbTitlesFunc(event) {
	if( event.type === JMI.Map.event.EMPTY) {
		return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
	}
	if( event.type === JMI.Map.event.ERROR) {
		return {shortTitle: 'Sorry, an error occured.' + event.message, longTitle: 'Sorry, an error occured. Error: ' + event.message};
	}
	return breadcrumbTitles;
};
function getParams() {
	return {
		map: 'SpreadSheet',
    	//spreadsheetserverurl: 'http://labs.just-map-it.com',
    	spreadsheetserverurl: 'http://localhost:8080/web-labs',
		jsessionid: '<%=session.getId()%>',
		inverted: <%=inverse%>,
		sheetUrl: '<%=sheetUrl%>',
		data:'{"entities":[{"id":"1","name":"entity1","attributes":["1"]}],"attributes":[{"id":"2","name":"attribute1"}]}'
    };
};
function GoMap() {
	var parameters = getParams();
	parameters.analysisProfile='GlobalProfile';
	if( parameters.sheetUrl.length > 0) {
		var map = JMI.Map({
					parent: 'map', 
					clientUrl: '../jmi-client/', 
					//server: 'http://server.just-map-it.com'
					server: 'http://localhost:8080/jmi-server/'
				});
		map.addEventListener(JMI.Map.event.READY, function(event) {
		} );
		map.addEventListener(JMI.Map.event.ACTION, function(event) {
			window[event.fn](event.map, event.args);
		} );
		new JMI.extensions.Breadcrumb('breadcrumb',map,{'namingFunc':JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
		new JMI.extensions.Slideshow(map);
		map.compute( parameters);
	};
};
function JMIF_Navigate(map, url) {
	window.open( url, "_blank");
}
function JMIF_Focus(map, args) {
	var parameters = getParams();
	parameters.entityId = args[0];
	map.compute( parameters);
	breadcrumbTitles.shortTitle = "Focus";
	breadcrumbTitles.longTitle = "Focus on named entity: " + args[1];
}
function JMIF_Center(map, args) {
	var parameters = getParams();
	parameters.attributeId = args[0];
	parameters.analysisProfile = "DiscoveryProfile";
	map.compute( parameters);
	breadcrumbTitles.shortTitle = "Centered";
	breadcrumbTitles.longTitle = "Centered on item: " + args[1];
}
</script>
<jsp:include page="../ga.jsp" />
</head>
<body onload="GoMap()">
<form id="main" method="get">
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
			<input type="text" name="sheetUrl" title="sheetUrl" size="80" value="<%=sheetUrl%>" />
			<input type="submit" value="Just Map It!" />
			<input type="checkbox" name="Inverse" <%=inverse ? "checked" : ""%> onclick="document.getElementById('main').submit();"/>Inverse
		</td>
		<td align="right"><a title="Just Map It! Spreadsheet" href="./"><img alt="Just Map It! Adisseo" src="../images/justmapit.png" /></a></td>
	</tr>
</table>
</form>
<div id="breadcrumb">&nbsp;</div>
<div id="map"></div>
</body>
</html>
