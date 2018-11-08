package de.hgu.gsehen.gui.view;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;

public class ConfigDialogCheckBox extends ConfigDialogElement<CheckBox, Boolean> {

  @SuppressWarnings("checkstyle:javadocmethod")
  public ConfigDialogCheckBox(Text text, Text label,
      List<ConfigDialogElement<Node, Object>> add) {
    super(
        text, new CheckBox(), label, add,
        (node, value) -> node.setSelected(value),
        node -> node.isSelected());
  }
}
