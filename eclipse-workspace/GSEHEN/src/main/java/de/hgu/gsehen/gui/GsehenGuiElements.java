package de.hgu.gsehen.gui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;

import de.hgu.gsehen.Gsehen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class GsehenGuiElements {

  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
  }

  /**
   * Creates a GridPane.
   * 
   * @return - the GridPane.
   */
  public GridPane gridPane(BorderPane pane) {
    GridPane grid = new GridPane();

    grid.setPadding(new Insets(20, 0, 10, 20));
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setGridLinesVisible(false);

    ColumnConstraints column1 = new ColumnConstraints(100, 200, 300, Priority.ALWAYS, HPos.LEFT,
        true);
    ColumnConstraints column2 = new ColumnConstraints(100, 200, 300, Priority.ALWAYS, HPos.LEFT,
        true);
    column1.setHgrow(Priority.ALWAYS);
    column2.setHgrow(Priority.ALWAYS);

    grid.getColumnConstraints().add(column1);
    grid.getColumnConstraints().add(column2);
    grid.setMaxSize(pane.getMaxWidth(), pane.getMaxHeight());

    RowConstraints rowEmpty = new RowConstraints();
    grid.getRowConstraints().add(0, rowEmpty);

    return grid;
  }

  /**
   * Creates a Text.
   *
   * @param text
   *          the initial text of this Text element
   * @param fontWeight
   *          a particular font weight for this text
   * @return - the Text.
   */
  public Text text(String text, FontWeight fontWeight) {
    Text textElement = new Text();
    if (fontWeight != null) {
      textElement.setFont(Font.font("Arial", fontWeight, 14));
    } else {
      textElement.setFont(Font.font("Arial", 14));
    }
    textElement.setText(text);
    return textElement;
  }

  /**
   * Creates a Text.
   *
   * @param text
   *          the initial text of this Text element
   * @return - the Text.
   */
  public Text text(String text) {
    return text(text, null);
  }

  /**
   * Creates a Button.
   * 
   * @return - the Button.
   */
  public Button button(double width) {
    Button button = new Button();
    button.setId("glass-grey");
    button.setPrefSize(width, 25);

    return button;
  }
  
  /**
   * Creates a JFXButton.
   *
   * @return - the JFXButton.
   */
  public JFXButton jfxButton(String label) {
    return jfxButton(label, null);
  }

  /**
   * Creates a JFXButton with an action handler.
   *
   * @param handler the action handler to attach
   * @return - the JFXButton.
   */
  public JFXButton jfxButton(String label, EventHandler<ActionEvent> handler) {
    final JFXButton jfxButton = new JFXButton(label);
    jfxButton.setButtonType(com.jfoenix.controls.JFXButton.ButtonType.RAISED);
    jfxButton.setStyle("-fx-background-color: #e8e8e8; -fx-text-fill: black;");
    if (handler != null) {
      jfxButton.setOnAction(handler);
    }
    return jfxButton;
  }

  /**
   * Creates a DatePicker.
   * 
   * @return - the DatePicker.
   */
  public DatePicker datepicker() {
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT,
        gsehenInstance.getSelectedLocale());
    String pattern = ((SimpleDateFormat) dateFormat).toPattern();

    DatePicker datePicker = new DatePicker();
    datePicker.setShowWeekNumbers(true);

    StringConverter<LocalDate> convert = new StringConverter<LocalDate>() {
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

      @Override
      public String toString(LocalDate date) {
        if (date != null) {
          return dateFormatter.format(date);
        } else {
          return "";
        }
      }

      @Override
      public LocalDate fromString(String string) {
        if (string != null && !string.isEmpty()) {
          return LocalDate.parse(string, dateFormatter);
        } else {
          return null;
        }
      }
    };

    datePicker.setConverter(convert);
    datePicker.setPromptText(pattern);
    return datePicker;
  }

  /**
   * Creates a combo box.
   *
   * @param items the items to put into the combo box initially
   * @return the new combo box
   */
  public <T> JFXComboBox<T> comboBox(T[] items) {
    JFXComboBox<T> comboBox = new JFXComboBox<T>();
    comboBox.getItems().addAll(items);
    return comboBox;
  }
}
