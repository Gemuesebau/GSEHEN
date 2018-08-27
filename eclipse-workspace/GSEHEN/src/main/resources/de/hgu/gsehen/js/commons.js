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

var eventsDebouncer = {
  timeoutObjects: {},
  debounce: function(timeoutObjKey, delegateFunc, delegateFuncArgsAsArray) {
    if (eventsDebouncer.timeoutObjects[timeoutObjKey] != null) {
      window.clearTimeout(eventsDebouncer.timeoutObjects[timeoutObjKey]);
      eventsDebouncer.timeoutObjects[timeoutObjKey] = null;
    }
    if (delegateFuncArgsAsArray == null || delegateFuncArgsAsArray.length == 0) {
      eventsDebouncer.timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500);
    }
    else if (delegateFuncArgsAsArray.length == 1) {
      eventsDebouncer.timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500,
        delegateFuncArgsAsArray[0]
      );
    }
    else if (delegateFuncArgsAsArray.length == 2) {
      eventsDebouncer.timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500,
        delegateFuncArgsAsArray[0],
        delegateFuncArgsAsArray[1]
      );
    }
    else {
      alert("No debounce implementation for " + delegateFuncArgsAsArray.length +
        " delegate function arguments!");
    }
  }
}

function truncToDayUsingCalendar(calendar, date) {
    calendar.setTime(date);
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
    calendar.set(java.util.Calendar.MINUTE, 0);
    calendar.set(java.util.Calendar.SECOND, 0);
    calendar.set(java.util.Calendar.MILLISECOND, 0);
    return calendar.getTime();
}

function truncToDay(date) {
    return truncToDayUsingCalendar(java.util.Calendar.getInstance(), date);
}

function transformArray(arr, startIndex, transformFunc) {
	for (var i=startIndex; i<arr.length; i++) {
		arr[i] = transformFunc(arr[i]);
	}
	return arr;
}

function arrayToObject(arr, propNamesArray) {
	var obj = {};
	for (var i=0; i<arr.length; i++) {
		obj[propNamesArray[i]] = arr[i];
	}
	return obj;
}
