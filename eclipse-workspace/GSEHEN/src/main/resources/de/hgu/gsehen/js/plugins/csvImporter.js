loadLocalJavaScript("lib/arrayUtilities.js");

function getPlugin() {
	var calculateWindspeed2m = function(windspeed, windspeedMeasHeightMeters) {
		return Packages.de.hgu.gsehen.evapotranspiration.UtilityFunctions.convertWindSpeed2m(
			windspeed,
			windspeedMeasHeightMeters
		);
	};
	var incrementDaysCountForLinesCount = function(linesCount, statistics) {
		var daysCount = statistics[linesCount];
		statistics[linesCount] = daysCount == null ? 1 : daysCount + 1;
	};
	var logStatistics = function(statistics) {
		for (var linesCount in statistics) {
			LOGGER.log(java.util.logging.Level.CONFIG, "Found " + statistics[linesCount] + " days with " + linesCount + " data lines");
		}
		java.lang.Thread.sleep(3000);//DEBUG
	};
	var logObject = function(message, obj) {
		var str = message + "{";
		for (var attr in obj) {
			str += ("\n  \"" + attr + "\": " + (typeof obj[attr] == "number" ? obj[attr] : "\"" + obj[attr] + "\""));
		}
		str += ("\n}");
		LOGGER.log(java.util.logging.Level.CONFIG, str);
	};
	var calculateDayDataForOneDay = function(pluginConfig, dayDate, weatherDataArray) {
		// über grouping lösen. einem entspr. Predicate muss außer dem aktuellen auch der letzte Datensatz gegeben werden.
		// muss nach erstem Parse-Schritt sein, aber nicht notwendigerweise mit "Zwischenobjekt" ( = Schritt 2). Dieses kann
		// zugunsten des ZIEL-Objekts (nach Aggregation!) wieder eingespart werden!!
		var dayData = new (Java.type("de.hgu.gsehen.evapotranspiration.DayData"))();
		dayData.setDate(dayDate);
		dayData.setTempMax(arrayUtilities.objArrayMax(weatherDataArray, "temp"));
		dayData.setTempMin(arrayUtilities.objArrayMin(weatherDataArray, "temp"));
		dayData.setTempMean(arrayUtilities.objArrayMean(weatherDataArray, "temp"));
		dayData.setAirHumidityRelMax(arrayUtilities.objArrayMax(weatherDataArray, "airHumidityRel"));
		dayData.setAirHumidityRelMin(arrayUtilities.objArrayMin(weatherDataArray, "airHumidityRel"));
		dayData.setAirHumidityRelMean(arrayUtilities.objArrayMean(weatherDataArray, "airHumidityRel"));
		/* dayData.setGlobalRad(arrayUtilities.objArraySum(weatherDataArray, "globalRad") * pluginConfig.measIntervalSeconds / 1000000); */
		dayData.setGlobalRad(arrayUtilities.objArraySum(weatherDataArray, "globalRad") * 0.0864 / ((60/(pluginConfig.measIntervalSeconds / 60))*24));
		/* 0.0864*Glob/144  --->  das alles hier über Werttransformation?!! pluginConfig reinreichen?! (put, dann exec eval?) */

		dayData.setPrecipitation(arrayUtilities.objArraySum(weatherDataArray, "precipitation"));
		dayData.setWindspeed2m(
			calculateWindspeed2m(arrayUtilities.objArrayMean(weatherDataArray, "windspeed"), pluginConfig.windspeedMeasHeightMeters)
		);
		return dayData;
	};
	var processWeatherDataForOneDay = function(currentWeatherDataForOneDay, completeDayData, pluginConfig, statistics) {
		if (currentWeatherDataForOneDay.length > 0) {
			completeDayData.add(calculateDayDataForOneDay(pluginConfig, currentWeatherDataForOneDay[0].lineDate, currentWeatherDataForOneDay));
			incrementDaysCountForLinesCount(currentWeatherDataForOneDay.length, statistics);
		}
	};
	var calculateDayData = function(pluginConfig, date /* currently unused */, weatherDataArray) {
		var completeDayData = new java.util.ArrayList();
		var lastLineDayStamp = -1;
		var currentWeatherDataForOneDay = [];
		var statistics = {};
		arrayUtilities.iterateArray(weatherDataArray, function (weatherDataLine) {
			//logObject("Processing measurement ", weatherDataLine);
			var lineDayStamp = 0 + weatherDataLine.lineDate.getTime();
			if (lineDayStamp != lastLineDayStamp) {
				processWeatherDataForOneDay(currentWeatherDataForOneDay, completeDayData, pluginConfig, statistics);
				lastLineDayStamp = lineDayStamp;
				currentWeatherDataForOneDay = [];
			}
			currentWeatherDataForOneDay.push(weatherDataLine);
		});
		processWeatherDataForOneDay(currentWeatherDataForOneDay, completeDayData, pluginConfig, statistics);
		logStatistics(statistics);
		return completeDayData;
	};
	var getWeatherDataAttributeNamesArray = function(arr) {
		if (arr == null) {
			arr = [];
		}
		arr.push("dateTimeStr");
		arr.push("temp");
		arr.push("airHumidityRel");
		arr.push("timeDuration");
		arr.push("windspeed");
		arr.push("globalRad");
		arr.push("battery");
		arr.push("precipitation");
		return arr;
	};
	var processNumberColumns = function(lineStringsArray, numberFormat) {
		return arrayUtilities.transformArray(lineStringsArray, 1, function (str) {
			return numberFormat.parse(str).doubleValue();
		});
	};
	var importWeatherData = function(date /* currently unused */, pluginConfig, stack, withExceptions) {
		//var timeStamp = date.getTime();
		var weatherDataArray = stack == null ? [] : stack;
		var lineNumberReader = new java.io.LineNumberReader(new java.io.FileReader(pluginConfig.dataFilePath));
		var line;
		var lineNumber = 0;
		var numberFormat = newNumberFormat(pluginConfig.numberFormat); // now in de.hgu.gsehen.util.TransformableTypedColumnData
		var dateFormat = newDateFormat(pluginConfig.dateFormatString);
		LOGGER.log(java.util.logging.Level.FINE, "dateFormat = " + dateFormat.toPattern());
		while ((line = lineNumberReader.readLine()) != null) {
			lineNumber++;
			try {
				/*

"  hallo \" welt "

substrstwre = \\(.) replacejs = m.group(1)
substrstwre = "" replacejs = '"'

aktuell: lineDate nur tagesgenau, was hier intern nützlich ist, aber nicht ausreicht, um in DayData
den "exakten" Zeitpunkt der letzten berücksichtigten Messwertzeile zu übergeben! GSEH-35
				*/
				var lineDate = dateFormat.parse(line.replace(/^"/, "").replace(/ .*/, ""));
				//LOGGER.log(java.util.logging.Level.FINE, "date = " + date);
				//LOGGER.log(java.util.logging.Level.FINE, "lineDate = " + lineDate);
				//if (lineDate.getTime() >= timeStamp) {
					var weatherDataMeasurementObject = arrayUtilities.arrayToObject(
						processNumberColumns(line.split(/; */), numberFormat),
						getWeatherDataAttributeNamesArray()
					);
					if (withExceptions) {
						weatherDataMeasurementObject.lineNumber = lineNumber;
					}
					weatherDataMeasurementObject.lineDate = lineDate;
					weatherDataArray.push(weatherDataMeasurementObject);
					//logObject("Added measurement ", weatherDataMeasurementObject);
				//}
			}
			catch (e) {
				LOGGER.log(java.util.logging.Level.CONFIG, "" + lineNumber + ": " + e);
				if (!(lineNumber == 1 && e.getClass().getName() == "java.text.ParseException") && withExceptions) {
					weatherDataArray.push({
						"lineNumber": lineNumber,
						"exceptionMsg": typeof e.getMessage == "function" ? e.getMessage() : e.message
					});
				}
			}
		}
		return weatherDataArray;
	};
	var createTableView = function(arrayList, columnKeys, msgBundle) {
	    var tableView = new javafx.scene.control.TableView();
	    var tableViewColumns = tableView.getColumns();
	    for each(var columnKey in columnKeys) {
		    var column = new javafx.scene.control.TableColumn(
		    		msgBundle.getString("tableviewcolumnname." + columnKey));
		    tableViewColumns.add(column);
		    column.setMinWidth(20);
		    column.setStyle("-fx-alignment:top-center; -fx-font-style: italic");
		    column.setCellValueFactory(new javafx.scene.control.cell.MapValueFactory(columnKey));
	    }
	    tableView.getItems().addAll(javafx.collections.FXCollections.observableArrayList(arrayList));
	    return tableView;
	};
	var setGridConstraints = function(element, columnIndex, nodeIndex) {
		javafx.scene.layout.GridPane.setConstraints(element, columnIndex, nodeIndex);
	};
	var addConfigItems = function(configNodes, specificConfigItems, fixedItemsCount) {
		var itemIndex = fixedItemsCount;
		var specificConfigItemsCount = specificConfigItems.size();
		for (var i=0; i<specificConfigItemsCount; i++) {
			var item = specificConfigItems.get(i);
			var label = item.getLabel();
			configNodes.add(label);
	        setGridConstraints(label, 0, itemIndex);
	        var node = item.getNode();
	        configNodes.add(node);
	        setGridConstraints(node, 1, itemIndex);
			var example = item.getExample();
			if (example != null) {
				configNodes.add(example);
				example.setFont(
					javafx.scene.text.Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 12)
				);
				setGridConstraints(example, 2, itemIndex);
			}
			itemIndex++;
		}
	};
	var getLocaleDisplay = function(stringStringMap) {
		var result = [];
		var iterator = stringStringMap.keySet().iterator();
		while (iterator.hasNext()) {
			result.push(iterator.next());
		}
		return result;
	};
	var reverseLookup = function(value, map) {
		var iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			var compareValue = entry.getValue();
			var key = entry.getKey();
			if (value == null) {
				if (compareValue == null) {
					return key;
				}
			}
			else {
				if (value.equals(compareValue)) {
					return key;
				}
			}
		}
		return null;
	};
	var WeatherDataPlugin = Java.extend(Java.type("de.hgu.gsehen.model.WeatherDataPlugin"), {
		//-----------
		/*@Override*/determineDayData: function(weatherDataSource, date /* currently unused */) {
			var pluginConfig = JSON.parse(weatherDataSource.getPluginConfigurationJSON());
			var weatherDataArray = importWeatherData(date /* currently unused */, pluginConfig);
			if (weatherDataArray.length == 0) {
				return null;
			}
			return calculateDayData(pluginConfig, date /* currently unused */, weatherDataArray);
		},
		//----------------------------------------------------------------------------------------
		// TODO! sollte sich darauf stützen, was in den createGuiControl-Aufrufen benannt wurde
		getConfigObject: function() {
			return {
				measIntervalSeconds: this.guiControls.interval.getNodeValue(),                  // 60
				windspeedMeasHeightMeters: this.guiControls.windspeed.getNodeValue(),           // 2
				dateFormatString: this.guiControls.dateformat.getNodeValue(),                   // "y-M-d" ---> see newDateFormat
				numberFormat: this.javaLocaleMap.get(this.guiControls.localeid.getNodeValue()), // "Deutsch (GERMAN)" ---> see newNumberFormat
				dataFilePath: this.guiControls.filepath.getNodeValue()                          // "C:\\Data\\10MinDaten.csv"
			};
		},
		createGuiControl: function(item, type, hasExample, itemObjectsList, data, dataKey, transform, comboBox) {
			var ConfigField = Packages.de.hgu.gsehen.gui.view["ConfigDialog" + type];
			var text = this.gsehenGui.text(this.msgBundle.getString(item));
			var example = hasExample ? this.gsehenGui.text(this.msgBundle.getString(item + "example")) : null;
			if (comboBox != null) {
				this.guiControls[item] = new ConfigField(text, example, itemObjectsList, comboBox, function(event) { });
			}
			else {
				this.guiControls[item] = new ConfigField(text, example, itemObjectsList, this.gsehenInstance);
			}
			if (data != null && data[dataKey] != null) {
				var control = this.guiControls[item];
				var temp = data[dataKey];
				if (transform != null) {
					temp = transform(temp);
				}
				control.setNodeValue(temp);
			}
		},
		gsehenGui: null,
		javaLocaleMap: null,
		guiControls: null,
		gsehenInstance: null,
		msgBundle: null,
		parentStackPane: null,
		/*filechooserbutton: null*/
		//-----------
		/*@Override*/createAndFillSpecificControls: function(json, configurator) {
			this.gsehenGui = Packages.de.hgu.gsehen.gui.GsehenGuiElements;
			this.javaLocaleMap = configurator.getJavaLocaleMap();
			this.guiControls = {
				interval: null,
				windspeed: null,
				dateformat: null,
				localeid: null,
				filepath: null
			};
			this.gsehenInstance = configurator.getInstance();
			this.msgBundle = loadLocalResourceBundle("csvImporter_i18n", configurator.getLocale());
			this.parentStackPane = configurator.getParentStackPane();
			var specificConfigItems = new java.util.ArrayList();
			var data = JSON.parse(json);
			this.createGuiControl("interval", "DoubleField", false, specificConfigItems, data, "measIntervalSeconds",
					function(v) { return v.doubleValue(); });
			this.createGuiControl("windspeed", "DoubleField", false, specificConfigItems, data, "windspeedMeasHeightMeters",
					function(v) { return v.doubleValue(); });
			this.createGuiControl("dateformat", "StringField", true, specificConfigItems, data, "dateFormatString");
			this.createGuiControl("localeid", "ComboBox", false, specificConfigItems, data, "numberFormat",
					function(v) { return reverseLookup(v, configurator.getJavaLocaleMap()); },
					this.gsehenGui.comboBox(getLocaleDisplay(this.javaLocaleMap)));
			this.createGuiControl("filepath", "StringField", true, specificConfigItems, data, "dataFilePath");

			this.createGuiControl("charset", "StringField", true, specificConfigItems, data, "dataFile");
			this.createGuiControl("lineend", "StringField", true, specificConfigItems, data, "dataFile");
			this.createGuiControl("separatorchar", "StringField", true, specificConfigItems, data, "dataFile");
			this.createGuiControl("quotechar", "StringField", true, specificConfigItems, data, "dataFile");
			this.createGuiControl("quotedregexp", "StringField", true, specificConfigItems, data, "dataFile");
			this.createGuiControl("quotedreplacejs", "StringField", true, specificConfigItems, data, "dataFile");
			this.createGuiControl("headlinejs", "StringField", true, specificConfigItems, data, "dataFile");
			arr.push("datetime");     // 0, de.hgu.gsehen.util.DateUtil.
			arr.push("temperature");
			arr.push("airhumidity");
			arr.push("timeduration"); // 3, v*1000
			arr.push("windspeed");
			arr.push("globalrad");
			arr.push("battery");
			arr.push("precipitation");// 7, v/10
			//Spalten + Werttransformation f(s, d, n) = [JavaScript-Ausdruck, der das String-Array "s" verwenden kann, oder die Funktionen d(ate) und n(umber) mit Spaltenindex als Parameter, welche die konfigurierten Formate nutzen]
			// Problem: Zuordnung wird dann nur bei der Wertermittlung genutzt, aber nicht bei der "Auswertung" der Kopfzeile. Diese sollte mindestens in der Preview direkt unter dem fachlichen Spaltennamen (z.B. [tableviewcolumnname.dateTimeStr]) angezeigt werden....
			//---> mit Hinweis auf erwartete Einheit, muss in Preview berücksichtigt werden
			// (d.h., Preview zeigt zwar Zeilennummern, und Spalten wie in der Datei, aber bereits transformierte Daten,
			// anhand Typ des zugeordneten Werts ..)

			
			/*filechooserbutton//Datei auswählen ---> filepath*/
			var weatherDataPlugin = this;
			new Packages.de.hgu.gsehen.gui.view.ConfigDialogActionButton(
					this.msgBundle.getString("importtest"), specificConfigItems,
					function(event) {
						var arrayList = new (
							Java.extend(java.util.ArrayList, Packages.de.hgu.gsehen.model.Stack, {
								push: function(jsObj) {
									arrayList.add(jsObj);
								}
							})
						)();
						importWeatherData(
							Packages.de.hgu.gsehen.util.DateUtil.truncToDay(new java.util.Date()),
							weatherDataPlugin.getConfigObject(),
							arrayList,
							true
						);
						showImportPreview(arrayList);
					}
			);
			addConfigItems(configurator.getConfigNodes(), specificConfigItems, configurator.getFixedItemsCount());
		},
		//-----------
		/*@Override*/getSpecificConfigurationJSON: function() {
			return JSON.stringify(this.getConfigObject());
		},
		showImportPreview: function(arrayList) {
		    var content = new com.jfoenix.controls.JFXDialogLayout();
		    content.setHeading(this.gsehenGui.text(this.msgBundle.getString("importpreview")));
		    var dialog = new com.jfoenix.controls.JFXDialog(
		    	this.parentStackPane,
		    	content,
		    	com.jfoenix.controls.JFXDialog.DialogTransition.CENTER
		    );
		    dialog.show();

		    var columnKeys = [];
		    columnKeys.push("lineNumber");
		    getWeatherDataAttributeNamesArray(columnKeys);
		    columnKeys.push("exceptionMsg");
		    var previewTable = createTableView(arrayList, columnKeys, this.msgBundle);
		    setGridConstraints(previewTable, 0, 0);

		    var closeButton = this.gsehenGui.jfxButton(this.msgBundle.getString("importpreviewclose"));
		    closeButton.setOnAction(function(e) {
		      dialog.close();
		    });
		    setGridConstraints(closeButton, 0, 1);

		    var inputGridPane = new javafx.scene.layout.GridPane();
		    inputGridPane.setHgap(6);
		    inputGridPane.setVgap(6);
		    inputGridPane.getChildren().add(previewTable);
		    inputGridPane.getChildren().add(closeButton);
		    content.setBody(inputGridPane);
		}
	});
	return new WeatherDataPlugin();
}
