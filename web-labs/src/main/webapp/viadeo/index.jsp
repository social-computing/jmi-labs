<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="com.socialcomputing.labs.viadeo.ViadeoRestProvider"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
 <head>
<title>Just Map It! Viadeo</title>
<%String code = request.getParameter("code");
if( code == null) { %>
<title>Redirection</title>
<meta name="robots" content="noindex,follow" />
<!--meta http-equiv="refresh" content="0; url=https://secure.viadeo.com/oauth-provider/authorize2?response_type=code&display=popup&lang=en&client_id=<%=ViadeoRestProvider.CLIENT_ID%>&redirect_uri=<%=java.net.URLEncoder.encode( ViadeoRestProvider.APP_URL, "UTF-8")%>" /-->
</head>
<body>
<script> top.location.href='https://secure.viadeo.com/oauth-provider/authorize2?response_type=code&display=popup&lang=en&client_id=<%=ViadeoRestProvider.CLIENT_ID%>&redirect_uri=<%=java.net.URLEncoder.encode(ViadeoRestProvider.APP_URL, "UTF-8")%>'</script>
</body>
</html>
<%} else {
String authcode = ViadeoRestProvider.GetAccessToken(code);%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css" media="screen">
html, body {
	height: 100%;
}
#content {
	width: 100%;
	height: 90%;
	background-color: #FFFFFF;
}
img 
{
	border: 0;
}
object:focus {
	outline: none;
}
#flashContent {
	display: none;
}
</style>
<link rel="stylesheet" type="text/css" href="../client/flex/history/history.css" />
<script type="text/javascript" src="../js/jquery-1.7.1.min.js"></script>
<script type="text/javascript" src="../client/flex/history/history.js"></script>
<link rel="stylesheet" href="../css/main.css"/>

<script type="text/javascript"> 
  function empty() {
	Alert("Sorry, the map is empty");
  }
  function error( error) {
	Alert( "Sorry, an error occured." + error);
  }
  function navigate( url) {
 	 window.open( url, "_blank");
  }
  function focus( args) {
	var parameters = {};
	parameters["entityId"] = args[0];
	parameters["feed"] = args[2];
	completeParameters( parameters);
	document.getElementById("jmi-viadeo").compute( parameters);
  }
  function center( args) {
	var parameters = {};
	parameters["attributeId"] = args[0];
	parameters["analysisProfile"] = "DiscoveryProfile";
	completeParameters( parameters);
	document.getElementById("jmi-viadeo").compute( parameters);
  }
  function completeParameters( parameters) {
	 parameters.allowDomain = "*";
	 parameters.wpsserverurl = "http://localhost:8080/jmi-server";
     parameters.viadeoserverurl = "http://localhost:8080/web-labs";
	 //parameters.wpsserverurl = "http://server.just-map-it.com";
     //parameters.allocineserverurl = "http://labs.just-map-it.com";
	 parameters.wpsplanname = "Viadeo";
	 parameters.authcode = '<%=authcode%>';
	 parameters.kind = $('#kind option:selected')[0].value;
	 parameters.jsessionid = '<%=session.getId()%>';
  }
</script>
  
<script type="text/javascript" src="../client/flex/swfobject.js"></script>
<script type="text/javascript">
$(document).ready(function() {
   var swfVersionStr = "10.0.0";
   var xiSwfUrlStr = "../client/flex/playerProductInstall.swf";
   var flashvars = {};
   completeParameters( flashvars);
   flashvars.analysisProfile = "GlobalProfile";
   var params = {};
   params.quality = "high";
   params.bgcolor = "#FFFFFF";
   params.allowscriptaccess = "sameDomain";
   params.allowfullscreen = "true";
   var attributes = {};
   attributes.id = "jmi-viadeo";
   attributes.name = "jmi-viadeo";
   attributes.align = "middle";
   swfobject.embedSWF(
           "../client/flex/jmi-flex-1.0-SNAPSHOT.swf", "flashContent", 
           "100%", "100%", 
           swfVersionStr, xiSwfUrlStr, 
           flashvars, params, attributes);
   
	  $('#kind').change(function(){
			 var parameters = {};
			 completeParameters( parameters);
			 parameters.analysisProfile = "GlobalProfile";
			 $('#jmi-viadeo')[0].compute( parameters);
			});
});	 
</script>
</head>
<body>
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Viadeo" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
			<select id="kind">
			<option value="contacts" >Contacts</option>
			<option value="groups" >Groups</option>
			<option value="likes" >Likes</option>
			</select>
		</td>
		<td align="right"><a title="Just Map It! Viadeo" href="./"><img alt="Just Map It! Viadeo" src="../images/justmapit_viadeo.png" /></a></td>
	</tr>
</table>
		<div id="content">
        <div id="flashContent">
        	<p>
	        	To view this page ensure that Adobe Flash Player version 
				10.0.0 or greater is installed. 
			</p>
			<script type="text/javascript"> 
				var pageHost = ((document.location.protocol == "https:") ? "https://" :	"http://"); 
				document.write("<a href='http://www.adobe.com/go/getflashplayer'><img src='" 
								+ pageHost + "www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>" ); 
			</script> 
        </div>
	   	
       	<noscript>
           <a href="http://www.adobe.com/go/getflashplayer">
               <img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash Player" />
           </a>
	    </noscript>		
	    </div>
   </body>
</html>
<%} %>
