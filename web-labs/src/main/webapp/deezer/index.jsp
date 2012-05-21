<!DOCTYPE html>
<%@page import="java.util.UUID"%>
<%@page import="com.socialcomputing.labs.deezer.services.DeezerRestProvider"%>
<%@page import="java.net.URLEncoder"%>
<html lang="fr">
	<head>
		<%
		String access_token = (String) session.getAttribute("access_token");
		
		String errorMsg = "";
		if(access_token == null) {
			String code = request.getParameter("code");
			if(code != null) {
				String state = (String) session.getAttribute("state");
				if(state != null && state.equals(request.getParameter("state"))) {
					access_token = DeezerRestProvider.getAccessToken(code, session);
				}
				else {
				    errorMsg = "Invalid state received : CSRF";
				}
			}
			else {
			    //CSRF protection @see http://fr.wikipedia.org/wiki/Cross-site_request_forgery : generating a unique identifier 
				session.setAttribute("state", UUID.randomUUID().toString());
				response.sendRedirect(DeezerRestProvider.AUTHORIZE_ENDPOINT + "?app_id=" + DeezerRestProvider.APP_ID  
				+ "&redirect_uri=" + URLEncoder.encode(DeezerRestProvider.CALLBACK_URL, "UTF-8") 
				+ "&perms=" + DeezerRestProvider.APP_PERMS
				+ "&state=" + session.getAttribute("state"));
			}
		}
		%>
	    <title>Just Map It! Deezer</title>
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
		<%
		  // Reading query parameters 
		  Boolean inverse = request.getParameter("Inverse") != null;
	      String maptype = request.getParameter("maptype");
	      String query = request.getParameter("query");
	      
	      if(maptype == null) maptype = "";
	    %>
		<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
		<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
		<script type="text/javascript">
		var breadcrumbTitles = { shortTitle: 'Initial query', longTitle: '<%=maptype%>s map' };
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
				map: 'Deezer',
				jsessionid: '<%=session.getId()%>',
				query: '<%=query%>',
				access_token: '<%=access_token%>',
				maptype: '<%=maptype%>'
		    };
		    return p;
		};
		function GoMap() {
			var parameters = getParams();
			parameters.analysisProfile='GlobalProfile';
			if( parameters.maptype.length > 0) {
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
			<table style="width:100%">
				<tr>
					<td><a title="Just Map It! Deezer" href="./"><img alt="Just Map It! Lecko" src="../images/logo-deezer.png" /></a></td>
					<td>
					    <input type="hidden" name="access_token" value="<%=access_token%>" />
					    <fieldset>
                          <legend>Type de carte:</legend>
                          <label><input type="radio" name="maptype" value="artist" <%=("".equals(maptype) || "artist".equalsIgnoreCase(maptype)) ? "checked=\"checked\"" : ""%> />Artistes</label>
                          <label><input type="radio" name="maptype" value="album" <%=("album".equalsIgnoreCase(maptype)) ? "checked=\"checked\"" : ""%>  />Albums</label>
                        </fieldset>
					</td>
					<td><input type="submit" value="Just Map It!" /></td>
					<td align="right"><a title="Just Map It!" href="http://www.social-computing.com/offre/cartographie-just-map-it/" target="_blank"><img alt="Just Map It!" src="../images/justmapit.png" /></a></td>
				</tr>
			</table>
		</form>
		<div id="breadcrumb">&nbsp;</div>
		<div id="map"></div>
	</body>
</html>
