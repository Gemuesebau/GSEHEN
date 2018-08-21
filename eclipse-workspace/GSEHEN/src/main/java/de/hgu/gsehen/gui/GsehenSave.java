package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class GsehenSave extends Application {
  private Gsehen gsehenInstance;
  protected static final ResourceBundle mainBundle =
      ResourceBundle.getBundle("i18n.main", Locale.GERMAN);

  {
    gsehenInstance = Gsehen.getInstance();
  }

  @Override
  public void start(final Stage stage) {
    stage.setTitle(mainBundle.getString("save.titel"));
    stage.getIcons().add(new Image("/de/hgu/gsehen/images/Logo_UniGeisenheim_36x36.png"));
    stage.setAlwaysOnTop(true);
    stage.setHeight(100);
    stage.setWidth(420);

    final Button saveButton = new Button(mainBundle.getString("save.saveandexit"));
    final Button exitButton = new Button(mainBundle.getString("save.exitwithoutsave"));
    final Button cancelButton = new Button(mainBundle.getString("save.cancel"));

    saveButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(final ActionEvent e) {
        gsehenInstance.saveUserData();
        Platform.exit();
        System.exit(0);
      }
    });

    exitButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(final ActionEvent e) {
        Platform.exit();
        System.exit(0);
      }
    });

    cancelButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(final ActionEvent e) {
        stage.close();
      }
    });

    final GridPane inputGridPane = new GridPane();

    GridPane.setConstraints(saveButton, 0, 1);
    GridPane.setConstraints(exitButton, 1, 1);
    GridPane.setConstraints(cancelButton, 2, 1);
    inputGridPane.setHgap(6);
    inputGridPane.setVgap(6);
    inputGridPane.getChildren().addAll(saveButton, exitButton, cancelButton);

    final Pane rootGroup = new VBox(12);
    rootGroup.getChildren().addAll(inputGridPane);
    rootGroup.setPadding(new Insets(12, 12, 12, 12));

    stage.setScene(new Scene(rootGroup));
    stage.show();
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
