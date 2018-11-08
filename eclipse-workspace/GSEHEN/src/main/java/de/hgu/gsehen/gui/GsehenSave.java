package de.hgu.gsehen.gui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;

import de.hgu.gsehen.Gsehen;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public final class GsehenSave {
  private Gsehen gsehenInstance;
  private GsehenGuiElements gsehenGuiElements;
  protected final ResourceBundle mainBundle;

  {
    gsehenInstance = Gsehen.getInstance();

    gsehenGuiElements = new GsehenGuiElements();

    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());
  }

  /**
   * Shows a dialog when the user wants to exit.
   */
  public void exitApplication() {
    JFXDialogLayout content = new JFXDialogLayout();
    content.setHeading(new Text(mainBundle.getString("save.titel")));
    StackPane stackPane = new StackPane();
    Scene scene = new Scene(stackPane, 300, 250);
    Stage stage = (Stage) gsehenInstance.getScene().getWindow();
    JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
    stage.setScene(scene);
    dialog.show();

    final JFXButton saveButton = gsehenGuiElements
        .jfxButton(mainBundle.getString("save.saveandexit"));
    final JFXButton exitButton = gsehenGuiElements
        .jfxButton(mainBundle.getString("save.exitwithoutsave"));
    final JFXButton cancelButton = gsehenGuiElements.jfxButton(mainBundle.getString("save.cancel"));

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
        dialog.close();
        stackPane.setVisible(false);
        stage.setScene(gsehenInstance.getScene());
      }
    });

    final GridPane inputGridPane = new GridPane();

    GridPane.setConstraints(saveButton, 0, 1);
    GridPane.setConstraints(exitButton, 1, 1);
    GridPane.setConstraints(cancelButton, 2, 1);
    inputGridPane.setHgap(6);
    inputGridPane.setVgap(6);
    inputGridPane.getChildren().addAll(saveButton, exitButton, cancelButton);

    content.setBody(inputGridPane);
  }
}
