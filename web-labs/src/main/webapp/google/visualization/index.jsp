<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Just Map It! Visualization</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="robots" content="index,follow" /> 
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
String sourceId = request.getParameter("sourceId");
if( sourceId == null || sourceId.length() == 0) 
    sourceId = "https://docs.google.com/spreadsheet/tq?range=A1%3AE196&key=0AlyyavYc1ciUdHVNX1AyQU5sSnNWZGFXeDIyUmdqMlE&gid=0&headers=-1";
%>
<link rel="stylesheet" type="text/css" href="../../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="http://www.google.com/jsapi"></script> 
<script type="text/javascript" src="../../jmi-client/jmi-client.js"></script>
<script type="text/javascript" src="./jmi-visualization.js"></script>
<script type="text/javascript">
google.load('visualization', '1', {packages:['table']});
google.setOnLoadCallback(googleVisualizationPackagesLoaded);
function googleVisualizationPackagesLoaded() {

	var data = new google.visualization.DataTable();
	data.addColumn('string', 'Pays');
	data.addColumn('boolean', 'Bleu');
	data.addColumn('boolean', 'Blanc');
	data.addColumn('boolean', 'Rouge');
	data.addColumn('boolean', 'Jaune');
	data.addColumn('boolean', 'Vert');
	data.addColumn('boolean', 'Noir');
	data.addRows(5);
	data.setCell(0, 0, 'France');
	data.setCell(0, 1, true);
	data.setCell(0, 2, true);
	data.setCell(0, 3, true);
	data.setCell(1, 0, 'Italie');
	data.setCell(1, 5, true);
	data.setCell(1, 2, true);
	data.setCell(1, 3, true);
	data.setCell(2, 0, 'Espagne');
	data.setCell(2, 4, true);
	data.setCell(2, 3, true);

	//display( data);
	
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
    var table = new google.visualization.Table(document.getElementById('table'));
    table.draw(data, {});
	
	var map = new JMI.google.Visualization(document.getElementById('map'));
	map.draw(data, {sourceId:'<%=sourceId%>',breadcrumb:'breadcrumb',invert:<%=inverse%>});

    google.visualization.events.addListener(map, 'select', function() {
		table.setSelection( map.getSelection());	
    });    
    google.visualization.events.addListener(table, 'select', function() {
		map.setSelection( table.getSelection());	
    });    
}
</script>
<jsp:include page="../../ga.jsp" />
</head>
<body>
<form id="main" method="get">
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../../images/justmapit_labs.png" /></a></td>
		<td>
			<input type="text" name="sourceId" title="Google Data Source" size="80" value="<%=sourceId%>" />			
			<input type="submit" value="Just Map It!" />
			<input type="checkbox" name="Inverse" <%=inverse ? "checked" : ""%> onclick="document.getElementById('main').submit();"/>Inverse
		</td>
		<td align="right"><a title="Just Map It! Visualization" href="./"><img alt="Just Map It! Visualization" src="../../images/justmapit.png" /></a></td>
	</tr>
</table>
</form>
<div id="breadcrumb">&nbsp;</div>
<div id="map" style="width: 45%;float: left;"></div>
<div id="table" style="width: 50%; height:600px;float: right;"></div>
</body>
</html>
