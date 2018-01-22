package de.hgu.gsehen;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

  /* (non-Javadoc)
   * @see javafx.application.Application#start(javafx.stage.Stage)
   */
  @Override
  public void start(Stage stage) {
    Parent root;
    try {
      root = FXMLLoader.load(getClass().getResource(MAIN_FXML));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.sizeToScene();
    stage.show();
    WebView webView = (WebView)stage.getScene().lookup(WEB_VIEW_ID);
    webView.getEngine().load("https://maps.google.de/");
  }
}
