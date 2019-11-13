package de.hgu.gsehen.util;

import javafx.scene.control.TextField;

public class JavaFxUtil {

  /**
   * Checks text fields for contents.
   *
   * @param textFields the text fields to check
   * @return true if and only if none of the text fields is empty, after trimming its contents
   */
  public static boolean noneIsEmpty(TextField[] textFields) {
    for (TextField textField : textFields) {
      if (textField.getText().trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }
}
