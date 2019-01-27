function getPlugin() {
	var WeatherDataPlugin = Java.extend(Java.type("de.hgu.gsehen.model.WeatherDataPlugin"), {
		createAndFillSpecificControls: function(json, configElementsParent, fixedNodesCount) {
		    print("createAndFillSpecificControls");
		},
		getSpecificConfigurationJSON: function() {
		    print("getSpecificConfigurationJSON");
			return "";
		}
	});
	return new WeatherDataPlugin();
}
