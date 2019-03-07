package de.hgu.gsehen.gui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;

import de.hgu.gsehen.Gsehen;

import java.util.ResourceBundle;

import javafx.application.Platform;
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
    StackPane stackPane = new StackPane();
    JFXDialogLayout content = new JFXDialogLayout();
    content.setHeading(new Text(mainBundle.getString("save.titel")));
    JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
    Stage stage = (Stage) gsehenInstance.getScene().getWindow();
    stage.setScene(new Scene(stackPane, 300, 250));
    dialog.show();

    final JFXButton saveButton = gsehenGuiElements
        .jfxButton(mainBundle.getString("save.saveandexit"));
    saveButton.setOnAction(e -> {
      gsehenInstance.saveUserData();
      Platform.exit();
      System.exit(0);
    });
    GridPane.setConstraints(saveButton, 0, 1);

    final JFXButton exitButton = gsehenGuiElements
        .jfxButton(mainBundle.getString("save.exitwithoutsave"));
    exitButton.setOnAction(e -> {
      Platform.exit();
      System.exit(0);
    });
    GridPane.setConstraints(exitButton, 1, 1);

    final JFXButton cancelButton = gsehenGuiElements
        .jfxButton(mainBundle.getString("save.cancel"));
    cancelButton.setOnAction(e -> {
      dialog.close();
      stackPane.setVisible(false);
      stage.setScene(gsehenInstance.getScene());
    });
    GridPane.setConstraints(cancelButton, 2, 1);

    final GridPane inputGridPane = new GridPane();
    inputGridPane.setHgap(6);
    inputGridPane.setVgap(6);
    inputGridPane.getChildren().addAll(saveButton, exitButton, cancelButton);
    content.setBody(inputGridPane);
  }
}
