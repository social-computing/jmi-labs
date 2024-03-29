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
	if( oauth_token != null) {
	    oauth_token_secret = (String) session.getAttribute( "oauth_token_secret");
	    Map<String,String> result = null;
	    try {
	    	result = OAuthHelper.GetAccessToken(RestProvider.ACCESS_TOKEN_URL, RestProvider.API_KEY, RestProvider.API_SECRET, oauth_token_secret, oauth_token, oauth_verifier);
	    } catch(Exception e) {
	        oauth_token = null;
	    }
	    if( result != null) {
		    user_token = result.get( "oauth_token");
		    user_token_secret = result.get( "oauth_token_secret");
		    session.setAttribute( "user_token", user_token);
		    session.setAttribute( "user_token_secret", user_token_secret);
	    }
	}
	if( oauth_token == null) {
	    Map<String,String> result = OAuthHelper.GetRequestToken( RestProvider.REQUEST_TOKEN_URL, RestProvider.CALLBACK, RestProvider.API_KEY, RestProvider.API_SECRET);
	    oauth_token = result.get( "oauth_token");
	    oauth_token_secret = result.get( "oauth_token_secret");
	    oauth_verifier = result.get( "oauth_verifier");
	    session.setAttribute( "oauth_token", oauth_token);
	    session.setAttribute( "oauth_token_secret", oauth_token_secret);
	%>
		</head>
		    <meta http-equiv="refresh" content="0; url=<%=RestProvider.OAUTH_URL%>?oauth_token=<%=java.net.URLEncoder.encode(oauth_token,"UTF-8")%>&oauth_token_secret=<%=java.net.URLEncoder.encode(oauth_token_secret,"UTF-8")%>" />
			<title>Just Map It! Linkedin</title>
			<meta name="robots" content="index,follow" />
		</head>
		<body>
		<p><a href="<%=RestProvider.OAUTH_URL%>?oauth_token=<%=oauth_token %>&oauth_token_secret=<%=oauth_token_secret%>">Redirection</a></p>
		<!-- script> top.location.href='<%=RestProvider.OAUTH_URL%>?oauth_token=<%=java.net.URLEncoder.encode(oauth_token,"UTF-8")%>&oauth_token_secret=<%=java.net.URLEncoder.encode(oauth_token_secret,"UTF-8")%>'</script-->
		</body>
	<%}
}
if(user_token != null) {
%>
<head>
<title>Just Map It! Linkedin</title>
<meta name="robots" content="noindex,nofollow" /> 
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="target- densitydpi=device-dpi, width=device-width, user-scalable=no"/>
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
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
.jmi-breadcrumb li, .jmi-breadcrumb a {
	color: #006699;
	font-size: 14px;
	font-weight: bold;
}
</style>
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<script type="text/javascript">
var breadcrumbTitles = { shortTitle: 'Initial map', longTitle: 'All your contacts and their skills' };
function JMIF_breadcrumbTitlesFunc(event) {
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
	return breadcrumbTitles;
}
function getParams() {
	return {
		map: 'Linkedin',
    	linkedinserverurl: 'http://labs.just-map-it.com',
    	//linkedinserverurl: 'http://localhost:8080/web-labs',
    	authtoken: '<%=user_token%>',
    	authtokensecret: '<%=user_token_secret%>',
		jsessionid: '<%=session.getId()%>',
		inverted: 'false',
		kind: ''
    };
};
function GoMap() {
	var parameters = getParams();
	parameters.analysisProfile='GlobalProfile';
	var map = JMI.Map({
				parent: 'map', 
				clientUrl: '../jmi-client/', 
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
	breadcrumb = new JMI.extensions.Breadcrumb('breadcrumb',map,{'namingFunc':JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
	new JMI.extensions.Slideshow(map);
	map.compute( parameters);

	$('#kind').change(function(){
		var parameters = getParams();
		parameters.analysisProfile = "GlobalProfile";
		$('#map')[0].JMI.compute( parameters);
	});
};
function JMIF_Navigate(map, args) {
	window.open( args[0], "_blank");
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
		<td rowspan="2"><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
<script src="//platform.linkedin.com/in.js" type="text/javascript"></script>
<script type="IN/Share" data-url="http://labs.just-map-it.com/linkedin/" data-counter="right"></script>
		</td>
		<td>
<iframe src="//www.facebook.com/plugins/like.php?href=https%3A%2F%2Flabs.just-map-it.com%2Flinkedin%2F&amp;send=false&amp;layout=standard&amp;width=450&amp;show_faces=false&amp;action=like&amp;colorscheme=light&amp;font=arial&amp;height=35&amp;appId=136353756473765" scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:450px; height:35px;" allowTransparency="true"></iframe>		</td>
		<td rowspan="2" align="right"><a title="Just Map It! Linkedin" href="./"><img alt="Just Map It! Linkedin" src="../images/justmapit_linkedin.png" /></a></td>
	</tr>
	<tr>
		<td>
<a href="https://twitter.com/share" class="twitter-share-button" data-url="http://labs.just-map-it.com/linkedin/">Tweet</a>
<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
		</td>
		<td></td>
	</tr>
</table>
</form>
<div id="breadcrumb">&nbsp;</div>
<div id="map"></div>
</body>
<%}%>
</html>
