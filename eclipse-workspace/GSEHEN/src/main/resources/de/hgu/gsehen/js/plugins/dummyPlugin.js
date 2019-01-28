function getPlugin() {
	var WeatherDataPlugin = Java.extend(Java.type("de.hgu.gsehen.model.WeatherDataPlugin"), {
		createAndFillSpecificControls: function(json, configElementsParent, fixedNodesCount) {
		    print("## dummyPlugin.js ## createAndFillSpecificControls");
		},
		getSpecificConfigurationJSON: function() {
		    print("## dummyPlugin.js ## getSpecificConfigurationJSON");
			return "";
		}
	});
	return new WeatherDataPlugin();
}
