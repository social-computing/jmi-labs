<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.util.Map"%>
<%@page import="com.socialcomputing.wps.server.planDictionnary.connectors.utils.OAuthHelper"%>
<%@page import="com.socialcomputing.labs.linkedin.RestProvider"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<%
String user_token = (String) session.getAttribute( "user_token");
String user_token_secret = (String) session.getAttribute( "user_token_secret");
String oauth_verifier = null;
if(user_token == null) {
    String oauth_token = request.getParameter("oauth_token");
    String oauth_token_secret = request.getParameter("oauth_token_secret");
    oauth_verifier = request.getParameter("oauth_verifier");
	if( oauth_token == null) {
	    Map<String,String> result = OAuthHelper.GetRequestToken( RestProvider.REQUEST_TOKEN_URL, RestProvider.CALLBACK, RestProvider.API_KEY, RestProvider.API_SECRET);
	    oauth_token = result.get( "oauth_token");
	    oauth_token_secret = result.get( "oauth_token_secret");
	    oauth_verifier = result.get( "oauth_verifier");
	    session.setAttribute( "oauth_token", oauth_token);
	    session.setAttribute( "oauth_token_secret", oauth_token_secret);
	%>
		</head>
		    <meta http-equiv="refresh" content="0; url=<%=RestProvider.OAUTH_URL%>?oauth_token=<%=oauth_token%>&oauth_token_secret=<%=oauth_token_secret%>" />
			<title>Redirection</title>
			<meta name="robots" content="noindex,follow" />
		</head>
		<body>
		<p><a href="<%=RestProvider.OAUTH_URL%>?oauth_token=<%=oauth_token %>&oauth_token_secret=<%=oauth_token_secret%>">Redirection</a></p>
		</body>
	<%
	} else {
	    oauth_token_secret = (String) session.getAttribute( "oauth_token_secret");
	    Map<String,String> result = OAuthHelper.GetAccessToken(RestProvider.ACCESS_TOKEN_URL, RestProvider.API_KEY, RestProvider.API_SECRET, oauth_token_secret, oauth_token, oauth_verifier);
	    user_token = result.get( "oauth_token");
	    user_token_secret = result.get( "oauth_token_secret");
	    session.setAttribute( "user_token", user_token);
	    session.setAttribute( "user_token_secret", user_token_secret);
	}
}
if(user_token != null) {
%>
<head>
<title>Just Map It! Linkedin</title>
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
<link rel="stylesheet" type="text/css" href="../jmi-client/jmi-client.css" />
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript" src="../js/jquery-1.7.1.min.js"></script>
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
		map: 'Linkedin',
    	//linkedinserverurl: 'http://labs.just-map-it.com',
    	linkedinserverurl: 'http://localhost:8080/web-labs',
    	authtoken: '<%=user_token%>',
    	authtokensecret: '<%=user_token_secret%>',
		jsessionid: '<%=session.getId()%>',
		inverted: <%=inverse%>,
		kind: $('#kind option:selected')[0].value
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
	breadcrumb = new JMI.extensions.Breadcrumb('breadcrumb',map,{'namingFunc':JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
	map.compute( parameters);

	$('#kind').change(function(){
		var parameters = getParams();
		parameters.analysisProfile = "GlobalProfile";
		$('#map')[0].JMI.compute( parameters);
	});
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
<jsp:include page="../ga.jsp" />
</head>
<body onload="GoMap()">
<form id="main" method="get">
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
			<select id="kind">
				<option value="contacts" >Contacts</option>
				<option value="groups" >Groups</option>
				<option value="likes" >Likes</option>
			</select>
			<input type="checkbox" name="Inverse" <%=inverse ? "checked" : ""%> onclick="document.getElementById('main').submit();"/>Inverse
		</td>
		<td align="right"><a title="Just Map It! Linkedin" href="./"><img alt="Just Map It! Adisseo" src="../images/justmapit.png" /></a></td>
	</tr>
</table>
</form>
<div id="breadcrumb">&nbsp;</div>
<div id="map"></div>
</body>
<%}%>
</html>
