package de.hgu.gsehen;

import de.hgu.gsehen.webview.Map;
import java.io.IOException;
import javafx.application.Application;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * The GSEHEN main application.
 *
 * @author MO, AT
 */
public class Gsehen extends Application {

  private static final String WEB_VIEW_ID = "#webView";
  private static final String MAIN_FXML = "main.fxml";

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Application.launch(args);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javafx.application.Application#start(javafx.stage.Stage)
   */
  @SuppressWarnings("checkstyle:rightcurly")
  @Override
  public void start(Stage stage) {
    Parent root;
    try {
      root = FXMLLoader.load(getClass().getResource(MAIN_FXML));
    }
    catch (IOException e) {
      throw new RuntimeException(MAIN_FXML + " couldn't be loaded", e);
    }
    Scene scene = new Scene(root, 1280, 768);
    stage.setScene(scene);
    stage.sizeToScene();
    stage.show();
    WebEngine engine = ((WebView) stage.getScene().lookup(WEB_VIEW_ID)).getEngine();
    engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
      if (newState == State.SUCCEEDED) {
        engine.executeScript("initialize({"
            + " center: new google.maps.LatLng(52.266344, 10.519835),"
            + " zoom: 16, fullscreenControl: false"
            + " }); draw()");
      }
    });
    engine.loadContent(Map.getMapHtml());
  }
}
