package de.hgu.gsehen;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * The GSEHEN main application.
 */
public class Gsehen extends Application {

  private static final String MAIN_FXML = "main.fxml";

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Application.launch(args);
  }

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
    Node node = stage.getScene().getRoot().getChildrenUnmodifiable().get(0);
    node = node;

    // SplitPane splitPane1 = new SplitPane();
    // splitPane1.setPrefSize(200, 100);
    // splitPane1.setOrientation(Orientation.VERTICAL);
    // final Button l = new Button("Left Button");
    // final Button r = new Button("Right Button");
    // splitPane1.getItems().addAll(l, r);
    // HBox hbox = createHbox();
    // hbox.getChildren().add(splitPane1);
    //
    // Scene scene = new Scene(new Group(hbox), 560, 240);
    // scene.setFill(Color.GHOSTWHITE);
    // stage.setScene(scene);
    // stage.setTitle("SplitPane");
    // stage.show();
  }
  // @Override
  // public void start(Stage stage) {
  // stage.setTitle("Hello World");
  // Button btn = new Button();
  // btn.setLayoutX(100);
  // btn.setLayoutY(80);
  // btn.setText("Hello World");
  // btn.setOnAction(event -> System.out.println("Hello World")); // TODO: Lambda Schreibweise
  // Group root = new Group();
  // root.getChildren().add(btn);
  // Scene scene = new Scene(root, 300, 250);
  // stage.setScene(scene);
  // stage.show();
  // }

  private HBox createHbox() {
    HBox hbox = new HBox(20);
    hbox.setTranslateX(20);
    hbox.setTranslateY(20);
    return hbox;
  }
}
