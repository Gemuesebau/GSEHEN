eval(instance.getUtf8ResourceAsOneString("/de/hgu/gsehen/js/utilities.js"));
eval(instance.getUtf8ResourceAsOneString("/de/hgu/gsehen/js/commons.js"));

var jsonFarms = [];

for each (var javaFarm in farms) {
  LOGGER.info("Processing farm '" + javaFarm.getName() + "'");
  var jsonFarm = { name: javaFarm.getName(), polygon: buildJavaScriptPolygon(javaFarm.getPolygon(), "") };
  jsonFarm.fields = [];
  for each (var javaField in javaFarm.getFields()) {
    LOGGER.info("  Processing field '" + javaField.getName() + "'");
    var jsonField = {
      name: javaField.getName(),
      area: javaField.getArea(),
      rootingZone: javaField.getRootingZone(),
      polygon: buildJavaScriptPolygon(javaField.getPolygon(), "  ")
    };
    jsonField.plots = [];
    for each (var javaPlot in javaField.getPlots()) {
      LOGGER.info("    Processing plot '" + javaPlot.getName() + "'");
      jsonField.plots.push({ name: javaPlot.getName(), polygon: buildJavaScriptPolygon(javaPlot.getPolygon(), "    ") });
    }
    jsonFarm.fields.push(jsonField);
  }
  jsonFarms.push(jsonFarm);
}

instance.writeStringAsUTF8File(JSON.stringify(jsonFarms, null, 2), userDataFileName);