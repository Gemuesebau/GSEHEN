package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GsehenFileChooser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
@SuppressWarnings({"checkstyle:commentsindentation"})
public class MainController {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
  }

  // Views
  @FXML
  private Accordion accordion;
  @FXML
  private TitledPane fieldsPane;
  @FXML
  private TabPane tabPane;
  @FXML
  private Tab mapViewTab;
  @FXML
  private Tab farmViewTab;
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

  // Help-Menu
  @FXML
  private MenuItem contactMenuItem;
  @FXML
  private MenuItem aboutUsMenuItem;

  @FXML
  private void about(ActionEvent a) {
    accordion.setVisible(false);
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
  private void openContactView(ActionEvent o) {
    accordion.setVisible(false);
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
    accordion.setVisible(true);
    tabPane.getTabs().clear();
    tabPane.getTabs().addAll(mapViewTab, farmViewTab, fieldViewTab, plotViewTab, logViewTab);
  }

  /**
   * Save & Exit Event.
   */
  public void exit() {
    if (gsehenInstance.isDataChanged()) {
      Stage stage = new Stage();
      GsehenFileChooser fileChooser = new GsehenFileChooser();
      fileChooser.start(stage);
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
}
