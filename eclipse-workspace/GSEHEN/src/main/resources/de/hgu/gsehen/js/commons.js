function buildJavaScriptPolygon(javaPolygon, logIndent) {
	var javaPolygonPoints = javaPolygon.getGeoPoints();
	var javaPolygonPointsLength = javaPolygonPoints.size();
	var javaScriptPolygon = [];
	for (var i=0; i<javaPolygonPointsLength; i++) {
		var polygonPoint = javaPolygonPoints.get(i);
		javaScriptPolygon.push({ lng: polygonPoint.getLng(), lat: polygonPoint.getLat() });
		//if (logIndent != null) {
		//	LOGGER.info(logIndent + "added polygon point " + polygonPoint.getLng() + ", " + polygonPoint.getLat());
		//}
	}
	return javaScriptPolygon;
}
