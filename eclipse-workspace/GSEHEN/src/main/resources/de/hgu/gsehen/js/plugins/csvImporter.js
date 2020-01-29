loadLocalJavaScript("lib/arrayUtilities.js");

function getPlugin() {
	var calculateWindspeed2m = function(windspeed, windspeedMeasHeightMeters) {
		return Packages.de.hgu.gsehen.evapotranspiration.UtilityFunctions.convertWindSpeed2m(
			windspeed,
			windspeedMeasHeightMeters
		);
	};
	var calculateDayDataForOneDay = function(pluginConfig, dayDate, weatherDataArray) {
		var dayData = new (Java.type("de.hgu.gsehen.evapotranspiration.DayData"))();
		dayData.setDate(dayDate); // FIXME
		dayData.setTempMax(arrayUtilities.objArrayMax(weatherDataArray, "temp"));
		dayData.setTempMin(arrayUtilities.objArrayMin(weatherDataArray, "temp"));
		dayData.setTempMean(arrayUtilities.objArrayMean(weatherDataArray, "temp"));
		dayData.setAirHumidityRelMax(arrayUtilities.objArrayMax(weatherDataArray, "airHumidityRel"));
		dayData.setAirHumidityRelMin(arrayUtilities.objArrayMin(weatherDataArray, "airHumidityRel"));
		dayData.setAirHumidityRelMean(arrayUtilities.objArrayMean(weatherDataArray, "airHumidityRel"));
		dayData.setGlobalRad(arrayUtilities.objArraySum(weatherDataArray, "globalRad") * 0.0864 / ((60/(pluginConfig.measIntervalSeconds / 60))*24));
		/* 1/1000000 vs 0.0864*Glob/144 */
		dayData.setPrecipitation(arrayUtilities.objArraySum(weatherDataArray, "precipitation"));
		dayData.setWindspeed2m(
			calculateWindspeed2m(arrayUtilities.objArrayMean(weatherDataArray, "windspeed"), pluginConfig.windspeedMeasHeightMeters)
		);
		return dayData;
	};
	var processWeatherDataForOneDay = function(currentWeatherDataForOneDay, completeDayData, pluginConfig) {
		if (currentWeatherDataForOneDay.length > 0) {
			completeDayData.add(calculateDayDataForOneDay(pluginConfig, currentWeatherDataForOneDay[0].lineDate, currentWeatherDataForOneDay));
		}
	};
	var calculateDayData = function(pluginConfig, date /* currently unused */, weatherDataArray) {
		var completeDayData = new java.util.ArrayList();
		var lastLineDayStamp = -1;
		var currentWeatherDataForOneDay = [];
		arrayUtilities.iterateArray(weatherDataArray, function (weatherDataLine) {
			var lineDayStamp = 0 + weatherDataLine.lineDate.getTime();
			if (lineDayStamp != lastLineDayStamp) {
				processWeatherDataForOneDay(currentWeatherDataForOneDay, completeDayData, pluginConfig);
				lastLineDayStamp = lineDayStamp;
				currentWeatherDataForOneDay = [];
			}
			currentWeatherDataForOneDay.push(weatherDataLine);
		});
		processWeatherDataForOneDay(currentWeatherDataForOneDay, completeDayData, pluginConfig);
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
	var newTransformableColumnData = function(pluginConfig) {
		return new de.hgu.gsehen.util.TransformableColumnData(
                new de.hgu.gsehen.util.ColumnDataText(
                    pluginConfig.separatorChar,
                    pluginConfig.quoteChar,
                    pluginConfig.quotedRegExp,
                    pluginConfig.quotedReplaceJS
                )
            );
    };
	var importWeatherData = function(date /* currently unused */, pluginConfig, stack, withExceptions) {
		var columnData = newTransformableColumnData(pluginConfig);
		var columnDefProps = [
			  "coldefdatetime",
			  "coldeftemperature",
			  "coldefairhumidity",
			  "coldeftimeduration",
			  "coldefwindspeed",
			  "coldefglobalrad",
			  "coldefprecipitation"
		];
		arrayUtilities.iterateArray(columnDefProps, function (columnDefProp) {
			var colDefString = pluginConfig[columnDefProp];
			// 7, v * 1000
			columnData.addColumnDefinition(colDef, "datetime", "Date", dateParser("d.M.y H:m:s"), null,
	        dateFormatter("dd.MM.yyyy, HH:mm:ss"));
		});
		
		// FIXME parse user-provided column definition strings, add & process (see below);
		//  two cases: preview (like below); after that, actual import (see TransformableTypedColumnData.main)
	    
	    columnData.addColumnDefinition(6, "batterymV", "Double", doubleParser("GERMAN"), v -> 1000 * v,
	        doubleFormatter(Locale.forLanguageTag("de")));
	    columnData.processAsRows(
	        "",
	        "utf-8",
	        15,
	        a -> System.out.println(Arrays.asList(a)),
	        (i, l) -> l.get(0).length() > 0 && Character.isLetter(l.get(0).charAt(0))
	    );


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
				var lineDate = dateFormat.parse(line.replace(/^"/, "").replace(/ .*/, ""));
				var weatherDataMeasurementObject = arrayUtilities.arrayToObject(
					processNumberColumns(line.split(/; */), numberFormat),
					getWeatherDataAttributeNamesArray()
				);
				if (withExceptions) {
					weatherDataMeasurementObject.lineNumber = lineNumber;
				}
				weatherDataMeasurementObject.lineDate = lineDate;
				weatherDataArray.push(weatherDataMeasurementObject);
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
		getConfigValue: function(item) {
			var configObjectTransform = this.configObjectTransforms[item];
			var configValue = this.guiControls[item].getNodeValue();
			return configObjectTransform == null ? configValue : configObjectTransform(configValue);
		},
		getConfigObject: function() {
			var result = {};
			for (var dataKey in this.itemsByDataKeys) {
				result[dataKey] = this.getConfigValue(this.itemsByDataKeys[dataKey]);
			}
			java.lang.System.out.println(JSON.stringify(result, null, 2));
			return result;
		},
		createGuiControl: function(item, type, hasExample, itemObjectsList, data, dataKey, transform, configObjectTransform, comboBox) {
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
			this.itemsByDataKeys[dataKey] = item;
			if (configObjectTransform != null) {
				this.configObjectTransforms[item] = configObjectTransform;
			}
		},
		gsehenGui: null,
		guiControls: null,
		itemsByDataKeys: null,
		configObjectTransforms: null,
		gsehenInstance: null,
		msgBundle: null,
		parentStackPane: null,
		//-----------
		/*@Override*/createAndFillSpecificControls: function(json, configurator) {
			this.gsehenGui = Packages.de.hgu.gsehen.gui.GsehenGuiElements;
			this.guiControls = {
				interval: null,
				windspeed: null,
				dateformat: null,
				localeid: null,
				filepath: null
			};
			this.itemsByDataKeys = {};
			this.configObjectTransforms = {};
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
					function(s) { return configurator.getJavaLocaleMap().get(s); },
					this.gsehenGui.comboBox(getLocaleDisplay(configurator.getJavaLocaleMap())));
			this.createGuiControl("filepath", "StringField", true, specificConfigItems, data, "dataFilePath");
			this.createGuiControl("charset", "StringField", true, specificConfigItems, data, "dataFileCharset");
			this.createGuiControl("separatorchar", "StringField", true, specificConfigItems, data, "separatorChar");
			this.createGuiControl("quotechar", "StringField", true, specificConfigItems, data, "quoteChar");
			this.createGuiControl("quotedregexp", "StringField", true, specificConfigItems, data, "quotedRegExp");
			this.createGuiControl("quotedreplacejs", "StringField", true, specificConfigItems, data, "quotedReplaceJS");
			this.createGuiControl("headlinejs", "StringField", true, specificConfigItems, data, "headlineJS");
			this.createGuiControl("coldefdatetime", "StringField", true, specificConfigItems, data, "coldefdatetime");
			this.createGuiControl("coldeftemperature", "StringField", false, specificConfigItems, data, "coldeftemperature");
			this.createGuiControl("coldefairhumidity", "StringField", false, specificConfigItems, data, "coldefairhumidity");
			this.createGuiControl("coldeftimeduration", "StringField", false, specificConfigItems, data, "coldeftimeduration");
			this.createGuiControl("coldefwindspeed", "StringField", false, specificConfigItems, data, "coldefwindspeed");
			this.createGuiControl("coldefglobalrad", "StringField", false, specificConfigItems, data, "coldefglobalrad");
			this.createGuiControl("coldefprecipitation", "StringField", true, specificConfigItems, data, "coldefprecipitation");

/*


    AggregatedDataObjects<DayData> objects =
        new AggregatedDataObjects<>(
            newTransformableColumnData(pluginConfig)
        );
    objects.addColumnDefinition(0, "datetime", "Date", dateParser(dateformat), null,
        dateFormatter("dd.MM.yyyy, HH:mm:ss"), // ??
        dtList -> dtList.get(dtList.size() - 1), (dd, dt) -> dd.setDate(dt));
    objects.addColumnDefinition(6, "batterymV", "Double", doubleParser("GERMAN"), v -> 1000 * v,
        doubleFormatter(Locale.forLanguageTag("de")),
        doubleMean(), (dd, d) -> dd.setBatteryMean(d));
    objects.process(
        "GSEHENWetter.csv",
        charset,
        -1,
        headlinejs,
        (last, current) -> {
          return !DateUtil.sameDay(
              (Date)current.getValue("datetime"), (Date)last.getValue("datetime"));
        },
        () -> new DayData(),
        d -> System.out.println(d.getDate()) // hier, oder gleich oben, das neue DayData-Objekt in die Liste!
    );


*/

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
			try {
				return JSON.stringify(this.getConfigObject());
			}
			catch (e) {
				java.lang.System.err.println(e);
				java.lang.System.err.println(e.message);
			}
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
