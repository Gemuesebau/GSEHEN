package de.hgu.gsehen.gui.controller;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gui.GsehenFileChooser;
import de.hgu.gsehen.model.CsvItem;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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
  private String column2;
  private String column3;
  private String column4;
  private String column5;
  private String column6;
  private String column7;
  private String column8;
  private String column9;
  private String column10;
  private String column11;
  private String column12;
  private String column13;
  private TableView<CsvItem> tableView;
  private ObservableList<CsvItem> dataList;
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  {
    gsehenInstance = Gsehen.getInstance();
  }

  // Views
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

  @SuppressWarnings({"unchecked", "rawtypes", "static-access"})
  @FXML
  private void csv(ActionEvent c) {
    Stage primaryStage = new Stage();
    primaryStage.setTitle("CSV-Konfiguration");

    tableView = new TableView<>();
    dataList = FXCollections.observableArrayList();

    String csvFile = "src\\main\\resources\\de\\hgu\\gsehen\\csv\\GeisenheimKlima.csv";
    String fieldDelimiter = ";";

    BufferedReader br;

    try {
      br = new BufferedReader(new FileReader(csvFile));

      Boolean firstLine = true;
      String line;
      while ((line = br.readLine()) != null) {
        String[] fields = line.split(fieldDelimiter, -1);

        if (firstLine) {
          column2 = fields[1];
          column3 = fields[2];
          column4 = fields[3];
          column5 = fields[4];
          column6 = fields[5];
          column7 = fields[6];
          column8 = fields[7];
          column9 = fields[8];
          column10 = fields[9];
          column11 = fields[10];
          column12 = fields[11];
          column13 = fields[12];
          firstLine = false;
        } else {
          CsvItem csvItem =
              new CsvItem(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5],
                  fields[6], fields[7], fields[8], fields[9], fields[10], fields[11], fields[12]);
          dataList.add(csvItem);
        }

      }

    } catch (FileNotFoundException ex) {
      LOGGER.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      LOGGER.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
    }

    TableColumn columnF1 = new TableColumn("");
    columnF1.setCellValueFactory(new PropertyValueFactory<>("f1"));

    TableColumn columnF2 = new TableColumn(column2);
    columnF2.setCellValueFactory(new PropertyValueFactory<>("f2"));

    TableColumn columnF3 = new TableColumn(column3);
    columnF3.setCellValueFactory(new PropertyValueFactory<>("f3"));

    TableColumn columnF4 = new TableColumn(column4);
    columnF4.setCellValueFactory(new PropertyValueFactory<>("f4"));

    TableColumn columnF5 = new TableColumn(column5);
    columnF5.setCellValueFactory(new PropertyValueFactory<>("f5"));

    TableColumn columnF6 = new TableColumn(column6);
    columnF6.setCellValueFactory(new PropertyValueFactory<>("f6"));

    TableColumn columnF7 = new TableColumn(column7);
    columnF7.setCellValueFactory(new PropertyValueFactory<>("f7"));

    TableColumn columnF8 = new TableColumn(column8);
    columnF8.setCellValueFactory(new PropertyValueFactory<>("f8"));

    TableColumn columnF9 = new TableColumn(column9);
    columnF9.setCellValueFactory(new PropertyValueFactory<>("f9"));

    TableColumn columnF10 = new TableColumn(column10);
    columnF10.setCellValueFactory(new PropertyValueFactory<>("f10"));

    TableColumn columnF11 = new TableColumn(column11);
    columnF11.setCellValueFactory(new PropertyValueFactory<>("f11"));

    TableColumn columnF12 = new TableColumn(column12);
    columnF12.setCellValueFactory(new PropertyValueFactory<>("f12"));

    TableColumn columnF13 = new TableColumn(column13);
    columnF13.setCellValueFactory(new PropertyValueFactory<>("f13"));

    tableView.setItems(dataList);
    tableView.getColumns().addAll(columnF1, columnF2, columnF3, columnF4, columnF5, columnF6,
        columnF7, columnF8, columnF9, columnF10, columnF11, columnF12, columnF13);

    VBox vBox = new VBox();
    vBox.setSpacing(10);
    vBox.setPadding(new Insets(20, 20, 20, 20));
    vBox.getChildren().add(tableView);

    Group root = new Group();
    root.getChildren().add(vBox);

    primaryStage.setScene(new Scene(root, 1100, 450));
    primaryStage.show();

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
