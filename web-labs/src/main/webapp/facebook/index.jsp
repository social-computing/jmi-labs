<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="com.socialcomputing.labs.facebook.FacebookRestProvider"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">	
<head>
<title>Just Map It! Facebook</title>
<meta name="viewport" content="target- densitydpi=device-dpi, width=device-width, user-scalable=no"/>
<%String code = request.getParameter("code");
String error = request.getParameter("error");
String oauth_token = FacebookRestProvider.GetProperty( request.getParameter("signed_request"), "oauth_token");
String user_id = FacebookRestProvider.GetProperty( request.getParameter("signed_request"), "user_id");
if( error != null) {%>
</head><body>
Sorry...
</body>
<%} else if( code == null && oauth_token == null && true) { %>
<meta name="robots" content="noindex,follow" />
<!--meta http-equiv="refresh" content="0; url=https://www.facebook.com/dialog/oauth?client_id=<%=FacebookRestProvider.CLIENT_ID%>&redirect_uri=<%=java.net.URLEncoder.encode("http://facebook.just-map-it.com/postinstall.jsp", "UTF-8")%>&scope=friends_likes,friends_groups,friends_activities,friends_events,publish_stream,user_photos" /-->
</head>
<body>
<script> top.location.href='https://www.facebook.com/dialog/oauth?client_id=<%=FacebookRestProvider.CLIENT_ID%>&redirect_uri=<%=java.net.URLEncoder.encode("http://facebook.just-map-it.com/postinstall.jsp", "UTF-8")%>&scope=friends_likes,friends_groups,friends_activities,friends_events,publish_stream,user_photos'</script>
</body>
<%} else {%>
<meta name="google" value="notranslate">         
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css" media="screen"> 
html, body	{ height:100%; }
body { margin:0; padding:0; overflow:auto; text-align:left; background-color: #FFFFFF; }   
#content {
	width: 810px;
	height: 80%;
	background-color: #FFFFFF;
}
#jmi-map {
	width: 100%;
	height: 100%;
	background-color: #FFFFFF;
}
img {
	border: 0;
}
#mode .ui-button-text { 
	font-family:Arial, Verdana, sans-serif;	
	font-size:11px;	
	color:#ffffff;
	background:#335595;
	border:1px solid #343434;
	width: 45px;
}
#mode .ui-button-text:hover {
	background:#eeeeee;
	color:#343434;
}
#mode .ui-state-active .ui-button-text { 
	color:#335595;
	background:#dfdfdf;
	border:1px solid #343434;
}
#mode .ui-state-active .ui-button-text:hover {
	background:#eeeeee;
	color:#335595;
}
#mode .ui-state-active {
	border: 1px solid #CCCCCC;
}
#upload .ui-button-text { 
	font-family:Arial, Verdana, sans-serif;	
	font-size:11px;	
	color:#343434;
	background:#ffffff;
	border:1px solid #343434;
}
#upload .ui-button-text:hover {
	background:#CCCCCC;
	color:#343434;
}
#tagl .ui-button-text { 
	font-family:Arial, Verdana, sans-serif;	
	font-size:10px;	
	color:#fff0000;
	background:#ffffff;
	border:none;
}
#tagl .ui-button-text:hover {
	background:#eeeeee;
	color:#343434;
}
#tagl .ui-state-active .ui-button-text { 
	color:#00ff00;
	background:#dfdfdf;
	border:none;
}
</style>
<link type="text/css" href="../css/jquery-ui-1.8.21.custom.css" rel="Stylesheet" />	
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui-1.8.21.custom.min.js"></script>
<script type="text/javascript" src="../js/jquery.base64.js"></script>
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript" src="./facebook.js"></script>
<script type="text/javascript">

$(document).ready( function() {
	var map = new JMI.facebook.Map('jmi-map');
	map.session = '<%=session.getId()%>';
	map.accessToken = '<%=code != null ? FacebookRestProvider.GetAccessToken(code) : oauth_token%>';
	map.fbuserid = '<%=user_id%>';
	map.mode = 'likes';
	map.draw(map.mode,{breadcrumb:'jmi-breadcrumb'});
});
$(function() {
	$( "#mode" ).buttonset();
	$( "#mode").change( function() { 
		$("#jmi-map")[0].JMI.facebook.mode = $("input[@name=mode]:checked").attr('id');
		$("#jmi-map")[0].JMI.facebook.compute();
	});
	$( "#upload, #tag").button();	
	$( "#upload").click( function() {
		$("#jmi-map")[0].JMI.facebook.uploadAsPhoto(true,$("#jmi-map")[0].JMI.facebook.mode);
	});
	$( "#tag").change( function() {
		var mode = $("input[@name=tag]:checked");
		alert(mode);
	});
});
</script>
</head>
<body>
<div id="toolbar">
	<div id="mode">
		<input type="radio" id="friends" name="mode" /><label for="friends">friends</label>
		<input type="radio" id="likes" name="mode" checked /><label for="likes">likes</label>
		<input type="radio" id="movies" name="mode" /><label for="movies">movies</label>
		<input type="radio" id="music" name="mode" /><label for="music">music</label>
		<input type="radio" id="books" name="mode" /><label for="books">books</label>
		<input type="radio" id="photos" name="mode" /><label for="photos">photos</label>
		<input type="radio" id="video" name="mode" /><label for="video">video</label>
		<input type="radio" id="events" name="mode" /><label for="events">events</label>
		<input type="radio" id="groups" name="mode" /><label for="groups">groups</label>
	</div>
	<button id="upload">Upload a photo</button>
	<!-- input type="checkbox" id="tag" name="tag"/><label id="tagl" for="tag">and don't tag friends</label-->
</div>
<div id="content">
	<div id="jmi-breadcrumb">&nbsp;</div>
	<div id="jmi-map">
</div>
</body>
<%}%>
</html>
