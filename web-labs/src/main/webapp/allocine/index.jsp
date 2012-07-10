<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.net.URLEncoder"%>
<%@page import="com.socialcomputing.labs.allocine.AllocineRestProvider"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Allocine</title>
<meta name="robots" content="noindex,nofollow" /> 
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
	text-align: center;
}
img {
	border: 0;
}
</style>
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<script type="text/javascript"> 
var breadcrumb, breadcrumbTitles = { shortTitle: '', longTitle: '' };
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
};

function GoMap(id, name) {
	var map = JMI.Map({
				parent: 'map', 
				//server: 'http://localhost:8080/jmi-server/',
				//client: JMI.Map.SWF,
				clientUrl: '../jmi-client/'
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
	same(map, [id,name]);
};
$(function() {
	$('#filter').change(function(){
		if( breadcrumb)
			breadcrumb.reset();
		fillFilter($('#filter option:selected').get(0).value);
	}); 
	fillFilter($('#filter option:selected').get(0).value);
});

function fillFilter(filter) {
	var i, movie, html='';
	$.ajax({
		  type: "GET",
		  dataType: 'json',
		  url: "<%=AllocineRestProvider.API_URL%>/rest/v3/movieList?partner=<%=AllocineRestProvider.API_KEY%>&format=json&count=50&filter=" + escape(filter)
	}).done(function( jso ) {
		/*if( typeof jso === "string")
			jso = jQuery.parseJSON(jso);*/
		if( jso && jso.feed && jso.feed.movie) {
			for( i = 0 ; i < jso.feed.movie.length; ++i) {
				movie = jso.feed.movie[i];
				html += '<a class="maplink" href="' + movie.code + '" >' + movie.title + '</a><br/>';
			}
			$('#map').get(0).innerHTML = html;
			$('.maplink').click(function( event){
				event.preventDefault();
				$('#map').get(0).innerHTML = '';
				GoMap(event.target.href.substr(event.target.href.lastIndexOf("/")+1), event.target.innerHTML);
			}); 
		}
	});
}
function navigate(map, id) {
	 window.open( "http://www.allocine.fr/film/fichefilm_gen_cfilm=" + id + ".html", "_blank");
}
function focus(map, args) {
	var parameters = {};
	parameters["entityId"] = args[0];
	completeParameters( parameters);
	breadcrumbTitles.shortTitle = "Focus";
	breadcrumbTitles.longTitle = "Focus sur " + args[1];
	map.compute( parameters);
}
function center(map, args) {
	var parameters = {};
	parameters["attributeId"] = args[0];
	parameters["analysisProfile"] = "DiscoveryProfile";
	completeParameters( parameters);
	breadcrumbTitles.shortTitle = "Centré";
	breadcrumbTitles.longTitle = "Centré sur " + args[1];
	map.compute( parameters);
}
function same(map, args) {
	var parameters = {};
	parameters["attributeId"] = args[0];
	parameters["analysisProfile"] = "DiscoveryProfile";
	completeParameters( parameters);
	parameters.kind = 'film_same';
	parameters.filter = args[0];
	breadcrumbTitles.shortTitle = "Similaires";
	breadcrumbTitles.longTitle = "Similaires à " + args[1];
	map.compute( parameters);
}
function completeParameters(parameters) {
	//parameters.allocineserverurl = "http://localhost:8080/web-labs";
	parameters.allocineserverurl = "http://labs.just-map-it.com";
	parameters.map = "Allocine";
	parameters.filter = $('#filter option:selected').get(0).value;
	parameters.jsessionid = '<%=session.getId()%>';
}
</script>
<jsp:include page="../ga.jsp" />
</head>
<body>
<table width="100%" border="0">
	<tr>
		<td align="left"><a title="Just Map It! Allocine" href="./"><img alt="Just Map It! Allocine" src="../images/justmapit_allocine.png" /></a></td>
		<td>
		<select id="filter">
		<option value="outthisweek">Sorties de la semaine</option>
		<option value="nowshowing" >A l'écran</option>
		<option value="comingsoon" >Bientôt à l'affiche</option>
		<option value="top:week" >Meilleurs films de la semaine</option>
		<option value="top:month" >Meilleurs films du mois</option>
		<option value="top:alltimes" >Meilleurs films</option>
		<option value="worst" >Les pires films</option>
		</select>
		</td>
		<td align="right"><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
	</tr>
</table>
<div id="breadcrumb">&nbsp;</div>
<div id="map"></div>
</body>
</html>
