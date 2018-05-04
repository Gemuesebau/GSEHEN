eval(instance.getUtf8ResourceAsOneString("/de/hgu/gsehen/js/utilities.js"));
var jsonData = JSON.parse(instance.readUTF8FileAsString(userDataFileName));

var Farm = Java.type("de.hgu.gsehen.model.Farm");
var Field = Java.type("de.hgu.gsehen.model.Field");
var Plot = Java.type("de.hgu.gsehen.model.Plot");
var GeoPolygon = Java.type("de.hgu.gsehen.gui.GeoPolygon");
var ArrayList = Java.type("java.util.ArrayList");

function buildJavaPolygon(jsonPolygon, logIndent) {
	var javaPolygon = new GeoPolygon();
	for each(var polygonPoint in jsonPolygon) {
		javaPolygon.addGeoPointByCoords(polygonPoint.lat, polygonPoint.lng);
		LOGGER.info(logIndent + "added polygon point " + polygonPoint.lng + ", " + polygonPoint.lat);
	}
	return javaPolygon;
}

farms.clear();
for each (var jsonFarm in jsonData) {
	LOGGER.info("Found farm '" + jsonFarm.name + "'");
	var javaFarm = new Farm();
	javaFarm.setName(jsonFarm.name);
	javaFarm.setPolygon(buildJavaPolygon(jsonFarm.polygon, ""));
	var fieldList = new ArrayList();
	for each (var jsonField in jsonFarm.fields) {
		LOGGER.info("  Found field '" + jsonField.name + "'");
		var javaField = new Field();
		javaField.setName(jsonField.name);
		javaField.setPolygon(buildJavaPolygon(jsonField.polygon, "  "));
		var plotList = new ArrayList();
		for each (var jsonPlot in jsonField.plots) {
			LOGGER.info("    Found plot '" + jsonPlot.name + "'");
			var javaPlot = new Plot();
			javaPlot.setName(jsonPlot.name);
			javaPlot.setPolygon(buildJavaPolygon(jsonPlot.polygon, "    "));
			plotList.add(javaPlot);
		}
		javaField.setPlots(plotList);
		fieldList.add(javaField);
	}
	javaFarm.setFields(fieldList);
	farms.add(javaFarm);
}
instance.notifyEventListeners();
