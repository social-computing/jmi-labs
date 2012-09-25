<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Saur</title>
<meta name="robots" content="noindex,nofollow" /> 
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="target- densitydpi=device-dpi, width=device-width, user-scalable=no"/>
<style type="text/css" media="screen">
html, body {
	height: 100%;
}
#map {
	width: 100%;
	height: 100%;
	background-color: #FFFFFF;
	text-align: center;
}
img {
	border: 0;
}
</style>
<%Boolean inverse = request.getParameter("Inverse") != null;
String dataFormat = request.getParameter("dataFormat") != null ? request.getParameter("dataFormat") : "matrix";
String sourceId = "https://docs.google.com/spreadsheet/pub?key=0AlyyavYc1ciUdHJ6enh3R1ZkUXEzM1EySTdFdlBTZWc";%>
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="https://www.google.com/jsapi"></script> 
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript" src="./js/jmi-visualization.js"></script>
<script type="text/javascript">
google.load('visualization', '1', {packages:['table']});
google.setOnLoadCallback(googleVisualizationPackagesLoaded);
function googleVisualizationPackagesLoaded() {

	var query = new google.visualization.Query('<%=sourceId%>');
    query.send(handleQueryResponse);
};

handleQueryResponse = function(response) {
    if (response.isError()) {
        document.getElementById('breadcrumb').innerHTML= "Error on data source: "+ response.getDetailedMessage();
    }
    else {
    	display( response.getDataTable());
    }
};

function display(data) {
	var map = new JMI.google.Visualization(document.getElementById('map'));
	map.draw(data, {sourceId:'<%=sourceId%>',breadcrumb:'breadcrumb',invert:<%=inverse%>,dataFormat:'<%=dataFormat%>'});

    var table = new google.visualization.Table(document.getElementById('table'));
    table.draw(data, {});
    
    google.visualization.events.addListener(table, 'select', function() {
    	if( table.getSelection().length > 0) {
			var i, row = table.getSelection()[0].row, id = data.getFormattedValue(row, 2) + ' ' + data.getFormattedValue(row, 3);
			for( i=0; i < map.visualizationData.attributes.length; ++i ) {
				if( map.visualizationData.attributes[i].id === id) {
					if( map.invert)
						map.invert = false;
		    		JMI.google.Visualization.JMIF_Center(map.map, [id, id]);	
		    		return;
				}
	    	}
    	}
    });    			
};
</script>
<jsp:include page="../ga.jsp" /> 
</head>
<body>
<table width="100%" border="0">
	<tr>
		<!-- td align="left"><a title="Just Map It! Saur" href="./"><img alt="Just Map It! Saur" src="../images/justmapit_allocine.png" /></a></td-->
		<td align="left"><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
	</tr>
</table>
<div style="width: 80%; height: 80%; float: left;">
	<div id="breadcrumb">&nbsp;</div>
	<div id="map"></div>
</div>
<div style="width: 20%; float: right;">
	<div id="table" style="width: 100%; height:680px;"></div>
</div>
</body>
</html>
