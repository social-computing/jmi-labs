<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!-- saved from url=(0014)about:internet -->
<html xmlns="http://www.w3.org/1999/xhtml" lang="fr" xml:lang="fr">	
    <head>
        <title>WPS turbulences application</title>
        <meta name="google" value="notranslate" />         
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<!-- Include CSS to eliminate any default margins/padding and set the height of the html element and 
		     the body element to 100%, because Firefox, or any Gecko based browser, interprets percentage as 
			 the percentage of the height of its parent container, which has to be set explicitly.  Fix for
			 Firefox 3.6 focus border issues.  Initially, don't display flashContent div so it won't show 
			 if JavaScript disabled.
		-->
        <style type="text/css" media="screen"> 
			html, body	{ height:100%; }
			body { margin:0; padding:0; overflow:auto; text-align:center; 
			       background-color: #FFFFFF; }   
			object:focus { outline:none; }
			#flashContent { display:none; }
        </style>
		
		<!-- Enable Browser History by replacing useBrowserHistory tokens with two hyphens -->
        <!-- BEGIN Browser History required section -->
        <link rel="stylesheet" type="text/css" href="../client/flex/history/history.css" />
        <script type="text/javascript" src="../client/flex/history/history.js"></script>
        <!-- END Browser History required section -->  
		
		<link rel="stylesheet" href="../css/main.css"/>
		<link rel="stylesheet" href="../css/wps.css" />
		
		<script type="text/javascript" src="../client/applet/jquery.js" ></script>
        <script type="text/javascript" src="../client/applet/jquery.wpsmap.js" ></script>    
        <script type="text/javascript" src="../client/flex/swfobject.js"></script>
        <script type="text/javascript">
            // For version detection, set to min. required Flash Player version, or 0 (or 0.0.0), for no version detection. 
            var swfVersionStr = "10.0.0";
            
            // To use express install, set to playerProductInstall.swf, otherwise the empty string.
            var xiSwfUrlStr = "../client/flex/playerProductInstall.swf";
            var flashvars = {};
            flashvars.wpsserverurl = "http://168.143.91.243:8080/wps-server/";
            flashvars.wpsplanname = "turbulences";
            flashvars.analysisProfile = "GlobalProfile";
            var params = {};
            params.quality = "high";
            params.bgcolor = "#FFFFFF";
            params.allowscriptaccess = "sameDomain";
            params.allowfullscreen = "true";
            var attributes = {};
            attributes.id = "wps-flex";
            attributes.name = "wps-flex";
            attributes.align = "middle";
            swfobject.embedSWF(
                "../client/flex/wps-flex-1.0-SNAPSHOT.swf", "flashContent", 
                "100%", "100%", 
                swfVersionStr, xiSwfUrlStr, 
                flashvars, params, attributes);
			// JavaScript enabled so display the flashContent div in case it is not replaced with a swf object.
			swfobject.createCSS("#flashContent", "display:block;text-align:left;");
        </script>
        <script type="text/javascript" >
	        function getMap() {
	            if (navigator.appName.indexOf ("Microsoft") !=-1) {
	                return window["wps-flex"];
	            } 
	            else {
	                return document["wps-flex"];
	            }
	        }
	        
	    	function ready() {
	    		// do something here
	    	}
	    	function status( status) {
	    		// do something here
	    	}
	    	function error( error) {
	    		alert( error);
	    	}
	    	function navigate( url, target) {
	    		// alert("url :"  + url + ", target : " + target); 
	    		// Nothing to do here, using native flex behavior
	    	}
	    	
	    	// Actions that are swatch defined
	        function NewWin(args) {
		        var parameters = {};
		        parameters["entityId"] = args[0];
		        getMap().compute(parameters);
	        }
	        function Discover( args) {
		        var parameters = {};
		        parameters["attributeId"] = args[0];
		        parameters["analysisProfile"] = "DiscoveryProfile";
		        getMap().compute(parameters);
	        }
        
        
        /*
	    	function setMap(params) {
	    		alert("setMap");
	    		params['planName'] = 'turbulences';
	
	    		$("#map").wpsmap({
	    			wps: params, 
	    			display: {color:'336699'},
	    			plugin: {noscript:'../../noscript.jsp'}
	    		});
	    	}
        
			// Fired from flex application
			function NewWin(id, name) {
				setMap({entityId:id});
				$('#titre').html(name);
			}
			
			// Fired from flex application
			function Discover(id, name) {
				setMap({analysisProfile:'DiscoveryProfile', attributeId:id});
				$('#titre').html( name);
			}
	    */
        </script>
    </head>
    <body>
    
<%--     	<div id="top"><jsp:include page="top.jsp" /></div> --%>
<%-- 	    <div id="menu"><jsp:include page="menu.jsp" /></div> --%>
        <!-- SWFObject's dynamic embed method replaces this alternative HTML content with Flash content when enough 
			 JavaScript and Flash plug-in support is available. The div is initially hidden so that it doesn't show
			 when JavaScript is disabled.
		-->
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
	    <div id="status">Status here</div>
	   	
       	<noscript>
            <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="100%" height="100%" id="wps-flex">
                <param name="movie" value="../client/flex/wps-flex-1.0-SNAPSHOT.swf" />
                <param name="quality" value="high" />
                <param name="bgcolor" value="#FFFFFF" />
                <param name="allowScriptAccess" value="sameDomain" />
                <param name="allowFullScreen" value="true" />
                <!--[if !IE]>-->
                <object type="application/x-shockwave-flash" data="../client/flex/wps-flex-1.0-SNAPSHOT.swf" width="100%" height="100%">
                    <param name="quality" value="high" />
                    <param name="bgcolor" value="#FFFFFF" />
                    <param name="allowScriptAccess" value="sameDomain" />
                    <param name="allowFullScreen" value="true" />
                <!--<![endif]-->
                <!--[if gte IE 6]>-->
                	<p> 
                		Either scripts and active content are not permitted to run or Adobe Flash Player version
                		10.0.0 or greater is not installed.
                	</p>
                <!--<![endif]-->
                    <a href="http://www.adobe.com/go/getflashplayer">
                        <img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash Player" />
                    </a>
                <!--[if !IE]>-->
                </object>
                <!--<![endif]-->
            </object>
	    </noscript>		
	    </div>
   </body>
</html>
