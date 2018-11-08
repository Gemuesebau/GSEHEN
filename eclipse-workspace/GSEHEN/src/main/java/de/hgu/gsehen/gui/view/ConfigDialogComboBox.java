package de.hgu.gsehen.gui.view;

import com.jfoenix.controls.JFXComboBox;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class ConfigDialogComboBox extends ConfigDialogElement<JFXComboBox<String>, String> {

  @SuppressWarnings("checkstyle:javadocmethod")
  public ConfigDialogComboBox(Text text, JFXComboBox<String> comboBox, Text label,
      List<ConfigDialogElement<Node, Object>> add) {
    super(
        text, comboBox, label, add,
        (node, value) -> node.setValue(value),
        node -> node.getValue());
  }
}
