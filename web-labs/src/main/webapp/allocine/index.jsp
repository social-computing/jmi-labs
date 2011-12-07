<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Allocine</title>
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

<!-- Enable Browser History by replacing useBrowserHistory tokens with two hyphens -->
<!-- BEGIN Browser History required section -->
<link rel="stylesheet" type="text/css" href="../client/flex/history/history.css" />
<script type="text/javascript" src="../js/jquery-1.7.1.min.js"></script>
<script type="text/javascript" src="../client/flex/history/history.js"></script>
<!-- END Browser History required section -->
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
	 attributes.id = "jmi-allocine";
	 attributes.name = "jmi-allocine";
	 attributes.align = "middle";
	 swfobject.embedSWF(
	     "../client/flex/jmi-flex-1.0-SNAPSHOT.swf", "flashContent", 
	     "100%", "100%", 
	     swfVersionStr, xiSwfUrlStr, 
	     flashvars, params, attributes);
	swfobject.createCSS("#flashContent", "display:block;text-align:left;");
	
	  $('#kind').change(function(){
		 var parameters = {};
		 completeParameters( parameters);
		 parameters.analysisProfile = "GlobalProfile";
		 $('#jmi-allocine')[0].compute( parameters);
		});
	  $('#filter').change(function(){
		 var parameters = {};
		 completeParameters( parameters);
		 parameters.analysisProfile = "GlobalProfile";
		 $('#jmi-allocine')[0].compute( parameters);
		}); 
});	 
</script>
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
	document.getElementById("jmi-allocine").compute( parameters);
  }
  function center( args) {
	var parameters = {};
	parameters["attributeId"] = args[0];
	parameters["analysisProfile"] = "DiscoveryProfile";
	completeParameters( parameters);
	document.getElementById("jmi-allocine").compute( parameters);
  }
  function completeParameters( parameters) {
	 //parameters.wpsserverurl = "http://localhost:8080/jmi-server";
     //parameters.allocineserverurl = "http://localhost:8080/web-labs";
	 parameters.wpsserverurl = "http://server.just-map-it.com";
     parameters.allocineserverurl = "http://labs.just-map-it.com";
	 parameters.wpsplanname = "Allocine";
	 parameters.kind = $('#kind option:selected')[0].value;
	 parameters.filter = $('#filter option:selected')[0].value;
	 parameters.jsessionid = '<%=session.getId()%>';
  }
</script>
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
<div id="content">
<div id="flashContent">
<p>To view this page ensure that Adobe Flash Player version 10.0.0 or greater is installed.</p>
<script type="text/javascript"> 
	var pageHost = ((document.location.protocol == "https:") ? "https://" :	"http://"); 
	document.write("<a href='http://www.adobe.com/go/getflashplayer'><img src='" + pageHost + "www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>" ); 
</script></div>
<noscript><p>Either scripts and active content are not permitted to run Just Map It! Allocine.</p></noscript>
</div>
</body>
</html>
