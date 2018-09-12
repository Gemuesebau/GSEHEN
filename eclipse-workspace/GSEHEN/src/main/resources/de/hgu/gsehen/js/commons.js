function buildJavaScriptPolygon(javaPolygon, logIndent) {
	return buildJavaScriptPolygonLatLngCallback(javaPolygon, logIndent != null ? function (lat, lng) {
		LOGGER.info(logIndent + "added polygon point " + lng + ", " + lat);
	}
		 : null);
}

function buildJavaScriptPolygonLatLngCallback(javaPolygon, callbackFunc) {
	var javaPolygonPoints = javaPolygon.getGeoPoints();
	var javaPolygonPointsLength = javaPolygonPoints.size();
	var javaScriptPolygon = [];
	for (var i = 0; i < javaPolygonPointsLength; i++) {
		var polygonPoint = javaPolygonPoints.get(i);
		javaScriptPolygon.push({
			lat: polygonPoint.getLat(),
			lng: polygonPoint.getLng()
		});
		if (callbackFunc != null) {
			callbackFunc(polygonPoint.getLat(), polygonPoint.getLng());
		}
	}
	return javaScriptPolygon;
}

var eventsDebouncer = {
	timeoutObjects: {},
	debounce: function (timeoutObjKey, delegateFunc, delegateFuncArgsAsArray) {
		if (eventsDebouncer.timeoutObjects[timeoutObjKey] != null) {
			window.clearTimeout(eventsDebouncer.timeoutObjects[timeoutObjKey]);
			eventsDebouncer.timeoutObjects[timeoutObjKey] = null;
		}
		if (delegateFuncArgsAsArray == null || delegateFuncArgsAsArray.length == 0) {
			eventsDebouncer.timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500);
		} else if (delegateFuncArgsAsArray.length == 1) {
			eventsDebouncer.timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500,
					delegateFuncArgsAsArray[0]);
		} else if (delegateFuncArgsAsArray.length == 2) {
			eventsDebouncer.timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500,
					delegateFuncArgsAsArray[0],
					delegateFuncArgsAsArray[1]);
		} else {
			alert("No debounce implementation for " + delegateFuncArgsAsArray.length +
				" delegate function arguments!");
		}
	}
}

var arrayUtilities = {
	transformArray: function(arr, startIndex, transformFunc) {
		for (var i = startIndex; i < arr.length; i++) {
			arr[i] = transformFunc(arr[i]);
		}
		return arr;
	},
	arrayToObject: function(arr, propNamesArray) {
		var obj = {};
		for (var i = 0; i < arr.length; i++) {
			obj[propNamesArray[i]] = arr[i];
		}
		return obj;
	},
	objArrayAggregate: function(arr, propName, initVal, stepFunc) {
		if (arr == null || arr.length == 0) {
			return null;
		}
		var val = initVal;
		for (var i = 0; i < arr.length; i++) {
			val = stepFunc(val, arr[i][propName]);
		}
		return val;
	},
	objArrayMin: function(arr, propName) {
		return arrayUtilities.objArrayAggregate(arr, propName,
			Number.MAX_VALUE,
			function (currentValue, arrayItemValue) {
				if (arrayItemValue < currentValue) {
					return arrayItemValue;
				} else {
					return currentValue;
				}
		});
	},
	objArrayMax: function(arr, propName) {
		return arrayUtilities.objArrayAggregate(arr, propName,
			Number.MIN_VALUE,
			function (currentValue, arrayItemValue) {
				if (arrayItemValue > currentValue) {
					return arrayItemValue;
				} else {
					return currentValue;
				}
		});
	},
	objArraySum: function(arr, propName) {
		return arrayUtilities.objArrayAggregate(arr, propName,
			0,
			function (currentValue, arrayItemValue) {
				return currentValue + arrayItemValue;
		});
	},
	objArrayMean: function(arr, propName) {
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arrayUtilities.objArraySum(arr, propName) / arr.length;
	}
}
