<!DOCTYPE html>
<%@page import="java.util.UUID"%>
<%@page import="com.socialcomputing.labs.deezer.services.DeezerRestProvider"%>
<%@page import="java.net.URLEncoder"%>
<html lang="fr">
	<head>
		<%
		DeezerRestProvider.checkExpirationDate(session);
		String access_token = (String) session.getAttribute("access_token");
		
		String errorMsg = null;
		if(access_token == null) {
			String code = request.getParameter("code");
			if(code != null) {
				if(DeezerRestProvider.isStateValid(session, request.getParameter("state"))) {
					access_token = DeezerRestProvider.getAccessToken(code, session);
				}
				else {
				    errorMsg = "Invalid state received from deezer api. You might be a victim of a CSRF attack.";
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
		var mapType = '<%=maptype%>';
		if(mapType == "relArtist") mapType = "artist";
		var breadcrumbTitles = { shortTitle: 'Initial query', longTitle: mapType + 's map' };
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
				new JMI.extensions.Slideshow(map);
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
		
		function JMIF_AddFavorites(map, args) {
			//var parameters = getParams();
		    DZ.getLoginStatus(function(response) {
		        if (response.authResponse) {
		        	var maptype = args[1];
		        	if(maptype == "relArtist") maptype = "artist";
		        	var userID       = response.userID,
                        id           = args[0],
                        service_uri  = "/user/" +  userID + "/" + maptype + "s";
                        query_params = { 'access_token': response.authResponse.accessToken},
		                attribute = map.attributes.match(new RegExp('\\b' + id + '\\b'), ['ID']),
		                action = attribute[0].INFAVLIST ? 'delete' : 'post';

                        query_params[maptype + '_id'] = args[0];
	
		        	console.log("calling deezer api %s service with %s request method and %s parameters", service_uri, action, query_params);
		        	DZ.api(service_uri,
		        		   action,
		        		   query_params,
		        		   function(response) {
		        		       var msg = "this " + maptype + " ";
		        			   switch(response) {
		        			   case true:
		        				   if(action == "post") {
		        					   msg += "was sucessfully added to your favorite list";
		        				   } else {
		        					   msg += "was sucessfully removed from your favorite list";
		        				   }
		        				   console.debug("Attribute infavlist value before setProperty call : %s", attribute[0].INFAVLIST);
		        				   attribute[0].setProperty('INFAVLIST', !attribute[0].INFAVLIST);
		        				   console.debug("Attribute infavlist value after setProperty call : %s", attribute[0].INFAVLIST);
		        				   alert(msg);
		        				   break;
		        			   case false:
		        				   if(action == "post") {
                                       msg += "is already in your favorite list";
                                   } else {
                                       msg += "is not in your favorite list";
                                   }
		        				   alert(msg);
		        				   break;
		        			   default:
		        				   console.debug(response);
		        			       var err_msg = "sorry an error occured";
		        			       
		        			       if(response.error) err_msg += ': ' + response.error.type + ', ' + response.error.message;
		        				   alert(err_msg);
		        			   }
		        			   
		        			   // #http://tools.ietf.org/html/rfc2616#section-10.4.1
		        		   }
		            );
		    		// logged in and connected user, someone you know
		        	// alert("user logged in");
		        } else {
		            // no user session available, someone you dont know
		        	alert("Sorry, you are not logged in on deezer");
		        }
		    });
			
		}
		</script>
	</head>
	<% if(errorMsg == null) { %>
	<body onload="GoMap()">
	    <div id="dz-root"></div>
        <script>
	        window.dzAsyncInit = function() {
	            DZ.init({
	                appId : '<%=DeezerRestProvider.APP_ID%>',
	                channelUrl : '<%=DeezerRestProvider.CALLBACK_URL%>channel.html'
	            });
	        };
	        (function() {
	        	     var e = document.createElement('script');
	                 e.src = 'http://cdn-files.deezer.com/js/min/dz.js';
	                 e.async = true;
	                 document.getElementById('dz-root').appendChild(e);
	             }()
	        );
	    </script>
	
		<form id="main" method="get">
			<table style="width:100%">
				<tr>
					<td><a title="Just Map It! Deezer" href="./"><img alt="Just Map It! Lecko" src="../images/justmapit_deezer.png" /></a></td>
					<td>
					    <p>Discover music with the Just Map It! map for deezer</p>					    
					    <input type="hidden" name="access_token" value="<%=access_token%>" />
					    <fieldset>
                          <legend>Please select a category :</legend>
                          <label><input type="radio" name="maptype" value="artist" <%=("".equals(maptype) || "artist".equalsIgnoreCase(maptype)) ? "checked=\"checked\"" : ""%> />Favorite artists fans</label>
                          <label><input type="radio" name="maptype" value="album" <%=("album".equalsIgnoreCase(maptype)) ? "checked=\"checked\"" : ""%>  />Favorite albums fans</label>
                          <label><input type="radio" name="maptype" value="relArtist" <%=("relArtist".equalsIgnoreCase(maptype)) ? "checked=\"checked\"" : ""%> />Favorite artists deezer recommendations</label>
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
	<% } else { %>
	<body>
	   <header>
    	   <a title="Just Map It! Deezer" href="./"><img alt="Just Map It! Lecko" src="../images/logo-deezer.png" /></a>
	   </header>
	   <div>
	       <h1>An error occured</h1>
	       <p><%=errorMsg %></p>
	   </div>
	</body>
	<% } %>
</html>