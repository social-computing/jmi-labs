<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Adisseo</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="robots" content="noindex,nofollow" /> 
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
if( query == null) query = "";
String kind = request.getParameter("kind");
if( kind == null) kind = "search";%>
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="../js/jquery-1.7.1.min.js"></script>
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript">
var breadcrumbTitles = { };
function JMIF_breadcrumbTitlesFunc(event) {
	if( event.type === JMI.Map.event.EMPTY) {
		return {shortTitle: 'Sorry, the map is empty.', longTitle: 'Sorry, the map is empty.'};
	}
	if( event.type === JMI.Map.event.ERROR) {
		if(event.track) {
			return {shortTitle: 'Sorry, an error occured. If you want to be informed about it, please <a title="Fill the form" href="http://www.just-map-it.com/p/report.html?track='+ event.track +'" target="_blank">fill the form</a>', longTitle: 'Sorry, an error occured. Error: ' + event.message};
		}
		else {
			return {shortTitle: 'Sorry, an error occured: ' + event.message, longTitle: 'Sorry, an error occured. Error: ' + event.message};
		}
	}
	return breadcrumbTitles;
};
function getParams() {
	return {
		map: 'Adisseo',
    	dimeloserverurl: 'http://labs.just-map-it.com',
    	//dimeloserverurl: 'http://localhost:8080/web-labs',
		jsessionid: '<%=session.getId()%>',
		inverted: <%=inverse%>,
		kind: '<%=kind%>',
		param: '<%=query.replace("'", "\\'")%>'
    };
};
function GoMap() {
	var parameters = getParams();
	if( parameters.param.length > 0) {
		if( parameters.kind === 'search') {
			parameters.analysisProfile='GlobalProfile';
			breadcrumbTitles.shortTitle= 'Initial query';
			breadcrumbTitles.longTitle= 'Query: <%=query.replace("'", "\\'")%>';
		}
		else if( parameters.kind === 'feedback') {
			var data = {};
			data.feedback_url = 'https://open-adisseo.api.ideas.dimelo.com';
			data.access_token = '5eba7151e753e9fede13177a3098832f';
			$.getJSON( parameters.dimeloserverurl + "/rest/dimelo/feedbacks/<%=query%>.json", data, function( feedback) {
				breadcrumbTitles.shortTitle = parameters.inverted ? "Focus" : "Centered";
				breadcrumbTitles.longTitle = breadcrumbTitles.shortTitle + " on: " + feedback.title;
			});
			if( parameters.inverted) {
				parameters.entityId = '<%=query%>';
			}
			else {
				parameters.attributeId = '<%=query%>';
				parameters.analysisProfile = "DiscoveryProfile";
			}
		}
		else if( parameters.kind === 'user') {
			var data = {};
			data.user_url = 'https://open-adisseo.api.users.dimelo.com';
			data.access_token = '5eba7151e753e9fede13177a3098832f';
			$.getJSON( parameters.dimeloserverurl + "/rest/dimelo/users/<%=query%>.json", data, function( user) {
				breadcrumbTitles.shortTitle = parameters.inverted ? "Focus" : "Centered";
				breadcrumbTitles.longTitle = breadcrumbTitles.shortTitle + " on: " + user.firstname + ' ' + user.lastname;
			});
			if( parameters.inverted) {
				parameters.attributeId = '<%=query%>';
				parameters.analysisProfile = "DiscoveryProfile";
			}
			else {
				parameters.entityId = '<%=query%>';
			}
		};
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
		new JMI.extensions.Breadcrumb('breadcrumb',map,{'namingFunc':JMIF_breadcrumbTitlesFunc,'thumbnail':{}});
		new JMI.extensions.Slideshow(map);
		map.compute( parameters);
	};
};
function JMIF_Navigate(map, url) {
	window.open( url, "_blank");
};
function JMIF_Focus(map, args) {
	var parameters = getParams();
	parameters.entityId = args[0];
	breadcrumbTitles.shortTitle = "Focus";
	breadcrumbTitles.longTitle = "Focus on: " + args[1];
	map.compute( parameters);
};
function JMIF_Center(map, args) {
	var parameters = getParams();
	parameters.attributeId = args[0];
	parameters.analysisProfile = "DiscoveryProfile";
	breadcrumbTitles.shortTitle = "Centered";
	breadcrumbTitles.longTitle = "Centered on: " + args[1];
	map.compute( parameters);
};
function JMIF_goFeedback(id) {
	document.getElementById('query').value = id;
	document.getElementById('kind').value = 'feedback';
	document.getElementById('inverse').checked = false;
	document.getElementById('main').submit();
	return false;
};
function JMIF_goUser(id) {
	document.getElementById('query').value = id;
	document.getElementById('kind').value = 'user';
	document.getElementById('inverse').checked = true;
	document.getElementById('main').submit();
	return false;
};
</script>
<jsp:include page="../ga.jsp" />
</head>
<body onload="GoMap()">
<form id="main" method="get">
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
			<select id="kind" name="kind">
				<option value="search" <%=(kind.equalsIgnoreCase("search") ? "selected" : "")%>>Search ideas</option>
				<option value="feedback" <%=(kind.equalsIgnoreCase("feedback") ? "selected" : "")%>>Idea</option>
				<option value="user" <%=(kind.equalsIgnoreCase("user") ? "selected" : "")%>>User</option>
			</select>
			<input type="text" id="query" name="query" title="Query" size="80" value="<%=query.replace("\"", "&quot;")%>" />
			<input type="submit" value="Just Map It!" />
			<input type="checkbox" id="inverse" name="Inverse" <%=inverse ? "checked" : ""%> onclick="document.getElementById('main').submit();"/>Inverse
		</td>
		<td align="right"><a title="Just Map It! Adisseo" href="./"><img alt="Just Map It! Adisseo" src="../images/justmapit.png" /></a></td>
	</tr>
</table>
</form>
<div id="breadcrumb">&nbsp;</div>
<div id="map">
<%if(query.length() == 0) {%>
    Start from idea <a href="" onclick="return JMIF_goFeedback('89448');">Promote A-dry product through academic use</a><br/>
    Start from idea <a href="" onclick="return JMIF_goFeedback('81501');">Use emulsion factors to improve fat digestibility in poultry and/or in pigs</a><br/>
    Start from idea <a href="" onclick="return JMIF_goFeedback('84017');">Rovabio and Rhodimet in Liquid Feeding in Swine</a><br/>
    <br/><br/>
    Start from user <a href="" onclick="return JMIF_goUser('166014');">Stefan Bohne</a><br/>
    Start from user <a href="" onclick="return JMIF_goUser('160328');">Pierre-André Geraert</a><br/>
    Start from user <a href="" onclick="return JMIF_goUser('165934');">Pierre-André Geraert</a><br/>
<%}%>
</div>
</body>
</html>
