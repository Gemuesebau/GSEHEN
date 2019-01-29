package de.hgu.gsehen.model;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GsehenGuiElements;
import java.util.Locale;
import java.util.TreeMap;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public interface WeatherDataPlugin {

  /**
   * Adds GUI controls to the given parent, after the fixed GUI nodes,
   * and fills them according to the given JSON data.
   *
   * <p>This method may add GUI controls for data import preview purposes.</p>
   *
   * <p>This method may save any GUI control references for later! (e.g., for error messages)</p>
   *
   * @param json a JSON data string containing the values for the specific controls
   * @param configNodes a GUI node list where new controls are to be added
   * @param gsehenInstance the application instance
   * @param gsehenGuiElements the GUI elements helper
   * @param fixedItemsCount the number of leading configuration items to be left untouched
   * @param fixedNodesCount the number of the given parent's children to be left untouched
   * @param classLoader the class loader to be used for resource bundle lookup
   * @param locale the currently selected UI locale
   * @param javaLocaleMap a dictionary for available Java locale values, described in current locale 
   */
  void createAndFillSpecificControls(String json, ObservableList<Node> configNodes,
      Gsehen gsehenInstance, GsehenGuiElements gsehenGuiElements, int fixedItemsCount,
      int fixedNodesCount, ClassLoader classLoader, Locale locale,
      TreeMap<String, String> javaLocaleMap);

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
