package de.hgu.gsehen.model;

import de.hgu.gsehen.evapotranspiration.DayData;
import java.util.Date;
import java.util.List;

/**
 * Interface to be implemented by weather data (import) plug-ins.
 *
 * <p>Typical JavaScript code to implement this interface:</p>
 * <code>
 * // in "myPlugin.js"<br>
 * // ...<br>
 * function getPlugin() {<br>
 * &nbsp;&nbsp;// ...<br>
 * &nbsp;&nbsp;var WDPlugin = Java.extend(Java.type("de.hgu.gsehen.model.WeatherDataPlugin"), {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;// ...<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;determineDayData: function(weatherDataSource, date) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;},<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;// ...<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;createAndFillSpecificControls: function(json, configurator) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;},<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;// ...<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;getSpecificConfigurationJSON: function() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;// ...<br>
 * &nbsp;&nbsp;});<br>
 * &nbsp;&nbsp;return new WDPlugin();<br>
 * }
 * </code>
 * <p>The lifecycle is: for configuration of a weather data source, the application constructs an
 * instance of WeatherDataPlugin by calling an actual plug-in JavaScript file's
 * <code>getPlugin</code> function. On the obtained instance, typically the configuration-related
 * methods are called as follows:
 * <ol>
 * <li>{@link de.hgu.gsehen.model.WeatherDataPlugin#createAndFillSpecificControls(String json,
 *   WeatherDataConfigurator)}
 * <li>{@link de.hgu.gsehen.model.WeatherDataPlugin#getSpecificConfigurationJSON()}
 * </ol></p>
 * <p>In between, the application users may use a GUI dialog to configure the weather data source
 * using this plug-in.</p>
 * <p>The same, OR A NEW, instance of this plug-in, will be used to perform the actual weather data
 * import, using the following method:</p>
 * {@link de.hgu.gsehen.model.WeatherDataPlugin#determineDayData(WeatherDataSource, Date)}
 */
public interface WeatherDataPlugin {
  /**
   * Checks for new data, and returns the next check date.
   *
   * <p>This method may be called on a new instance of the plug-in, and should thus behave like a
   * <i>static</i> method!</p>
   *
   * @param weatherDataSource the particular weather data source to use
   * @return the next check date, or null if there's new data that should be imported
   */
  Double getNextCheckMillis(WeatherDataSource weatherDataSource);

  /**
   * Determines the (weather) day data for the available measurement data.
   *
   * <p>This method may be called on a new instance of the plug-in, and should thus behave like a
   * <i>static</i> method!</p>
   *
   * @param weatherDataSource the particular weather data source to use
   * @param todayDate today's date, for warnings concerning missing day measurement data etc.
   * @return a list of DayData objects for all found measurement dates
   */
  List<DayData> determineDayData(WeatherDataSource weatherDataSource, Date todayDate);

  /**
   * Adds GUI controls to the given node list, after the fixed GUI nodes,
   * and fills them according to the given JSON data.
   *
   * <p>This method may add GUI controls for data import preview purposes.</p>
   *
   * <p>This method may save any GUI control references for later! (e.g., for error messages)</p>
   *
   * <p>Mind the difference between nodes and items; particularly, fixedNodesCount is equal to,
   * <i>or greater than,</i> fixedItemsCount.</p>
   *
   * @param json a JSON data string containing the values for the specific controls, e.g. from DB
   * @param configurator the (GUI) component configuring weather data sources and plug-ins
   */
  void createAndFillSpecificControls(String json, WeatherDataConfigurator configurator);

  /**
   * Returns the values currently contained in this plugin's own controls.
   *
   * <p>This method may add error messages to the GUI!</p>
   *
   * @see createAndFillSpecificControls
   * @return the control values as a JSON string
   */
  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  String getSpecificConfigurationJSON();
}
