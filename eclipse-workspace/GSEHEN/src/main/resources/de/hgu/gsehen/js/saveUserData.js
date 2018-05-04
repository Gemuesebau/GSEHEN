eval(instance.getUtf8ResourceAsOneString("/de/hgu/gsehen/js/utilities.js"));

function buildJsonPolygon(javaPolygon, logIndent) {
	var jsonPolygon = [];
	for each(var polygonPoint in javaPolygon.getGeoPoints()) {
		jsonPolygon.push({ lng: polygonPoint.getLng(), lat: polygonPoint.getLat() });
		LOGGER.info(logIndent + "added polygon point " + polygonPoint.getLng() + ", " + polygonPoint.getLat());
	}
	return jsonPolygon;
}

var jsonFarms = [];

for each (var javaFarm in farms) {
	LOGGER.info("Processing farm '" + javaFarm.getName() + "'");
	var jsonFarm = { name: javaFarm.getName(), polygon: buildJsonPolygon(javaFarm.getPolygon(), "") };
	jsonFarm.fields = [];
	for each (var javaField in javaFarm.getFields()) {
		LOGGER.info("  Processing field '" + javaField.getName() + "'");
		var jsonField = { name: javaField.getName(), polygon: buildJsonPolygon(javaField.getPolygon(), "  ") };
		jsonField.plots = [];
		for each (var javaPlot in javaField.getPlots()) {
			LOGGER.info("    Processing plot '" + javaPlot.getName() + "'");
			jsonField.plots.push({ name: javaPlot.getName(), polygon: buildJsonPolygon(javaPlot.getPolygon(), "    ") });
		}
		jsonFarm.fields.push(jsonField);
	}
	jsonFarms.push(jsonFarm);
}

instance.writeStringAsUTF8File(JSON.stringify(jsonFarms, null, 2), userDataFileName);
