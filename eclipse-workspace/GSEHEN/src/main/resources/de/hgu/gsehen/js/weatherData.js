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

function objArrayAggregate(arr, propName, initVal, stepFunc) {
	if (arr==null || arr.length==0) {
		return null;
	}
	var val = initVal;
	for (var i=0; i<arr.length; i++) {
		val = stepFunc(val, arr[i][propName]);
	}
	return val;
}

function objArrayMin(arr, propName) {
	return objArrayAggregate(arr, propName,
		java.lang.Double.MAX_VALUE,
		function(currentValue, arrayItemValue) {
			if (arrayItemValue < currentValue) {
				return arrayItemValue;
			}
			else {
				return currentValue;
			}
		});
}

function objArrayMax(arr, propName) {
	return objArrayAggregate(arr, propName,
		java.lang.Double.MIN_VALUE,
		function(currentValue, arrayItemValue) {
			if (arrayItemValue > currentValue) {
				return arrayItemValue;
			}
			else {
				return currentValue;
			}
		});
}

function objArraySum(arr, propName) {
	return objArrayAggregate(arr, propName,
		0,
		function(currentValue, arrayItemValue) {
			return currentValue + arrayItemValue;
		});
}

function objArrayMean(arr, propName) {
	if (arr==null || arr.length==0) {
		return null;
	}
	return objArraySum(arr, propName) / arr.length;
}

function calculateWindspeed2m(windspeed, windspeedMeasHeightMeters) {
	if (windspeedMeasHeightMeters == 2) {
		return windspeed;
	}
	else {
		// FIXME calculate wind speed!
	}
}

function calculateDayData(weatherDataSource, date, weatherDataArray) {
	var dayData = new (Java.type("de.hgu.gsehen.evapotranspiration.DayData"))();
	dayData.setDate(date);
	dayData.setTempMax(objArrayMax(weatherDataArray, "temp"));
	dayData.setTempMin(objArrayMin(weatherDataArray, "temp"));
	dayData.setTempMean(objArrayMean(weatherDataArray, "temp"));
	dayData.setAirHumidityRelMax(objArrayMax(weatherDataArray, "airHumidityRel"));
	dayData.setAirHumidityRelMin(objArrayMin(weatherDataArray, "airHumidityRel"));
	dayData.setAirHumidityRelMean(objArrayMean(weatherDataArray, "airHumidityRel"));
	dayData.setGlobalRad(objArraySum(weatherDataArray, "globalRad") * weatherDataSource.getMeasIntervalSeconds() / 1000000);
	dayData.setPrecipitation(objArraySum(weatherDataArray, "precipitation"));
	dayData.setWindspeed2m(
		calculateWindspeed2m(objArrayMean(weatherDataArray, "windspeed"), weatherDataSource.getWindspeedMeasHeightMeters())
	);
	return dayData;
}

function determineDayData(weatherDataSource, date) {
	var timeStamp = date.getTime();
	var weatherDataArray = [];
	var lineNumberReader = new java.io.LineNumberReader(new java.io.FileReader(weatherDataSource.getDataFilePath()));
	var line;
	var dateFormat = weatherDataSource.getDateFormat();
	var numberFormat = weatherDataSource.getNumberFormat();
	while ((line = lineNumberReader.readLine()) != null) {
		try {
			if (dateFormat.parse(line.replace(/ .*/,"")).getTime() >= timeStamp) {
				weatherDataArray.push(arrayToObject(
					transformArray(line.split(/; */), 1, function (str) {
						return numberFormat.parse(str).doubleValue();
					}),
					[ "dateTimeStr", "temp", "airHumidityRel", "timeDuration",
						"windspeed", "globalRad", "battery", "precipitation" ]
				));
			}
		}
		catch (e) {
			/* nothing - probably header or comment */
		}
	}
	if (weatherDataArray.length == 0) {
		return null;
	}
	return calculateDayData(weatherDataSource, date, weatherDataArray);
}
