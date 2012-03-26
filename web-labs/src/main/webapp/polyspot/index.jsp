<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! PolySpot</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="target- densitydpi=device-dpi, width=device-width, user-scalable=no"/>
<style type="text/css" media="screen">
html, body {
	height: 100%;
}
#map {
	width: 100%;
	height: 75%;
	background-color: #FFFFFF;
}
img {
	border: 0;
}
</style>
<%Boolean contacts = (request.getParameter("Contacts") != null);
Boolean news = request.getParameter("News") != null;
Boolean wikipedia = request.getParameter("Wikipedia") != null;
Boolean inverse = request.getParameter("Inverse") != null;
String field = request.getParameter("field");
if( field == null) field = "";
String query = request.getParameter("query");
if( query == null) {
 query = "";
 news = true;
 }%>
<link rel="stylesheet" type="text/css" href="../jmi-client/jmi-client.css" />
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript">
var breadcrumb, t1 = 'Initial query', t2 = 'Query: <%=query%>';
function breadcrumbTitles() {
	return { 'shortTitle': t1, 'longTitle': t2};
}
function GoMap() {
	var parameters = {};
	JMIF_CompleteParameters( parameters);
	parameters.analysisProfile = "GlobalProfile";
	parameters.query = "<%=java.net.URLEncoder.encode(query, "UTF-8")%>";
	parameters.field = "<%=java.net.URLEncoder.encode(field, "UTF-8")%>";
	if( parameters.query.length > 0) {
		var map = JMI.Map({
					parent: 'map', 
					swf: '../jmi-client/jmi-flex-1.0-SNAPSHOT.swf', 
					server: 'http://server.just-map-it.com', 
					//server: 'http://localhost:8080/jmi-server/', 
					//client: JMI.Map.SWF
				});
		map.addEventListener(JMI.Map.event.READY, function(event) {
			//document.getElementById("message").innerHTML = breadcrumb.cuurent().longTitle;
		} );
		map.addEventListener(JMI.Map.event.ACTION, function(event) {
			window[event.fn](event.map, event.args);
		} );
		map.addEventListener(JMI.Map.event.EMPTY, function(event) {
		} );
		map.addEventListener(JMI.Map.event.ERROR, function(event) {
		} );
		breadcrumb = new JMI.extensions.Breadcrumb('breadcrumb',map,{'namingFunc':breadcrumbTitles,'thumbnail':{}});
		map.compute( parameters);
	}
};
  function JMIF_Navigate( url) {
 	 window.open( url, "_blank");
  }
  function JMIF_Focus(map,args)
  {
	var parameters = {};
	JMIF_CompleteParameters( parameters);
	parameters.entityId = args[0];
	parameters.query = map.getProperty("$query");
	parameters.field = map.getProperty("$field");
	t1 = "Focus";
	t2 = "Focus on named entity: " + args[1];
	map.compute( parameters);
  }
  function JMIF_Center(map,args)
  {
	var parameters = {};
	JMIF_CompleteParameters( parameters);
	parameters.attributeId = args[0];
	parameters.query = map.getProperty("$query");
	parameters.field = map.getProperty("$field");
	parameters.analysisProfile = "DiscoveryProfile";
	t1 = "Centered";
	t2 = "Centered on item: " + args[1];
	map.compute( parameters);
  }
function JMIF_CompleteParameters( parameters) {
	 parameters.allowDomain = "*";
     //parameters.polyspotserverurl = "http://localhost:8080/web-labs";
     parameters.polyspotserverurl = "http://labs.just-map-it.com";
	 parameters.map = "PolySpot";
	 parameters.sources = '<%= (contacts ? "1013" :"") + "," + (news ? "1014" :"") + "," + (wikipedia ? "1015" :"")%>';
	 parameters.inverted = '<%=inverse%>';
	 parameters.jsessionid = '<%=session.getId()%>';
} 
</script>
<jsp:include page="../ga.jsp" />
</head>
<body onload="GoMap()">
<form id="main" method="get">
<table width="100%" border="0">
	<tr>
		<td rowspan="3"><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td class="label" ><b>Enter query:</b></td>
		<td rowspan="3"align="right"><a title="Just Map It! Polyspot" href="./"><img alt="Just Map It! Syllabs" src="../images/justmapit.png" /></a></td>
	</tr>
	<tr>
		<td>
			<input type="text" name="query" title="URLs" size="80" value="<%=query != null ? query : "" %>" />
			<input type="submit" value="Just Map It!" />
		</td>
	</tr>
	<tr>
		<td>
			<!--input type="checkbox" name="Contacts" <%=contacts ? "checked" : ""%>/>Contacts-->
			<input type="checkbox" name="News" <%=news ? "checked" : ""%>/>News
			<input type="checkbox" name="Wikipedia" <%=wikipedia ? "checked" : ""%>/>Wikipedia
			<select name="field">
			  <option value="_ngrams" <%=field.equalsIgnoreCase("_ngrams") ? "selected" : ""%>>ngrams</option>
			  <option value="semantic-theme-label" <%=field.equalsIgnoreCase("semantic-theme-label") ? "selected" : ""%>>semantic theme</option>
			  <option value="person-label" <%=field.equalsIgnoreCase("person-label") ? "selected" : ""%>>person label</option>
			  <option value="organization-organisation-label" <%=field.equalsIgnoreCase("organization-organisation-label") ? "selected" : ""%>>organisation label</option>
			</select>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="checkbox" name="Inverse" <%=inverse ? "checked" : ""%> onclick="document.getElementById('main').submit();"/>Inverse
		</td>
	</tr>
	<tr>
		<td nowrap colspan="3">
		</td>
	</tr>
</table>
</form>
<div id="breadcrumb">&nbsp;</div>
<div id="map"></div>
</body>
</html>
