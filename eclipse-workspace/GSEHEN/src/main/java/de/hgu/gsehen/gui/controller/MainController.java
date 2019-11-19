package de.hgu.gsehen.gui.controller;

import static de.hgu.gsehen.Gsehen.isDeveloperMode;

import com.jfoenix.controls.JFXTabPane;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GsehenGuiElements;
import de.hgu.gsehen.gui.GsehenSave;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * The GSEHEN Main-Controller.
 *
 * @author CWI, AT
 */
@SuppressWarnings({ "checkstyle:commentsindentation" })
public class MainController {
  private Gsehen gsehenInstance = Gsehen.getInstance();

  @FXML
  @SuppressWarnings({ "checkstyle:javadocmethod" })
  public void helpMenuShowing() {
    if (!isDeveloperMode()) {
      final DeveloperController developerController = DeveloperController.getInstance();
      developerController.getDeveloperMenu().setVisible(false);
    }
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
  private Tab exportViewTab;
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
  private Button aboutBack;
  @FXML
  private Button contactBack;

  @FXML
  private void about(ActionEvent a) {
    tabPane.getTabs().clear();
    tabPane.getTabs().add(aboutViewTab);

    aboutBack = GsehenGuiElements.button(100);

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
    File pluginsFolder = new File(System.getProperty("user.home") + File.separator
        + ".gsehenIrrigationManager" + File.separator + "plugins");
    pluginsFolder.mkdirs();
    String openPluginFolderCommand = gsehenInstance.getPreferenceValue("openPluginFolderCommand");
    if (openPluginFolderCommand != null && openPluginFolderCommand.trim().length() != 0) {
      try {
        Runtime.getRuntime().exec(openPluginFolderCommand, new String[0], pluginsFolder).waitFor();
      } catch (Exception e) {
        throwOpenPluginsFolderException(e);
      }
    } else {
      try {
        Desktop.getDesktop().open(pluginsFolder);
      } catch (IOException e) {
        throwOpenPluginsFolderException(e);
      }
    }
  }

  private void throwOpenPluginsFolderException(Exception e) {
    throw new RuntimeException("Couldn't open plugins folder!", e);
  }

  @FXML
  private void openContactView(ActionEvent o) {
    tabPane.getTabs().clear();
    tabPane.getTabs().add(contactViewTab);

    contactBack = GsehenGuiElements.button(100);

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
      // Stage stage = new Stage();
      GsehenSave save = new GsehenSave();
      save.exitApplication();
    } else {
      Platform.exit();
      System.exit(0);
    }
  }

  /**
   * Handles the user's request to (re)load the user-created data (farms, fields, plots, ..).
   */
  public void loadFarmData() {
    gsehenInstance.loadFarmData();
  }

  /**
   * Handles the user's request to save all user-created data (prefs; farms, fields, plots, ..).
   */
  public void saveUserData() {
    gsehenInstance.saveUserData();
  }

  public JFXTabPane getJfxTabPane() {
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

  public Tab getExportViewTab() {
    return exportViewTab;
  }

  public void setExportViewTab(Tab exportViewTab) {
    this.exportViewTab = exportViewTab;
  }

  @FXML
  public void editPreferences() {
    Gsehen.editPreferences();
  }

  @FXML
  public void updateDayData(ActionEvent e) {
    Gsehen.updateDayData();
  }
}
