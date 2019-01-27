package de.hgu.gsehen.gui.util;

import com.jfoenix.controls.JFXComboBox;
import de.hgu.gsehen.model.Named;
import javafx.util.StringConverter;

public class ItemStringConverter<T extends Named> extends StringConverter<T> {
  private JFXComboBox<T> comboBox;

  public ItemStringConverter(JFXComboBox<T> comboBox) {
    super();
    this.comboBox = comboBox;
  }

  @Override
  public String toString(T object) {
    return object.getName();
  }

  @Override
  public T fromString(String string) {
    return itemByString(string);
  }

  private T itemByString(String string) {
    return comboBox.getItems().stream()
        .filter(ap -> ap.getName().equals(string)).findFirst().orElse(null);
  }
}
