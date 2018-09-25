package de.hgu.gsehen.gui;

import de.hgu.gsehen.Gsehen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class GsehenGuiElements {

  private Gsehen gsehenInstance;

  {
    gsehenInstance = Gsehen.getInstance();
  }

  /**
   * Creates a GridPane.
   * @return - the GridPane.
   */
  public GridPane gridPane() {
    GridPane grid = new GridPane();

    grid.setPadding(new Insets(20, 0, 10, 20));
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setGridLinesVisible(false);

    ColumnConstraints column1 = new ColumnConstraints(200, 100, 300);
    ColumnConstraints column2 = new ColumnConstraints(200, 100, 100);
    column1.setHgrow(Priority.ALWAYS);
    column2.setHgrow(Priority.ALWAYS);
    RowConstraints rowEmpty = new RowConstraints();

    grid.getColumnConstraints().addAll(column1, column2);
    grid.getRowConstraints().add(0, rowEmpty);
    grid.getRowConstraints().add(1, rowEmpty);

    return grid;
  }

  /**
   * Creates a Text.
   * @return - the Text.
   */
  public Text text() {
    Text text = new Text();
    text.setFont(Font.font("Arial", 14));

    return text;
  }

  /**
   * Creates a Button.
   * @return - the Button.
   */
  public Button button(double width) {
    Button button = new Button();
    button.setId("glass-grey");
    button.setPrefSize(width, 25);

    return button;
  }

  /**
   * Creates a DatePicker.
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

}
