loadGsehenJs("commons.js");

/* plugineigene Konfiguration und Hilfsmethoden

// schon berücksichtigt?! (s.u.)
de.hgu.gsehen.evapotranspiration.UtilityFunctions.convertWindSpeed2m

*/
function calculateWindspeed2m(windspeed, windspeedMeasHeightMeters) {
	if (windspeedMeasHeightMeters == 2) {
		return windspeed;
	}
	else {
		// FIXME calculate wind speed!
	}
}

function calculateDayData(pluginConfig, date, weatherDataArray) {
	var dayData = new (Java.type("de.hgu.gsehen.evapotranspiration.DayData"))();
	dayData.setDate(date);
	dayData.setTempMax(arrayUtilities.objArrayMax(weatherDataArray, "temp"));
	dayData.setTempMin(arrayUtilities.objArrayMin(weatherDataArray, "temp"));
	dayData.setTempMean(arrayUtilities.objArrayMean(weatherDataArray, "temp"));
	dayData.setAirHumidityRelMax(arrayUtilities.objArrayMax(weatherDataArray, "airHumidityRel"));
	dayData.setAirHumidityRelMin(arrayUtilities.objArrayMin(weatherDataArray, "airHumidityRel"));
	dayData.setAirHumidityRelMean(arrayUtilities.objArrayMean(weatherDataArray, "airHumidityRel"));
	dayData.setGlobalRad(arrayUtilities.objArraySum(weatherDataArray, "globalRad") * pluginConfig.measIntervalSeconds / 1000000);
	dayData.setPrecipitation(arrayUtilities.objArraySum(weatherDataArray, "precipitation"));
	dayData.setWindspeed2m(
		calculateWindspeed2m(arrayUtilities.objArrayMean(weatherDataArray, "windspeed"), pluginConfig.windspeedMeasHeightMeters)
	);
	return dayData;
}

function newNumberFormat(numberLocaleId) {
	var locale = java.util.Locale.ENGLISH;
	try {
		locale = java.util.Locale.class.getField(numberLocaleId).get(null);
	}
	catch (e) {
		// do nothing
	}
	return java.text.NumberFormat.getNumberInstance(locale);
}

function newDateFormat(dateFormatString) {
	return new java.text.SimpleDateFormat(dateFormatString);
}

function determineDayData(weatherDataSource, date) {
	var pluginConfig = JSON.parse(weatherDataSource.getPluginConfigurationJSON());
	var timeStamp = date.getTime();
	var weatherDataArray = [];
	var lineNumberReader = new java.io.LineNumberReader(new java.io.FileReader(pluginConfig.dataFilePath));
	var line;
	var lineNumber = 0;
	var dateFormat = newDateFormat(pluginConfig.dateFormatString);
	var numberFormat = newNumberFormat(pluginConfig.numberLocaleId);
	while ((line = lineNumberReader.readLine()) != null) {
		lineNumber++;
		try {
			if (dateFormat.parse(line.replace(/ .*/,"")).getTime() >= timeStamp) {
				weatherDataArray.push(arrayUtilities.arrayToObject(
					arrayUtilities.transformArray(line.split(/; */), 1, function (str) {
						return numberFormat.parse(str).doubleValue();
					}),
					[ "dateTimeStr", "temp", "airHumidityRel", "timeDuration",
						"windspeed", "globalRad", "battery", "precipitation" ]
				));
			}
		}
		catch (e) {
			// nothing - probably header or comment
			//print("Exception in data file line number " + lineNumber + ": " + e);
		}
	}
	if (weatherDataArray.length == 0) {
		return null;
	}
	return calculateDayData(pluginConfig, date, weatherDataArray);
}
