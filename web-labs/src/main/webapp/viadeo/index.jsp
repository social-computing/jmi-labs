<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!-- saved from url=(0014)about:internet -->
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
 <head>
<title>Just Map It! Viadeo</title>
<%String code = request.getParameter("code");
if( code == null) { %>
<title>Redirection</title>
<meta name="robots" content="noindex,follow" />
<meta http-equiv="refresh" content="0; url=https://secure.viadeo.com/oauth-provider/authorize2?response_type=code&display=popup&lang=en&client_id=SocialComputingMapViTBUC&redirect_uri=http://labs.just-map-it.com/viadeo/index.jsp" />
</head>
<body>
</body>
</html>
<%} else {%>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <style type="text/css" media="screen"> 
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
<script type="text/javascript" src="../client/flex/history/history.js"></script>
<link rel="stylesheet" href="../css/main.css"/>

<script type="text/javascript">
function navigate( url, target) {
	window.open( url, target);
}
</script>
  
<script type="text/javascript" src="../client/flex/swfobject.js"></script>
<script type="text/javascript">
   var swfVersionStr = "10.0.0";
   var xiSwfUrlStr = "../client/flex/playerProductInstall.swf";
   var flashvars = {};
   flashvars.wpsserverurl = "http://server.just-map-it.com/";
   flashvars.wpsplanname = "Viadeo";
   flashvars.authcode = '<%=code%>';
   flashvars.analysisProfile = "GlobalProfile";
   flashvars.kind = "contacts";
   var params = {};
   params.quality = "high";
   params.bgcolor = "#FFFFFF";
   params.allowscriptaccess = "sameDomain";
   params.allowfullscreen = "true";
   var attributes = {};
   attributes.id = "wps-viadeo";
   attributes.name = "wps-viadeo";
   attributes.align = "middle";
   swfobject.embedSWF(
           "../client/flex/jmi-viadeo-1.0-SNAPSHOT.swf", "flashContent", 
           "100%", "100%", 
           swfVersionStr, xiSwfUrlStr, 
           flashvars, params, attributes);
	swfobject.createCSS("#flashContent", "display:block;text-align:left;");
</script>
</head>
<body>
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Viadeo" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
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
            <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="100%" height="100%" id="wps-flex-demo">
                <param name="movie" value="../client/flex/jmi-viadeo-1.0-SNAPSHOT.swf" />
                <param name="quality" value="high" />
                <param name="bgcolor" value="#FFFFFF" />
                <param name="allowScriptAccess" value="sameDomain" />
                <param name="allowFullScreen" value="true" />
                <!--[if !IE]>-->
                <object type="application/x-shockwave-flash" data="../client/flex/jmi-viadeo-1.0-SNAPSHOT.swf" width="100%" height="100%">
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
<%} %>
