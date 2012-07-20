<!DOCTYPE html>
<%@page import="java.util.UUID"%>
<%@page import="com.socialcomputing.labs.deezer.services.DeezerRestProvider"%>
<%@page import="java.net.URLEncoder"%>
<html lang="fr">
	<head>
		<%
		DeezerRestProvider.checkExpirationDate(session);
		String access_token = (String) session.getAttribute("access_token");
		
		String errorReason = request.getParameter("error_reason");
		String errorMsg = null;
		if(errorReason != null && "user_denied".equals(errorReason)) {
			errorMsg = "You have to agree with the application permission requirements to use Just Map It! for Deezer"; 
		}
		else {
			if(access_token == null) {
				String code = request.getParameter("code");
				if(code != null) {
					access_token = DeezerRestProvider.getAccessToken(code, session);
				}
				else {
					response.sendRedirect(DeezerRestProvider.AUTHORIZE_ENDPOINT + "?app_id=" + DeezerRestProvider.APP_ID  
					+ "&redirect_uri=" + URLEncoder.encode(DeezerRestProvider.CALLBACK_URL, "UTF-8") 
					+ "&perms=" + DeezerRestProvider.APP_PERMS);
				}
			}
		}
		
        // Reading query parameters 
        Boolean inverse = request.getParameter("Inverse") != null;		
		%>
	    <title>Just Map It! Deezer</title>
	    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	    <meta name="viewport" content="target-densitydpi=device-dpi, width=device-width, user-scalable=no"/>
		<link rel="stylesheet" type="text/css" href="./css/style.css" />
		<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
		
		<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
		<script type="text/javascript" src="./js/jmi-deezer.js"></script>
		<script type="text/javascript">
			function GoMap() {
			    var map = new JMI.deezer.map('jmi-map', {
			    	'breadcrumb': 'jmi-breadcrumb',
			    	'map': {
			    		'map': 'Deezer',
			    		'access_token': '<%=access_token%>',
			    		'analysisProfile': 'GlobalProfile',
			    		'jsessionid': '<%=session.getId()%>',
			    		'maptype': 'relArtist',
			    	}});
			    map.draw();
			};
	    </script>
	</head>
	<body onload="GoMap()">
	    <!-- Deezer specific -->
	    <div id="dz-root"></div>
        <script>
	        window.dzAsyncInit = function() {
	            DZ.init({
	                appId : '<%=DeezerRestProvider.APP_ID%>',
	                channelUrl : '<%=DeezerRestProvider.ROOT_URL%>channel.html'
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
	    
	    <!-- Main layout -->
	    <!-- Header -->
	    <header>
			<table style="width:100%">
			<tr>
				<td><a title="Just Map It! Deezer" href="./"><img alt="Just Map It! Lecko" src="../images/justmapit_deezer.png" /></a></td>
				<td align="right"><a title="Just Map It!" href="http://www.social-computing.com/offre/cartographie-just-map-it/" target="_blank"><img alt="Just Map It!" src="../images/justmapit.png" /></a></td>
			</tr>
			</table>
		</header>
		
		<!-- Content -->
		<div id="content">
		    <h1>Discover music with the Just Map It! map for deezer</h1>
		    <p>A map is being generated from your favorite artists list. This map helps you discover new artists from the one you already like.</p>
		    <p>The relations between artists are constructed using deezer recommendation services.</p>
		    
			<!-- Map Elements-->
			<div id="jmi-breadcrumb">&nbsp;</div>
			<div id="jmi-map"></div>
		</div><!-- End Content -->
		
		<footer>
		    <div class="wrapper">
		        <!--Copyright Notice-->
                <p class="copyright_notice">Copyright 2012 - <strong>Social Computing</strong>. All Rights Reserved.</p>
            </div><!--.wrapper-->
        </footer><!--End Footer-->
	</body>
</html>