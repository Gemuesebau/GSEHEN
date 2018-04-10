package de.hgu.gsehen;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * The GSEHEN Main-Controller.
 * 
 * @author CWI
 *
 */
public class MainController_chris {

  // Views
  @FXML
  private Accordion accordion;
  @FXML
  private ToggleButton mapView;
  @FXML
  private ToggleButton farmView;
  @FXML
  private ToggleButton fieldView;
  @FXML
  private ToggleButton fieldPlotView;
  @FXML
  private ToggleGroup viewGroup;
  @FXML
  private WebView webView;
  @FXML
  private Pane pane;

  // Help-Menu
  @FXML
  private MenuItem contactMenuItem;
  @FXML
  private MenuItem aboutUsMenuItem;

  // Opens a new Stage.
  @FXML
  protected void about(ActionEvent t) {
    Stage stage = new Stage();
    Scene scene = new Scene(new VBox());
    stage.setTitle("popup");
    stage.setScene(scene);
    stage.show();
  }
  
  
  protected void changeView(ActionEvent t) {
    webView.toBack();
  }
}
