package de.hgu.gsehen;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * The GSEHEN main application.
 */
public class Gsehen extends Application {

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
    stage.setTitle("Hello World");
    Button btn = new Button();
    btn.setLayoutX(100);
    btn.setLayoutY(80);
    btn.setText("Hello World");
    btn.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        System.out.println("Hello World");
      }
    });
    Group root = new Group();
    root.getChildren().add(btn);
    Scene scene = new Scene(root, 300, 250);
    stage.setScene(scene);
    stage.show();
  }
}
