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
<%Boolean inverse = request.getParameter("Inverse") != null;%>
<link rel="stylesheet" type="text/css" href="../jmi-client/css/jmi-client.css" />
<script type="text/javascript" src="http://www.google.com/jsapi"></script> 
<script type="text/javascript" src="../jmi-client/jmi-client.js"></script>
<script type="text/javascript" src="./visualization/jmi-visualization.js"></script>
<script type="text/javascript">
google.load('visualization', '1');
google.setOnLoadCallback(googleVisualizationPackagesLoaded);
function googleVisualizationPackagesLoaded() {
    var source= 'TEST', sourceId= 'bof...';
	var data = {entities:[{id:"France",attributes:[{id:"hjkh"}]},{id:"Espagne ",attributes:[{id:"Europe"},{id:"hjkh"}]},{id:"Italie",attributes:[{id:"hkhjk"}]}],attributes:[{id:"Europe"},{id:"hjkh"},{id:"hkhjk"}]};
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

	var map = new JMI.google.Visualization('map');
	map.draw(data, {source:source,sourceId:sourceId,breadcrumb:'breadcrumb',invert:<%=inverse%>});
}
</script>
<jsp:include page="../ga.jsp" />
</head>
<body>
<form id="main" method="get">
<table width="100%" border="0">
	<tr>
		<td><a title="Just Map It! Labs" href=".."><img alt="Just Map It! Labs" src="../images/justmapit_labs.png" /></a></td>
		<td>
			<input type="submit" value="Just Map It!" />
			<input type="checkbox" name="Inverse" <%=inverse ? "checked" : ""%> onclick="document.getElementById('main').submit();"/>Inverse
		</td>
		<td align="right"><a title="Just Map It! Visualization" href="./"><img alt="Just Map It! Visualization" src="../images/justmapit.png" /></a></td>
	</tr>
</table>
</form>
<div id="breadcrumb">&nbsp;</div>
<div id="map"></div>
</body>
</html>
