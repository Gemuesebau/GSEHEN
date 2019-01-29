function getPlugin() {
	var setGridConstraints = function(element, columnIndex, nodeIndex) {
		javafx.scene.layout.GridPane.setConstraints(element, columnIndex, nodeIndex);
	};
	var addConfigItems = function(configElements, specificConfigItems, fixedItemsCount) {
		var itemIndex = fixedItemsCount;
		var specificConfigItemsCount = specificConfigItems.size();
		for (var i=0; i<specificConfigItemsCount; i++) {
			var item = specificConfigItems.get(i);
			var label = item.getLabel();
			configElements.add(label);
	        setGridConstraints(label, 0, itemIndex);
	        var node = item.getNode();
	        configElements.add(node);
	        setGridConstraints(node, 1, itemIndex);
			var example = item.getExample();
			if (example != null) {
				configElements.add(example);
				example.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontPosture.ITALIC, 12));
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
		javaLocaleMap: null,
		interval: null,
		windspeed: null,
		dateformat: null,
		localeid: null,
		filepath: null,
		/*filechooserbutton: null,
		/*filechooser: null,
		/*dateerror: null*/
		createAndFillSpecificControls: function(json, configElements, gsehenInstance, gsehenGui,
				fixedItemsCount, fixedNodesCount, classLoader, selLocale, javaLocaleMapParam) {
			javaLocaleMap = javaLocaleMapParam;
			var data = JSON.parse(json);
			var msgBundle = java.util.ResourceBundle.getBundle(
					"de.hgu.gsehen.js.plugins.csvImporter_i18n", selLocale, classLoader);
			var specificConfigItems = new java.util.ArrayList();
			interval = new Packages.de.hgu.gsehen.gui.view.ConfigDialogDoubleField(
					gsehenGui.text(msgBundle.getString("interval")),
					null, specificConfigItems, gsehenInstance);
			if (data != null && data.measIntervalSeconds != null) {
				interval.setNodeValue(data.measIntervalSeconds.doubleValue());
			}
			windspeed = new Packages.de.hgu.gsehen.gui.view.ConfigDialogDoubleField(
					gsehenGui.text(msgBundle.getString("windspeed")),
					null, specificConfigItems, gsehenInstance);
			if (data != null && data.windspeedMeasHeightMeters != null) {
				windspeed.setNodeValue(data.windspeedMeasHeightMeters.doubleValue());
			}
			dateformat = new Packages.de.hgu.gsehen.gui.view.ConfigDialogStringField( // TODO validate against new SimpleDateFormat? .. dateerror
					gsehenGui.text(msgBundle.getString("dateformat")),
					gsehenGui.text(msgBundle.getString("dateformatexample")), specificConfigItems, gsehenInstance);
			if (data != null && data.dateFormatString != null) {
				dateformat.setNodeValue(data.dateFormatString);
			}
			localeid = new Packages.de.hgu.gsehen.gui.view.ConfigDialogComboBox(
					gsehenGui.text(msgBundle.getString("localeid")),
					gsehenGui.comboBox(getLocaleDisplay(javaLocaleMap)), null,
					specificConfigItems, function(event) { print(event); });
			if (data != null && data.numberFormat != null) {
				localeid.setNodeValue(reverseLookup(data.numberFormat, javaLocaleMap));
			}
			filepath = new Packages.de.hgu.gsehen.gui.view.ConfigDialogStringField( // TODO validate against new File?
					gsehenGui.text(msgBundle.getString("filepath")),
					gsehenGui.text(msgBundle.getString("filepathexample")), specificConfigItems, gsehenInstance);
			if (data != null && data.dataFilePath != null) {
				filepath.setNodeValue(data.dataFilePath);
			}
			/*filechooserbutton//Datei auswählen
			/*filechooser//Dateipfad zur Wetterdatenquelle
			/*dateerror//Falsches Format!*/
			addConfigItems(configElements, specificConfigItems, fixedItemsCount);
		},
		getSpecificConfigurationJSON: function() {
			return JSON.stringify({
				measIntervalSeconds: interval.getNodeValue(),              // 60
				windspeedMeasHeightMeters: windspeed.getNodeValue(),       // 2
				dateFormatString: dateformat.getNodeValue(),               // "y-M-d"
				numberFormat: javaLocaleMap.get(localeid.getNodeValue()),  // "Deutsch (GERMAN)"
				dataFilePath: filepath.getNodeValue()                      // "C:\\Daten\\10MinDaten.csv"
			});
		}
	});
	return new WeatherDataPlugin();
}
