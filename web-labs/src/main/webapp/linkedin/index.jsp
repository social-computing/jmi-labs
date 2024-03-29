<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.util.Map"%>
<%@page import="com.socialcomputing.wps.server.planDictionnary.connectors.utils.OAuthHelper"%>
<%@page import="com.socialcomputing.labs.linkedin.RestProvider"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Linkedin</title>
<meta name="robots" content="index,follow" /> 
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="content-language" content="en" />
<meta name="viewport" content="target- densitydpi=device-dpi, width=device-width, user-scalable=no"/>
<meta name="description" content="Just Map It! Linkedin helps you understand how your LinkedIn contacts are connected through their Skills. You can navigate from contact to contact (Center) or see whom in your contacts are sharing one skill." />
<meta name="keywords" content="just map it, map, cartography, dataviz, visualization, data visualization, social computing, representation, information, skill, skills, competence, expert, expertise, talent, linkedin" />
<meta name="author" content="Social Computing" /> 
<meta property="og:title" content="Just Map It! Linkedin" />
<meta property="og:description" content="Just Map It! Linkedin helps you understand how your LinkedIn contacts are connected through their Skills. You can navigate from contact to contact (Center) or see whom in your contacts are sharing one skill." />
<meta property="og:image" content="http://labs.just-map-it.com/images/thumbnail-linkedin.png" />
<style type="text/css" media="screen">
html, body {
	height: 100%;
	background-color: #FFFFFF;
	font-family: Arial, Helvetica, 'Nimbus Sans L', sans-serif;
}
#explain {
  width: 960px;
  margin-left: auto;
  margin-right: auto;
  color: #006699;
  font-size:24px;
}
#go, #go a {
  width: 960px;
  margin-left: auto;
  margin-right: auto;
  text-align: center;
  color: #006699;
  font-size:16px;
}
img {
	border: 0;
}
</style>
<jsp:include page="../ga.jsp" />
</head>
<body>
<table width="100%" border="0">
	<tr>
		<td rowspan="2"><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
<script src="//platform.linkedin.com/in.js" type="text/javascript"></script>
<script type="IN/Share" data-url="http://labs.just-map-it.com/linkedin/" data-counter="right"></script>
		</td>
		<td rowspan="2">
<iframe src="//www.facebook.com/plugins/like.php?href=https%3A%2F%2Flabs.just-map-it.com%2Flinkedin%2F&amp;send=false&amp;layout=standard&amp;width=450&amp;show_faces=false&amp;action=like&amp;colorscheme=light&amp;font=arial&amp;height=35&amp;appId=136353756473765" scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:450px; height:35px;" allowTransparency="true"></iframe>		</td>
		<td rowspan="2" align="right"><a title="Just Map It! Linkedin" href="./"><img alt="Just Map It! Linkedin" src="../images/justmapit_linkedin.png" /></a></td>
	</tr>
	<tr>
		<td>
<a href="https://twitter.com/share" class="twitter-share-button" data-url="http://labs.just-map-it.com/linkedin/">Tweet</a>
<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
		</td>
		<td></td>
	</tr>
</table>
<div id="explain">
	<p>&nbsp;</p>
	<p>&nbsp;</p>
	<p>Just Map It! Linkedin helps you understand how your LinkedIn contacts are connected through their Skills.</p>
	<p>You can navigate from contact to contact (Center) or see whom in your contacts are sharing one skill ...</p>
</div>
<div id="go">
	<p><a title="Just Map It! Linkedin" href="map.jsp"><img alt="Just Map It! Linkedin" src="../images/linkedin-in.png" /></a>
	<br/><a title="Just Map It! Linkedin" href="map.jsp">Click to try!</a></p>
</div>
</body>
</html>
