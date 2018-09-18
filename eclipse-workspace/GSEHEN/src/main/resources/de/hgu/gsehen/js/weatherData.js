loadGsehenJs("commons.js");

/* plugineigene Konfiguration und Hilfsmethoden

de.hgu.gsehen.evapotranspiration.UtilityFunctions.convertWindSpeed2m

fieldview.weatherdataname                = Name der Wetterdatenquelle: 
fieldview.interval                       = Messintervall in Sekunden: 
fieldview.windspeed                      = Höhe der \nWindgeschwindigkeitsmessung \nin Meter: 
fieldview.dateformat                     = Datumsformat: 
fieldview.dateformatexample              = Beispiel: d.M.y
fieldview.localeid                       = Zahlenformat gemäß: 
fieldview.filepath                       = Dateipfad der \nWetterdaten-CSV-Datei: 
fieldview.filechooserbutton              = Datei auswählen 
fieldview.filechooser                    = Dateipfad zur Wetterdatenquelle 
fieldview.locationlat                    = Latitude (dezimal): 
fieldview.locationlatexample             = Beispiel: 51.869026 
fieldview.locationlng                    = Longitude (dezimal): 
fieldview.locationlngexample             = Beispiel: 8.917478 
fieldview.metersabove                    = Standort der Wetterdatenquelle \n(Meter über NN): 
fieldview.dateerror                      = Falsches Format! 

private TreeMap<String, String> javaLocaleMap;

private void fillJavaLocaleMap(final Locale selectedLocale) {
  javaLocaleMap = new TreeMap<String, String>();
  java.lang.reflect.Field[] fieldArray = Locale.class.getFields();
  for (int i = 0; i < fieldArray.length; i++) {
    if (fieldArray[i].getType().equals(Locale.class)) {
      String language;
      try {
        language = ((Locale) fieldArray[i].get(null)).getDisplayLanguage(selectedLocale);
      } catch (Exception e) {
        language = null;
      }
      if (language != null && language.length() > 0) {
        final String fieldName = fieldArray[i].getName();
        javaLocaleMap.put(language + " (" + fieldName + ")", fieldName);
      }
    }
  }
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
		// FIXME calculate wind speed! siehe Chat
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
	var dateFormat = newDateFormat(pluginConfig.dateFormat);
	var numberFormat = newNumberFormat(pluginConfig.numberFormat);
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
