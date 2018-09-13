loadGsehenJs("commons.js");

/* plugineigene Konfiguration
@SuppressWarnings({"checkstyle:javadocmethod", "checkstyle:rightcurly"})
public DecimalFormat getNumberFormat() {
  Locale locale = Locale.ENGLISH;
  try {
    locale = (Locale)Locale.class.getField(numberLocaleId).get(null);
  }
  catch (Exception e) {
    // do nothing
  }
  return (DecimalFormat)NumberFormat.getNumberInstance(locale);
}

public SimpleDateFormat getDateFormat() {
  return new SimpleDateFormat(dateFormatString);
}
private int measIntervalSeconds;
private double windspeedMeasHeightMeters;
private String dateFormatString;
private String numberLocaleId;
private String dataFilePath;
private double locationLng;
private double locationLat;
private double locationMetersAboveSeaLevel;

import de.hgu.gsehen.evapotranspiration.GeoData;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
*/

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
	dayData.setTempMax(arrayUtilities.objArrayMax(weatherDataArray, "temp"));
	dayData.setTempMin(arrayUtilities.objArrayMin(weatherDataArray, "temp"));
	dayData.setTempMean(arrayUtilities.objArrayMean(weatherDataArray, "temp"));
	dayData.setAirHumidityRelMax(arrayUtilities.objArrayMax(weatherDataArray, "airHumidityRel"));
	dayData.setAirHumidityRelMin(arrayUtilities.objArrayMin(weatherDataArray, "airHumidityRel"));
	dayData.setAirHumidityRelMean(arrayUtilities.objArrayMean(weatherDataArray, "airHumidityRel"));
	dayData.setGlobalRad(arrayUtilities.objArraySum(weatherDataArray, "globalRad") * weatherDataSource.getMeasIntervalSeconds() / 1000000);
	dayData.setPrecipitation(arrayUtilities.objArraySum(weatherDataArray, "precipitation"));
	dayData.setWindspeed2m(
		calculateWindspeed2m(arrayUtilities.objArrayMean(weatherDataArray, "windspeed"), weatherDataSource.getWindspeedMeasHeightMeters())
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
			/* nothing - probably header or comment */
		}
	}
	if (weatherDataArray.length == 0) {
		return null;
	}
	return calculateDayData(weatherDataSource, date, weatherDataArray);
}
