package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class GsehenFileChooser extends Application {
  private Gsehen gsehenInstance;
  protected static final ResourceBundle mainBundle =
      ResourceBundle.getBundle("i18n.main", Locale.GERMAN);
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  {
    gsehenInstance = Gsehen.getInstance();
  }

  @Override
  public void start(final Stage stage) {
    stage.setTitle(mainBundle.getString("filechooser.saveandexit"));
    stage.getIcons().add(new Image("/de/hgu/gsehen/images/Logo_UniGeisenheim_36x36.png"));
    stage.setAlwaysOnTop(true);
    stage.setHeight(100);
    stage.setWidth(300);

    final FileChooser fileChooser = new FileChooser();
    final Button saveButton = new Button(mainBundle.getString("menu.file.save"));
    final Button exitButton = new Button(mainBundle.getString("menu.file.exit"));
    final Button cancelButton = new Button(mainBundle.getString("filechooser.cancel"));

    saveButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(final ActionEvent e) {
        configureFileChooser(fileChooser);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
          saveFile("", file);
        }
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

  private static void configureFileChooser(final FileChooser fileChooser) {
    fileChooser.setTitle(mainBundle.getString("menu.file.save") + "...");
    fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));

    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    Date date = new Date();
    fileChooser.setInitialFileName("MeineFarm_" + dateFormat.format(date));
  }

  private void saveFile(String content, File file) {
    try {
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.write(content);
      fileWriter.close();
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }
}
