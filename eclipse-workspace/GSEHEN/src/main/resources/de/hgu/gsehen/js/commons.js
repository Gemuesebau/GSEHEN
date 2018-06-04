function buildJavaScriptPolygon(javaPolygon, logIndent) {
  return buildJavaScriptPolygonLatLngCallback(javaPolygon, logIndent != null ? function (lat, lng) {
    LOGGER.info(logIndent + "added polygon point " + lng + ", " + lat);
  } : null);
}

function buildJavaScriptPolygonLatLngCallback(javaPolygon, callbackFunc) {
	var javaPolygonPoints = javaPolygon.getGeoPoints();
	var javaPolygonPointsLength = javaPolygonPoints.size();
	var javaScriptPolygon = [];
	for (var i=0; i<javaPolygonPointsLength; i++) {
		var polygonPoint = javaPolygonPoints.get(i);
		javaScriptPolygon.push({ lat: polygonPoint.getLat(), lng: polygonPoint.getLng() });
		if (callbackFunc != null) {
			callbackFunc(polygonPoint.getLat(), polygonPoint.getLng());
		}
	}
	return javaScriptPolygon;
}
