<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! EDD</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="robots" content="noindex,nofollow" /> 
<style type="text/css" media="screen">
html, body {
	height: 100%;
}
#content {
	width: 100%;
	height: 82%;
	background-color: #FFFFFF;
}
img {
	border: 0;
}
object:focus {
	outline: none;
}
#flashContent {
	display: none;
}
</style>
<%Boolean persons = (request.getParameter("Persons") != null);
Boolean organizations = request.getParameter("Organizations") != null;
Boolean geos = request.getParameter("Geos") != null;
Boolean inverse = request.getParameter("Inverse") != null;
String entreprise = request.getParameter("entreprise");
if( entreprise == null) {
 entreprise = "";
 persons = true;
 }%>
<!-- Enable Browser History by replacing useBrowserHistory tokens with two hyphens -->
<!-- BEGIN Browser History required section -->
<link rel="stylesheet" type="text/css" href="../client/flex/history/history.css" />
<script type="text/javascript" src="../js/jquery-1.7.1.min.js"></script>
<script type="text/javascript" src="../client/flex/history/history.js"></script>
<!-- END Browser History required section -->
<script type="text/javascript" src="../client/flex/swfobject.js"></script>
<script type="text/javascript"> 
$(document).ready(function() {
<%if( entreprise.length() > 0) {%>
	 var swfVersionStr = "10.0.0";
	 var xiSwfUrlStr = "../client/flex/playerProductInstall.swf";
	 var flashvars = {};
	 JMIF_CompleteParameters( flashvars);
	 flashvars.analysisProfile = "GlobalProfile";
	 flashvars.entreprise = "<%=java.net.URLEncoder.encode(entreprise, "UTF-8")%>";
	 var params = {};
	 params.quality = "high";
	 params.bgcolor = "#FFFFFF";
	 params.allowscriptaccess = "always";
	 params.allowfullscreen = "true";
	 var attributes = {};
	 attributes.id = "jmi-polyspot";
	 attributes.name = "jmi-polyspot";
	 attributes.align = "middle";
	 swfobject.embedSWF(
	     "../jmi-client/swf/jmi-flex-1.0-SNAPSHOT.swf", "flashContent", 
	     "100%", "100%", 
	     swfVersionStr, xiSwfUrlStr, 
	     flashvars, params, attributes);
	swfobject.createCSS("#flashContent", "display:block;text-align:left;");
<%}%>	
});	 
</script>
<script type="text/javascript">
  function ready() {
	  var map = document.getElementById("jmi-polyspot");
  }
  function empty() {
	document.getElementById("message").innerHTML = "Sorry, the map is empty.";
  }
  function error( error) {
	document.getElementById("message").innerHTML = "Sorry, an error occured. Is this URL correct? <span class='hidden-message'>" + error + "</span>";
  }
  function JMIF_Navigate( url) {
 	 window.open( url, "_blank");
  }
  function JMIF_Focus( args)
  {
	var parameters = {};
	JMIF_CompleteParameters( parameters);
	parameters.entityId = args[0];
	parameters.entreprise = '<%=entreprise%>';
	document.getElementById("jmi-polyspot").compute( parameters);
	document.getElementById("message").innerHTML = "<i>Focus on named entity:</i> " + args[1];
  }
  function JMIF_Center( args)
  {
	var parameters = {};
	JMIF_CompleteParameters( parameters);
	parameters.attributeId = args[0];
	parameters.entreprise = '<%=entreprise%>';
	parameters.analysisProfile = "DiscoveryProfile";
	document.getElementById("jmi-polyspot").compute( parameters);
	document.getElementById("message").innerHTML = "<i>Centered on item:</i> " + args[1];
  }
function JMIF_CompleteParameters( parameters) {
	 parameters.allowDomain = "*";
	 //parameters.wpsserverurl = "http://localhost:8080/jmi-server";
     //parameters.eddserverurl = "http://localhost:8080/web-labs";
	 parameters.wpsserverurl = "http://server.just-map-it.com";
     parameters.eddserverurl = "http://labs.just-map-it.com";
	 parameters.wpsplanname = "EDD";
	 parameters.entities = '<%= (persons ? "Person" :"") + "," + (organizations ? "Organization" :"") + "," + (geos ? "Geo" :"")%>';
	 parameters.inverted = '<%=inverse%>';
	 parameters.jsessionid = '<%=session.getId()%>';
} 
</script>
<jsp:include page="../ga.jsp" />
</head>
<body>
<form id="main" method="get">
<table width="100%" border="0">
	<tr>
		<td rowspan="3"><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td class="label" ><b>Enter one or more URLs (comma separated):</b></td>
		<td rowspan="3"align="right"><a title="Just Map It! Syllabs" href="./"><img alt="Just Map It! Syllabs" src="../images/justmapit.png" /></a></td>
	</tr>
	<tr>
		<td>
			<select name="entreprise" id="entreprise">
			    <option value="mouilleron" <%=entreprise.equals("mouilleron") ? "selected":"" %>>boulangerie Mouilleron</option>
			    <option value="fournil" <%=entreprise.equals("fournil") ? "selected":"" %>>le fournil a rebais</option>
			    <option value="azentis" <%=entreprise.equals("azentis") ? "selected":"" %>>Azentis</option>
			    <option value="bnf" <%=entreprise.equals("bnf") ? "selected":"" %>>Bibliotheque Nationale de France</option>
			    <option value="flash" <%=entreprise.equals("flash") ? "selected":"" %>>Flash Copy</option>
			    <option value="fontenelle" <%=entreprise.equals("fontenelle") ? "selected":"" %>>Fontenelle Microcopie</option>
			    <option value="lys" <%=entreprise.equals("lys") ? "selected":"" %>>Lys call services</option>
			</select>
			<input type="submit" value="Just Map It!" />
		</td>
	</tr>
	<tr>
		<td>
			<input type="checkbox" name="Persons" <%=persons ? "checked" : ""%>/>Persons
			<input type="checkbox" name="Organizations" <%=organizations ? "checked" : ""%>/>Organizations
			<input type="checkbox" name="Geos" <%=geos ? "checked" : ""%>/>Geo
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="checkbox" name="Inverse" <%=inverse ? "checked" : ""%> onclick="document.getElementById('main').submit();"/>Inverse
		</td>
	</tr>
	<tr>
		<td nowrap colspan="3">
			<p id="message"></p>
		</td>
	</tr>
</table>
</form>
<div id="content">
<div id="flashContent">
<p>To view this page ensure that Adobe Flash Player version 10.0.0 or greater is installed.</p>
<script type="text/javascript"> 
	var pageHost = ((document.location.protocol == "https:") ? "https://" :	"http://"); 
	document.write("<a href='http://www.adobe.com/go/getflashplayer'><img src='" + pageHost + "www.adobe.com/images/shared/download_buttons/get_flash_player.gif' alt='Get Adobe Flash player' /></a>" ); 
</script></div>
<noscript><p>Either scripts and active content are not permitted to run Just Map It! Syllabs.</p></noscript>
</div>
</body>
</html>
