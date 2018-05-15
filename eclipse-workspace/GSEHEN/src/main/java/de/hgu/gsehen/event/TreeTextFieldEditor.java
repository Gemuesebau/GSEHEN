package de.hgu.gsehen.event;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.model.Farm;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class TreeTextFieldEditor extends TreeCell<String>
    implements GsehenEventListener<FarmDataChanged> {
  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
    gsehenInstance.registerForEvent(FarmDataChanged.class, this);
  }

  private TextField textField;

  @Override
  public void cancelEdit() {
    super.cancelEdit();
  }

  @Override
  public void startEdit() {
    super.startEdit();
    if (textField == null) {
      createTextField();
    }
    setText(null);
    setGraphic(textField);
    textField.selectAll();
  }

  private void createTextField() {
    textField = new TextField(getString());
    textField.setOnKeyReleased(new EventHandler<KeyEvent>() {

      public void handle(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
          commitEdit(textField.getText());
        } else if (e.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        }

      }
    });

  }

  private String getString() {
    return getItem() == null ? "" : getItem().toString();
  }

  @Override
  protected void updateItem(String string, boolean empty) {
    super.updateItem(string, empty);
    if (empty) {
      setText(null);
      setGraphic(null);
    } else {
      if (isEditing()) {
        if (textField != null) {
          textField.setText(getString());
        }
        setText(null);
        setGraphic(textField);
      } else {
        setText(getString());
        setGraphic(getTreeItem().getGraphic());
      }
    }
  }

  @Override
  public void handle(FarmDataChanged event) {
    // List<Farm> newFarms = event.getFarms();
    //
    // System.out.println(textField.getText());

    // TODO: Die handle feuert zu früh. textField ist noch unbekannt und kann somit nicht abgefangen
    // werden!
  }
}
