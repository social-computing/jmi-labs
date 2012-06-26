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
<link type="text/css" href="../css/jquery-ui-1.8.21.custom.css" rel="Stylesheet" />	
<link type="text/css" href="../jmi-client/css/jmi-client.css" rel="stylesheet" />
<link type="text/css" href="./facebook.css" rel="Stylesheet" />	
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui-1.8.21.custom.min.js"></script>
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
		var map = $("#jmi-map").get(0).JMI.facebook;
		map.mode = $("input[@name=mode]:checked").attr('id');
	    map.breadcrumbTitles.shortTitle = map.mode;
	    map.breadcrumbTitles.longTitle = 'friends according ' + map.mode;
		$("#jmi-map")[0].JMI.facebook.compute();
	});
	$( "#upload, #tag").button();	
	$( "#upload").click( function() {
		$("#jmi-map")[0].JMI.facebook.uploadAsPhoto($( "#tag").get(0).checked,$("#jmi-map").get(0).JMI.facebook.mode);
	});
	$( "#tag").change( function() {
		$( "#tag").button( "option", "label", this.checked ? 'with friends tags' : 'without friends tags');
		//$( "#tag").button( "refresh" );
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
	<button id="upload">snapshot in album</button>
	<input type="checkbox" id="tag" name="tag"/><label id="tagl" for="tag">whithout friends tags</label>
</div>
<div id="content">
	<div id="jmi-breadcrumb">&nbsp;</div>
	<div id="jmi-map"></div>
</div>
<div id="slideshow" style="visibility:hidden;">
	<ul>
		<li><img src="http://labs.just-map-it.com/images/justmapit_facebook.png"></img><p>Just Map It! Facebook displays the map of your Facebook friends</p></li>
		<li><img src="http://labs.just-map-it.com/images/justmapit_facebook.png"></img><p>Your friends are displayed according the pages, the books, the music... they like.</p><p>You can focus on a friend.</p><p>It's a new way to discover your entourage.</p></li>
		<li><img src="http://labs.just-map-it.com/images/justmapit_facebook.png"></img><p>You can save the map in your photo album.</p><p>You can automatically tag your friends in the map photo.</p><p>It's a new way to share your entourage.</p></li>
	</ul>
</div>
</body>
<%}%>
</html>
