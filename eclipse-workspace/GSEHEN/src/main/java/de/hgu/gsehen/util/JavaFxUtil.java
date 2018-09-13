package de.hgu.gsehen.util;

import javafx.scene.control.TextField;

public class JavaFxUtil {

  public static boolean noneIsEmpty(TextField[] textFields) {
    for (TextField textField : textFields) {
      if (textField.getText().trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }
}
