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
				example.setFont(
						javafx.scene.text.Font.font("Arial",
								javafx.scene.text.FontPosture.ITALIC, 12));
				setGridConstraints(example, 2, itemIndex);
			}
			itemIndex++;
		}
	};
	var getLocaleIds = function() {
		return [ "de", "en", "fr" ];
	}
	var WeatherDataPlugin = Java.extend(Java.type("de.hgu.gsehen.model.WeatherDataPlugin"), {
		createAndFillSpecificControls: function(json, configElements, gsehenInstance,
				gsehenGuiElements, fixedItemsCount, fixedNodesCount, classLoader, selLocale) {
			print("## csvImporter.js ## createAndFillSpecificControls");
			var msgBundle = java.util.ResourceBundle.getBundle(
					"de.hgu.gsehen.js.plugins.csvImporter_i18n", selLocale, classLoader);
			var specificConfigItems = new java.util.ArrayList();
			/*var interval = */new Packages.de.hgu.gsehen.gui.view.ConfigDialogDoubleField(
					gsehenGuiElements.text(msgBundle.getString("interval")),
					null, specificConfigItems, gsehenInstance);
			/*var windspeed = */new Packages.de.hgu.gsehen.gui.view.ConfigDialogDoubleField(
					gsehenGuiElements.text(msgBundle.getString("windspeed")),
					null, specificConfigItems, gsehenInstance);
			/*var dateformat = */new Packages.de.hgu.gsehen.gui.view.ConfigDialogStringField(//TODO validate?
					gsehenGuiElements.text(msgBundle.getString("dateformat")),
					gsehenGuiElements.text(msgBundle.getString("dateformatexample")), specificConfigItems, gsehenInstance);
			/*var localeid = */new Packages.de.hgu.gsehen.gui.view.ConfigDialogComboBox(
					gsehenGuiElements.text(msgBundle.getString("localeid")),
					gsehenGuiElements.comboBox(getLocaleIds()), null,
					specificConfigItems, function(event) { print(event); });
			/*var filepath = */new Packages.de.hgu.gsehen.gui.view.ConfigDialogStringField(
					gsehenGuiElements.text(msgBundle.getString("filepath")),
					gsehenGuiElements.text(msgBundle.getString("filepathexample")), specificConfigItems, gsehenInstance);
			/*var filechooserbutton = Datei auswählen
			/*var filechooser = Dateipfad zur Wetterdatenquelle
			/*var dateerror = Falsches Format!*/
			addConfigItems(configElements, specificConfigItems, fixedItemsCount);
		},
		getSpecificConfigurationJSON: function() {
			print("## csvImporter.js ## getSpecificConfigurationJSON");
			return "";
		}
	});
	return new WeatherDataPlugin();
}
