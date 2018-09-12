package de.hgu.gsehen.gsbalance;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class DayDataCalculation {
  private Gsehen gsehenInstance;
  protected final ResourceBundle mainBundle;

  {
    gsehenInstance = Gsehen.getInstance();
    mainBundle = ResourceBundle.getBundle("i18n.main", gsehenInstance.getSelectedLocale());
  }

  private static final Logger LOGGER = Logger.getLogger(DayDataCalculation.class.getName());
  private static final String WEATHER_DATA_JS = "/de/hgu/gsehen/js/weatherData.js";

  public static InputStreamReader getReaderForUtf8(String resourceName) throws IOException {
    return new InputStreamReader(Gsehen.class.getResourceAsStream(resourceName), "utf-8");
  }

  /**
   * Reads the contents of a given utf-8-encoded resource as one String.
   *
   * @param resourceName
   *          the name of the resource to read
   * @return a String containing the given resource's contents
   * @throws IOException
   *           if the resource can't be read (as utf-8)
   */
  public static String getUtf8ResourceAsOneString(String resourceName) throws IOException {
    try (BufferedReader buffer = new BufferedReader(getReaderForUtf8(resourceName))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  /**
   * Checks, if a calculation can start.
   */
  public void checkCalculation() {
    // creates a JFXDialog, if the calculation can't start
    JFXDialogLayout content = new JFXDialogLayout();
    content.setHeading(new Text(mainBundle.getString("daydatacalculation.titel")));
    StackPane stackPane = new StackPane();

    Scene scene = new Scene(stackPane, 300, 250);
    Stage stage = (Stage) gsehenInstance.getScene().getWindow();

    JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);

    JFXButton okay = new JFXButton(mainBundle.getString("daydatacalculation.okay"));
    okay.setPrefWidth(110);
    okay.setStyle("-fx-background-color: #5ed100; -fx-text-fill: white;");
    okay.setButtonType(JFXButton.ButtonType.RAISED);
    okay.setOnAction(event -> {
      dialog.close();
      stackPane.setVisible(false);
      stage.setScene(gsehenInstance.getScene());
    });
    stackPane.setOnMouseClicked(event -> stackPane.setVisible(false));
    content.setActions(okay);

    Boolean calculate = true;
    String text = (mainBundle.getString("daydatacalculation.general"));

    List<Field> fieldList = new ArrayList<Field>();
    List<Plot> plotList = new ArrayList<Plot>();
    for (int i = 0; i < gsehenInstance.getFarmsList().size(); i++) {
      fieldList.addAll(gsehenInstance.getFarmsList().get(i).getFields());
      if (gsehenInstance.getFarmsList().get(i).getFields().size() != 0) {
        plotList.addAll(gsehenInstance.getFarmsList().get(i).getFields().get(i).getPlots());
      }
    }

    if (gsehenInstance.getFarmsList().isEmpty()) {
      text = text + mainBundle.getString("daydatacalculation.farm");
      calculate = false;
    }
    if (fieldList.isEmpty()) {
      text = text + mainBundle.getString("daydatacalculation.field");
      calculate = false;
    }
    if (plotList.isEmpty()) {
      text = text + mainBundle.getString("daydatacalculation.plot");
      calculate = false;
    }

    for (Field f : fieldList) {
      if (f.getSoilProfileUuid() == null) {
        text = text + "\n" + f.getName() + " "
            + mainBundle.getString("daydatacalculation.nosoilprofile");
        calculate = false;
      }
      if (f.getWeatherDataSourceUuid() == null) {
        text = text + "\n" + f.getName() + " "
            + mainBundle.getString("daydatacalculation.noweatherdatasource");
        calculate = false;
      }
    }

    for (Plot p : plotList) {
      if (p.getCrop() == null) {
        text = text + "\n" + p.getName() + " " + mainBundle.getString("daydatacalculation.nocrop");
        calculate = false;
      }
    }

    Text dialogText = new Text(text);
    dialogText.setFont(Font.font("Arial", 14));
    content.setBody(dialogText);

    if (!calculate) {
      stage.setScene(scene);
      dialog.show();
    } else {
      recalculateDayData();
    }

  }

  /**
   * Recalculates today's day data.
   */
  @SuppressWarnings({ "checkstyle:rightcurly" })
  public void recalculateDayData() {
    final ScriptEngine engine = prepareScriptEngine();
    final Date today = DateUtil.truncToDay(new Date());

    for (WeatherDataSource weatherDataSource : gsehenInstance.getWeatherDataSources()) {
      DayData dayData = null;
      try {
        dayData = (DayData) ((Invocable) engine).invokeFunction("determineDayData",
            weatherDataSource, today);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error when running 'determineDayData' in " + WEATHER_DATA_JS, e);
      }
      LOGGER.log(Level.INFO, "Weather data import from '" + weatherDataSource.getName() + "' was "
          + (dayData == null ? "NOT " : "") + "successful");
      gsehenInstance.sendDayDataChanged(dayData, weatherDataSource, null);
    }
  }

  private ScriptEngine prepareScriptEngine() {
    final ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
    engine.put("LOGGER", LOGGER);
    try {
      engine.eval(getReaderForUtf8(WEATHER_DATA_JS));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't evaluate " + WEATHER_DATA_JS, e);
    }
    return engine;
  }
}
