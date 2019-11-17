package de.hgu.gsehen.model;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.function.Consumer;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public interface WeatherDataPlugin {
  /**
   * Determines the (weather) day data for the available measurement data.
   *
   * @param weatherDataSource the particular weather data source to use
   * @param todayDate today's date, for warnings concerning missing day measurement data etc.
   * @param beforeImport logic to execute before importing from the given weather data source
   * @return a list of DayData objects for all found measurement dates
   */
  List<DayData> determineDayData(WeatherDataSource weatherDataSource, Date todayDate,
      Consumer<WeatherDataSource> beforeImport);

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
   * @param configNodes a GUI node list where new controls are to be added
   * @param fixedNodesCount the number of GUI nodes in the given list to be left untouched
   * @param fixedItemsCount the number of leading configuration items to be left untouched
   * @param gsehenInstance the application instance
   * @param classLoader the class loader to be used for, e.g., resource bundle lookup
   * @param locale the currently selected UI locale
   * @param javaLocaleMap a dictionary for available Java locale values (described in "locale")
   * @param parentStackPane a StackPane to be used for, e.g., (modal) dialogs
   * @param errorSetter a String-consuming function that shows the given error message
   * @param resetter a function that resets the (error-holding) GUI part
   */
  void createAndFillSpecificControls(String json, ObservableList<Node> configNodes,
      int fixedNodesCount, int fixedItemsCount, Gsehen gsehenInstance,
      ClassLoader classLoader, Locale locale,
      TreeMap<String, String> javaLocaleMap, StackPane parentStackPane,
      Consumer<String> errorSetter, Runnable resetter);

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
