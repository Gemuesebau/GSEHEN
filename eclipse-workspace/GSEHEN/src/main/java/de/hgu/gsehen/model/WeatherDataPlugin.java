package de.hgu.gsehen.model;

import de.hgu.gsehen.evapotranspiration.DayData;
import java.util.Date;
import java.util.List;

public interface WeatherDataPlugin {
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
