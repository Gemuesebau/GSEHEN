loadLocalJavaScript("lib/arrayUtilities.js");

function getPlugin() {
	var calculateWindspeed2m = function(windspeed, windspeedMeasHeightMeters) {
		return Packages.de.hgu.gsehen.evapotranspiration.UtilityFunctions.convertWindSpeed2m(
			windspeed,
			windspeedMeasHeightMeters
		);
	};
	var calculateDayData = function(pluginConfig, date /* currently unused */) {
		var completeDayData = new java.util.ArrayList();
	    newImporter(pluginConfig, null, true).process(
	        pluginConfig.dataFilePath,
	        pluginConfig.dataFileCharset,
	        -1,
	        new Function("i,l", "return " + pluginConfig.headlineJS),
	        function(last, current) {
	        	return !Packages.de.hgu.gsehen.util.DateUtil.sameDay(
	        			current.getValue("dateTimeDefinition"), last.getValue("dateTimeDefinition"));
	        },
	        function() {
	        	return new (Java.type("de.hgu.gsehen.evapotranspiration.DayData"))();
	        },
	        function(dayData) {
	        	completeDayData.add(dayData);
	        }
	    );
		return completeDayData;
	};
	var ColumnData = Packages.de.hgu.gsehen.util.TransformableColumnData;
	var AggregatedData = Packages.de.hgu.gsehen.util.AggregatedDataObjects;
	var newTransformableColumnData = function(pluginConfig) {
		return new ColumnData(
            new Packages.de.hgu.gsehen.util.ColumnDataText(
                pluginConfig.separatorChar,
                pluginConfig.quoteChar,
                pluginConfig.quotedRegExp,
                new Function("m", "return " + pluginConfig.quotedReplaceJS)
            )
        );
	};
	var newAggregatedDataObjects = function(pluginConfig) {
		return new AggregatedData(
            newTransformableColumnData(pluginConfig)
        );
	};
	var newImporter = function(pluginConfig, msgBundle, aggregated) {
		var importer = aggregated ?
				newAggregatedDataObjects(pluginConfig) :
				newTransformableColumnData(pluginConfig);
		var columnDefProps = [
			"dateTimeDefinition",
			"temperatureDefinition",
			"airhumidityDefinition",
			"timeDurationDefinition",
			"windspeedDefinition",
			"globalRadDefinition",
			"precipitationDefinition"
		];
		var columnTypes = [
			"Date",
			"Double",
			"Double",
			"Double",
			"Double",
			"Double",
			"Double"
		];
		var columnParsers = [
			ColumnData.dateParser(pluginConfig.dateFormatString),
			ColumnData.doubleParser(pluginConfig.numberFormat),
			ColumnData.doubleParser(pluginConfig.numberFormat),
			ColumnData.doubleParser(pluginConfig.numberFormat),
			ColumnData.doubleParser(pluginConfig.numberFormat),
			ColumnData.doubleParser(pluginConfig.numberFormat),
			ColumnData.doubleParser(pluginConfig.numberFormat)
		];
		var previewOutputDoubleFormat = aggregated ?
				null :
				msgBundle.getString("importpreviewoutput.doubleformat");
		var columnFormatters = aggregated ?
				null :
				[
					ColumnData.dateFormatter(msgBundle.getString("importpreviewoutput.dateformat")),
					ColumnData.doubleFormatter(previewOutputDoubleFormat),
					ColumnData.doubleFormatter(previewOutputDoubleFormat),
					ColumnData.doubleFormatter(previewOutputDoubleFormat),
					ColumnData.doubleFormatter(previewOutputDoubleFormat),
					ColumnData.doubleFormatter(previewOutputDoubleFormat),
					ColumnData.doubleFormatter(previewOutputDoubleFormat)
				];
		var columnAggregators = aggregated ?
				[
					function(dtList) {
						return dtList.get(dtList.size() - 1);
					},
					function(tmList) {
						return {
							max:  AggregatedData.doubleMax().apply(tmList),
							min:  AggregatedData.doubleMin().apply(tmList),
							mean: AggregatedData.doubleMean().apply(tmList)
						};
					},
					function(ahList) {
						return {
							max:  AggregatedData.doubleMax().apply(ahList),
							min:  AggregatedData.doubleMin().apply(ahList),
							mean: AggregatedData.doubleMean().apply(ahList)
						};
					},
					function(tdList) {
						return 0;
					},
					function(wsList) {
						return calculateWindspeed2m(AggregatedData.doubleMean().apply(wsList), pluginConfig.windspeedMeasHeightMeters);
					},
					function(grList) {
						return AggregatedData.doubleSum().apply(grList) * 1/1000000 * pluginConfig.measIntervalSeconds;
					},
					AggregatedData.doubleSum()
				] :
				null;
		var columnSetters = aggregated ?
				[
					function(dd, dt) {
						dd.setDate(dt);
					},
					function(dd, tmMMMObj) {
						dd.setTempMax(tmMMMObj.max);
						dd.setTempMin(tmMMMObj.min);
						dd.setTempMean(tmMMMObj.mean);
					},
					function(dd, ahMMMObj) {
						dd.setAirHumidityRelMax(ahMMMObj.max);
						dd.setAirHumidityRelMin(ahMMMObj.min);
						dd.setAirHumidityRelMean(ahMMMObj.mean);
					},
					function(dd, td) {
					},
					function(dd, ws) {
						dd.setWindspeed2m(ws);
					},
					function(dd, gr) {
						dd.setGlobalRad(gr);
					},
					function(dd, pr) {
						dd.setPrecipitation(pr);
					}
				] :
				null;
		var columnDefParser = java.util.regex.Pattern.compile("\\s*(\\d+)\\s*(,\\s*(.*))?");
		function stringEmpty(str) {
			return str == null || str == "";
		}
		function getTransformer(columnDefMatcher) {
			if (stringEmpty(columnDefMatcher.group(2))) {
				return null;
			}
			return new Function("v", "return " + columnDefMatcher.group(3));
		}
		var maxCSVColIndex = -1;
		for (var i=0; i<columnDefProps.length; i++) {
			var columnDefProp = columnDefProps[i];
			var colDefString = pluginConfig[columnDefProp];
			var colDefMatcher = columnDefParser.matcher(colDefString);
			if (colDefMatcher.matches()) {
				var colIndex = java.lang.Integer.parseInt(colDefMatcher.group(1));
				if (colIndex > maxCSVColIndex) {
					maxCSVColIndex = colIndex;
				}
				if (aggregated) {
					importer.addColumnDefinition(colIndex,
						columnDefProp, columnTypes[i], columnParsers[i],
						getTransformer(colDefMatcher), null, columnAggregators[i],
						columnSetters[i]);
				}
				else {
					importer.addColumnDefinition(colIndex,
						columnDefProp, columnTypes[i], columnParsers[i],
						getTransformer(colDefMatcher), columnFormatters[i]);
				}
			}
		}
		return aggregated ? importer : { columnData: importer, maxCSVColIndex: maxCSVColIndex };
    };
	var importWeatherData = function(date /* currently unused */, pluginConfig, rowHandler, columnData) {
	    columnData.processAsRows(
	        pluginConfig.dataFilePath,
	        pluginConfig.dataFileCharset,
	        15,
	        rowHandler,
	        new Function("i,l", "return " + pluginConfig.headlineJS)
	        // Java example: (i, l) -> l.get(0).length() > 0 && Character.isLetter(l.get(0).charAt(0))
	    );
	};
	var createTableView = function(csvWidth, msgKeys, msgBundle) {
	    var tableView = new javafx.scene.control.TableView();
	    var tableViewColumns = tableView.getColumns();
	    for (var i=0; i<csvWidth; i++) {
		    var column = new javafx.scene.control.TableColumn(
		      msgBundle.getString("tableviewcolumnname." + msgKeys[i]));
		    tableViewColumns.add(column);
		    column.setMinWidth(20);
		    column.setStyle("-fx-alignment:top-center; -fx-font-style: italic");
		    column.setUserData(i);
		    column.setCellValueFactory(function(/*CellDataFeatures<String[], String>*/ p) {
		    	return new javafx.beans.property.SimpleStringProperty(p.getValue()[p.getTableColumn().getUserData()]);
		    });
	    }
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
			return calculateDayData(pluginConfig, date /* currently unused */);
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
			this.guiControls = {};
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
			this.createGuiControl("coldefdatetime", "StringField", true, specificConfigItems, data, "dateTimeDefinition");
			this.createGuiControl("coldeftemperature", "StringField", false, specificConfigItems, data, "temperatureDefinition");
			this.createGuiControl("coldefairhumidity", "StringField", false, specificConfigItems, data, "airhumidityDefinition");
			this.createGuiControl("coldeftimeduration", "StringField", false, specificConfigItems, data, "timeDurationDefinition");
			this.createGuiControl("coldefwindspeed", "StringField", false, specificConfigItems, data, "windspeedDefinition");
			this.createGuiControl("coldefglobalrad", "StringField", false, specificConfigItems, data, "globalRadDefinition");
			this.createGuiControl("coldefprecipitation", "StringField", true, specificConfigItems, data, "precipitationDefinition");
			var weatherDataPlugin = this;
			new Packages.de.hgu.gsehen.gui.view.ConfigDialogActionButton(
					this.msgBundle.getString("importtest"), specificConfigItems,
					function(event) {
						weatherDataPlugin.showImportPreview();
					}
			);
			addConfigItems(configurator.getConfigNodes(), specificConfigItems, configurator.getFixedItemsCount());
		},
		//-----------
		/*@Override*/getSpecificConfigurationJSON: function() {
			return JSON.stringify(this.getConfigObject());
		},
		showImportPreview: function() {
		    var content = new com.jfoenix.controls.JFXDialogLayout();
		    content.setHeading(this.gsehenGui.text(this.msgBundle.getString("importpreview")));
		    var dialog = new com.jfoenix.controls.JFXDialog(
		    	this.parentStackPane,
		    	content,
		    	com.jfoenix.controls.JFXDialog.DialogTransition.CENTER
		    );
		    dialog.show();
		    var pluginConfig = this.getConfigObject();
		    var columnDataHolderObj = newImporter(pluginConfig, this.msgBundle);
		    var csvWidth = columnDataHolderObj.maxCSVColIndex + 1;
		    var columnData = columnDataHolderObj.columnData;
		    var previewTable = createTableView(csvWidth, columnData.getKeysRow(csvWidth), this.msgBundle);
		    var previewTableItems = previewTable.getItems();
			importWeatherData(
				Packages.de.hgu.gsehen.util.DateUtil.truncToDay(new java.util.Date()),
				pluginConfig,
				function(strArr) {
					previewTableItems.add(strArr);
				},
				columnData
			);
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
