<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.net.URLEncoder"%>
<%@page import="com.socialcomputing.labs.bluekiwi.services.BluekiwiRestProvider"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<%
	String token = (String) session.getAttribute("oauth_token");
if(token == null) {
	token = request.getParameter("access_token");
	if(token == null) {
		String code = request.getParameter("code");
		if(code != null) {
	token = BluekiwiRestProvider.getAccessToken(code, session);
		}
		else {
	response.sendRedirect(BluekiwiRestProvider.AUTHORIZE_ENDPOINT + "?client_id=" + BluekiwiRestProvider.CLIENT_ID + "&response_type=code&redirect_uri=" + URLEncoder.encode(BluekiwiRestProvider.CALLBACK_URL, "UTF-8"));
		}
	}
}
%>

<title>Just Map It! Lecko</title>
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
<%Boolean inverse = request.getParameter("Inverse") != null;
String query = request.getParameter("query");
if( query == null) {
    query = "";
 }%>
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript">
var breadcrumbTitles = { shortTitle: 'Initial query', longTitle: 'Query: <%=query%>' };
function JMIF_breadcrumbTitlesFunc(event) {
	if( event.type === JMI.Map.event.EMPTY) {
		return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
	}
	if( event.type === JMI.Map.event.ERROR) {
		return {shortTitle: 'Sorry, an error occured.', longTitle: 'Sorry, an error occured. Error: ' + event.message};
	}
	return breadcrumbTitles;
}
function getParams() {
	var p = {
		map: 'BlueKiwi',
		jsessionid: '<%=session.getId()%>',
		query: '<%=query%>',
		token: '<%=token%>'
    };
    return p;
};
function GoMap() {
	var parameters = getParams();
	parameters.analysisProfile='GlobalProfile';
	if( parameters.query.length > 0) {
		var map = JMI.Map({
					parent: 'map', 
					clientUrl: '../jmi-client/', 
					server: 'http://server.just-map-it.com' 
					//server: 'http://localhost:8080/jmi-server/'
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
		breadcrumb = new JMI.extensions.Breadcrumb('breadcrumb',map,{'namingFunc':JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
		map.compute( parameters);
	};
};
function JMIF_Navigate(map, url) {
	window.open( url, "_blank");
}
function JMIF_Focus(map, args) {
	var parameters = getParams();
	parameters.entityId = args[0];
	breadcrumbTitles.shortTitle = "Focus";
	breadcrumbTitles.longTitle = "Focus on: " + args[1];
	map.compute( parameters);
}
function JMIF_Center(map, args) {
	var parameters = getParams();
	parameters.attributeId = args[0];
	parameters.analysisProfile = "DiscoveryProfile";
	breadcrumbTitles.shortTitle = "Centered";
	breadcrumbTitles.longTitle = "Centered on: " + args[1];
	map.compute( parameters);
}
</script>
</head>
<body onload="GoMap()">
<form id="main" method="get">
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Lecko" href="./"><img alt="Just Map It! Lecko" src="../images/logo_lecko.gif" /></a></td>
		<td>
		    <input type="hidden" name="access_token" value="<%=token%>" />
			<input type="text" name="query" title="Query" size="80" value="<%=query%>" />
			<input type="submit" value="Just Map It!" />
		</td>
		<td align="right"><a title="Just Map It!" href="http://www.social-computing.com/offre/cartographie-just-map-it/" target="_blank"><img alt="Just Map It!" src="../images/justmapit.png" /></a></td>
	</tr>
</table>
</form>
<div id="breadcrumb">&nbsp;</div>
<div id="map"></div>
</body>
</html>
