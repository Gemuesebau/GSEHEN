eval(instance.getUtf8ResourceAsOneString("/de/hgu/gsehen/js/utilities.js"));
eval(instance.getUtf8ResourceAsOneString("/de/hgu/gsehen/js/javaTypes.js"));

var bounds = {
  minX:  180,
  minY:  90,
  maxX: -180,
  maxY: -90
};

try {
  var jsonData = JSON.parse(instance.readUTF8FileAsOneString(userDataFileName));
  farms.clear();

  function updateBounds(bounds, x, y) {
    if (x < bounds.minX) {
      bounds.minX = x;
    }
    if (y < bounds.minY) {
      bounds.minY = y;
    }
    if (x > bounds.maxX) {
      bounds.maxX = x;
    }
    if (y > bounds.maxY) {
      bounds.maxY = y;
    }
  }

  function updateBoundsAndBuildJavaPolygon(bounds, jsonPolygon, logIndent) {
    var javaPolygon = new GeoPolygon();
    for each(var polygonPoint in jsonPolygon) {
      javaPolygon.addGeoPointByCoords(polygonPoint.lat, polygonPoint.lng);
      updateBounds(bounds, polygonPoint.lng, polygonPoint.lat);
      //LOGGER.info(logIndent + "added polygon point " + polygonPoint.lng + ", " + polygonPoint.lat);
    }
    return javaPolygon;
  }

  for each (var jsonFarm in jsonData) {
    LOGGER.info("Found farm '" + jsonFarm.name + "'");
    var javaFarm = new Farm();
    javaFarm.setName(jsonFarm.name);
    javaFarm.setPolygon(updateBoundsAndBuildJavaPolygon(bounds, jsonFarm.polygon, ""));
    var fieldList = new ArrayList();
    for each (var jsonField in jsonFarm.fields) {
      LOGGER.info("  Found field '" + jsonField.name + "'");
      var javaField = new Field();
      javaField.setName(jsonField.name);
      javaField.setPolygon(updateBoundsAndBuildJavaPolygon(bounds, jsonField.polygon, "  "));
      var plotList = new ArrayList();
      for each (var jsonPlot in jsonField.plots) {
        LOGGER.info("    Found plot '" + jsonPlot.name + "'");
        var javaPlot = new Plot();
        javaPlot.setName(jsonPlot.name);
        javaPlot.setPolygon(updateBoundsAndBuildJavaPolygon(bounds, jsonPlot.polygon, "    "));
        plotList.add(javaPlot);
      }
      javaField.setPlots(plotList);
      fieldList.add(javaField);
    }
    javaFarm.setFields(fieldList);
    farms.add(javaFarm);
  }
}
catch (ex) {
  LOGGER.log(Level.WARNING, "User data couldn't be loaded", ex);
}

instance.notifyEventListeners(function () {
  var event = new FarmDataChanged();
  event.setFarms(farms);
  event.setViewport(new Pair(
    new GeoPoint(bounds.minY, bounds.minX),
    new GeoPoint(bounds.maxY, bounds.maxX)
  ));
  return event;
}, null);
