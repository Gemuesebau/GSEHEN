package de.hgu.gsehen.model;

import javafx.scene.Parent;

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
   * @param configElementsParent a GUI node where new controls are to be added as children
   * @param fixedNodesCount the number of the given parent's children to be left untouched
   */
  void createAndFillSpecificControls(String json, Parent configElementsParent, int fixedNodesCount);

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
