package de.hgu.gsehen.gui.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import com.jfoenix.controls.JFXTabPane;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GsehenSave;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * The GSEHEN Main-Controller.
 *
 * @author CWI
 */
@SuppressWarnings({ "checkstyle:commentsindentation" })
public class MainController {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
  }

  // Views
  @FXML
  private TitledPane fieldsPane;
  @FXML
  private JFXTabPane tabPane;
  @FXML
  private Tab mapViewTab;
  @FXML
  private Tab fieldViewTab;
  @FXML
  private Tab plotViewTab;
  @FXML
  private Tab logViewTab;
  @FXML
  private Tab contactViewTab;
  @FXML
  private Tab aboutViewTab;
  @FXML
  private WebView contactWebView;
  @FXML
  private WebView aboutWebView;
  @FXML
  private ImageView imageView;

  @FXML
  private void about(ActionEvent a) {
    tabPane.getTabs().clear();
    tabPane.getTabs().add(aboutViewTab);
    WebEngine engine = aboutWebView.getEngine();
    engine.load(
        "https://www.hs-geisenheim.de/forschung/institute/gemuesebau/ueberblick-institut-fuer-gemuesebau/bewaesserung/ble-gsehen/");

    // if the URL does not contain "https://www.hs-geisenheim.de" skip back
    engine.locationProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.contains("https://www.hs-geisenheim.de")) {
        Platform.runLater(() -> {
          engine.load(
              "https://www.hs-geisenheim.de/forschung/institute/gemuesebau/ueberblick-institut-fuer-gemuesebau/bewaesserung/ble-gsehen/");
        });
      }
    });
  }

  @FXML
  private void openPluginsFolder(ActionEvent o) {
    File pluginsFolderObj = new File(System.getProperty("user.home")
        + File.separator + ".gsehenIrrigationManager"
        + File.separator + "plugins");
    pluginsFolderObj.mkdirs();
    try {
      Desktop.getDesktop().open(pluginsFolderObj);
    } catch (IOException e) {
      throw new RuntimeException("Couldn't open plugins folder!", e);
    }
  }

  @FXML
  private void openContactView(ActionEvent o) {
    tabPane.getTabs().clear();
    tabPane.getTabs().add(contactViewTab);
    WebEngine engine = contactWebView.getEngine();
    engine.load("https://www.hs-geisenheim.de/personen/person/231/");

    // if the URL does not contain "https://www.hs-geisenheim.de" skip back
    engine.locationProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.contains("https://www.hs-geisenheim.de")) {
        Platform.runLater(() -> {
          engine.load("https://www.hs-geisenheim.de/personen/person/231/");
        });
      }
    });
  }

  // Returns to Main-Menu.
  @FXML
  private void backToMainView(ActionEvent b) {
    tabPane.getTabs().clear();
    tabPane.getTabs().addAll(mapViewTab, fieldViewTab, plotViewTab, logViewTab);
  }

  /**
   * Save & Exit Event.
   */
  public void exit() {
    if (gsehenInstance.isDataChanged()) {
      Stage stage = new Stage();
      GsehenSave save = new GsehenSave();
      save.start(stage);
    } else {
      Platform.exit();
      System.exit(0);
    }
  }

  /**
   * Handles the user's request to (re)load the user-created data (farms, fields, plots, ..).
   */
  public void loadUserData() {
    gsehenInstance.loadUserData();
  }

  /**
   * Handles the user's request to save the user-created data (farms, fields, plots, ..).
   */
  public void saveUserData() {
    gsehenInstance.saveUserData();
  }

  /**
   * Handles the user's request to set the farm viewport according to the current map viewport.
   *
   * @see de.hgu.gsehen.Gsehen.setFarmViewportFromMap
   */
  public void setFarmViewportFromMap() {
    gsehenInstance.setFarmViewportFromMap();
  }

  /**
   * Handles the user's request to set the map viewport according to the current farm viewport.
   *
   * @see de.hgu.gsehen.Gsehen.setMapViewportFromFarm
   */
  public void setMapViewportFromFarm() {
    gsehenInstance.setMapViewportFromFarm();
  }

  public JFXTabPane getJFXTabPane() {
    return tabPane;
  }

  public Tab getFieldViewTab() {
    return fieldViewTab;
  }

  public void setFieldViewTab(Tab fieldViewTab) {
    this.fieldViewTab = fieldViewTab;
  }

  public Tab getMapViewTab() {
    return mapViewTab;
  }

  public void setMapViewTab(Tab mapViewTab) {
    this.mapViewTab = mapViewTab;
  }

  public Tab getPlotViewTab() {
    return plotViewTab;
  }

  public void setPlotViewTab(Tab plotViewTab) {
    this.plotViewTab = plotViewTab;
  }

  public Tab getLogViewTab() {
    return logViewTab;
  }

  public void setLogViewTab(Tab logViewTab) {
    this.logViewTab = logViewTab;
  }
}
