package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
  private Tab fieldPlotViewTab;
  @FXML
  private Tab contactViewTab;
  @FXML
  private Tab aboutViewTab;
  @FXML
  private WebView contactWebView;
  @FXML
  private WebView aboutWebView;

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
    // TODO: Sobald die Tabs wieder da sind:
    // tabPane.getTabs().addAll(mapViewTab, farmViewTab, fieldViewTab, fieldPlotViewTab);
    tabPane.getTabs().addAll(mapViewTab, farmViewTab);
  }

  /**
   * Save & Exit Event.
   */
  public void exit() {
    if (gsehenInstance.isWasCalled()) {
      Button button1 = new Button("Ja");
      button1.setStyle("-fx-font: 14 arial;");
      Button button2 = new Button("Nein");
      button2.setStyle("-fx-font: 14 arial;");
      Button button3 = new Button("Abbrechen");
      button3.setStyle("-fx-font: 14 arial;");

      button1.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
          gsehenInstance.saveUserData();
          Platform.exit();
          System.exit(0);
        }
      });

      button2.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
          Platform.exit();
          System.exit(0);
        }
      });

      Stage stage = new Stage();

      button3.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
          stage.close();
        }
      });

      Label label = new Label("Wollen Sie Ihre Daten speichern,\nbevor Sie das Programm beenden?");
      label.setStyle("-fx-font: 16 arial;");
      HBox horBox = new HBox();
      horBox.setSpacing(10);
      horBox.getChildren().addAll(button1, button2, button3);

      BorderPane borderPane = new BorderPane();
      borderPane.setTop(label);
      borderPane.setBottom(horBox);

      Scene scene = new Scene(borderPane, 260, 100);
      stage.setTitle("Speichern & beenden?");
      stage.setScene(scene);
      stage.show();
    } else {
      Platform.exit();
      System.exit(0);
    }
  }

  /**
   * Loads the user-created data (farms, fields, plots, ..)
   */
  public void loadUserData() {
    gsehenInstance.loadUserData();
  }

  /**
   * Saves the user-created data (farms, fields, plots, ..)
   */
  public void saveUserData() {
    gsehenInstance.saveUserData();
  }
}
