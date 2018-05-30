package de.hgu.gsehen;

import static de.hgu.gsehen.util.CollectionUtil.addToMappedList;
import static de.hgu.gsehen.util.JDBCUtil.executeQuery;
import static de.hgu.gsehen.util.JDBCUtil.executeUpdate;
import static de.hgu.gsehen.util.JDBCUtil.parseYmd;

import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEvent;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GsehenTreeTable;
import de.hgu.gsehen.gui.controller.MainController;
import de.hgu.gsehen.gui.view.Farms;
import de.hgu.gsehen.gui.view.Maps;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * The GSEHEN main application.
 *
 * @author MO, AT, CW
 */
@SuppressWarnings({"checkstyle:commentsindentation"})
public class Gsehen extends Application {
  protected static final ResourceBundle mainBundle =
      ResourceBundle.getBundle("i18n.main", Locale.GERMAN);
  private static final String GSEHEN_H2_LOCAL_DB = "gsehen-h2-local.db";
  private static final String DAYDATA_TABLE = "DAYDATA";
  private static final String MAIN_FXML = "main.fxml";
  public static final String DEBUG_TEXTAREA_ID = "#debugTA";
  public static final String TAB_PANE_ID = "#tabPane";
  private static final String MAPS_WEB_VIEW_ID = "#mapsWebView";
  private static final String FARMS_WEB_VIEW_ID = "#farmsWebView";
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());
  private static final String LOAD_USER_DATA_JS = "/de/hgu/gsehen/js/loadUserData.js";
  private static final String SAVE_USER_DATA_JS = "/de/hgu/gsehen/js/saveUserData.js";

  private static Maps maps;
  private static Farms farms;
  private GsehenTreeTable treeTable;
  private List<Farm> farmsList = new ArrayList<>();

  private Scene scene;
  private MainController mainController;

  private java.util.Map<Class<? extends GsehenEvent>, List<GsehenEventListener<?>>> eventListeners =
      new HashMap<>();
  private boolean dataChanged;

  private static Gsehen instance;

  {
    instance = this;
  }

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
  @SuppressWarnings({"checkstyle:rightcurly"})
  public static void main(String[] args) {
    System.setProperty("java.util.logging.config.class", "de.hgu.gsehen.logging.Configurator");
    try {
      LogManager.getLogManager().readConfiguration();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Application.launch(args);
  }

  /*
   * (non-Javadoc)
   *
   * @see javafx.application.Application#start(javafx.stage.Stage)
   */
  @SuppressWarnings({"checkstyle:rightcurly"})
  @Override
  public void start(Stage stage) {
    Parent root;
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_FXML), mainBundle);
      root = loader.load();
      mainController = loader.getController();
    } catch (IOException e) {
      throw new RuntimeException(MAIN_FXML + " couldn't be loaded", e);
    }

    scene = new Scene(root, 1280, 800);
    stage.setScene(scene);
    stage.setTitle("GSEHEN");
    stage.getIcons().add(new Image("/de/hgu/gsehen/images/Logo_UniGeisenheim_36x36.png"));
    stage.setMinWidth(root.minWidth(-1));
    stage.setMinHeight(root.minHeight(-1));
    stage.sizeToScene();
    stage.show();

    maps = new Maps(this, (WebView) scene.lookup(MAPS_WEB_VIEW_ID));
    farms = new Farms(this, (WebView) scene.lookup(FARMS_WEB_VIEW_ID));

    TabPane tabPane = (TabPane) stage.getScene().lookup(TAB_PANE_ID);
    tabPane.getTabs().remove(tabPane.getTabs().size() - 2, tabPane.getTabs().size());

    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent t) {
        t.consume();
        mainController.exit();
      }
    });

    loadUserData();
    
    treeTable = new GsehenTreeTable();
    treeTable.addFarmTreeView();
  }

  @SuppressWarnings({"unused", "checkstyle:rightcurly"})
  private void testDatabase(TextArea debugTextArea) {
    Connection con = null;
    try {
      String jdbcUrl = "jdbc:h2:./" + GSEHEN_H2_LOCAL_DB + ";CIPHER=AES";
      con = DriverManager.getConnection(jdbcUrl, "", "OCddpvUe ");
      // PW: space is important! But this is just a test, must be supplied by user or the like
      LOGGER.info("Opened local H2 database at url " + jdbcUrl);
    } catch (SQLException e) {
      throw new RuntimeException(GSEHEN_H2_LOCAL_DB + " couldn't be opened", e);
    }
    // in h2, the DATE column type has no time information!
    // id: http://www.h2database.com/html/datatypes.html#identity_type
    executeUpdate(con,
        "CREATE TABLE IF NOT EXISTS " + DAYDATA_TABLE + "(id IDENTITY, date DATE, t_min DOUBLE)",
        DAYDATA_TABLE + " couldn't be created");
    try (PreparedStatement insertDayData =
        con.prepareStatement("INSERT INTO " + DAYDATA_TABLE + " (date, t_min)" + " VALUES(?, ?)")) {
      executeUpdate(insertDayData, parseYmd("2018-01-21"), 12.1);
      executeUpdate(insertDayData, parseYmd("2018-01-22"), 12.2);
      executeUpdate(insertDayData, parseYmd("2018-01-23"), 12.3);
      con.commit();
    } catch (SQLException e) {
      throw new RuntimeException(DAYDATA_TABLE + " values couldn't be inserted", e);
    }
    try (PreparedStatement selectDayData =
        con.prepareStatement("SELECT * FROM " + DAYDATA_TABLE + " WHERE date > ?")) {
      ResultSet rs = executeQuery(selectDayData, parseYmd("2018-01-20"));
      while (rs.next()) {
        debugTextArea.appendText("[" + rs.getInt("id") + ", " + rs.getDate("date") + ", "
            + rs.getDouble("t_min") + "]\n");
      }
    } catch (SQLException e) {
      throw new RuntimeException(DAYDATA_TABLE + " values couldn't be selected", e);
    }

    if (con != null) {
      try {
        con.close();
      } catch (SQLException e) {
        throw new RuntimeException("DB connection couldn't be closed", e);
      }
    }
  }

  /**
   * Loads the user-created data (farms, fields, plots, ..)
   */
  public void loadUserData() {
    ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
    try {
      engine.put("instance", this);
      engine.put("LOGGER", LOGGER);
      engine.put("farms", farmsList);
      engine.eval(getReaderForUtf8(LOAD_USER_DATA_JS));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't evaluate " + LOAD_USER_DATA_JS, e);
    }
  }

  /**
   * Saves the user-created data (farms, fields, plots, ..)
   */
  public void saveUserData() {
    ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
    try {
      engine.put("instance", this);
      engine.put("LOGGER", LOGGER);
      engine.put("farms", farmsList);
      engine.eval(getReaderForUtf8(SAVE_USER_DATA_JS));
      dataChanged = false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't evaluate " + SAVE_USER_DATA_JS, e);
    }
  }

  public InputStreamReader getReaderForUtf8(String resourceName) throws IOException {
    return new InputStreamReader(this.getClass().getResourceAsStream(resourceName), "utf-8");
  }

  /**
   * Reads the contents of a given utf-8-encoded resource as one String.
   *
   * @param resourceName the name of the resource to read
   * @return a String containing the given resource's contents
   * @throws IOException if the resource can't be read (as utf-8)
   */
  public String getUtf8ResourceAsOneString(String resourceName) throws IOException {
    try (BufferedReader buffer = new BufferedReader(getReaderForUtf8(resourceName))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  public void registerForEvent(Class<? extends GsehenEvent> eventClass,
      GsehenEventListener<?> eventListener) {
    addToMappedList(eventListeners, eventClass, eventListener);
  }

  @SuppressWarnings({"checkstyle:javadocmethod", "checkstyle:rightcurly"})
  public void objectAdded(Drawable object,
      Class<? extends GsehenEventListener<FarmDataChanged>> skipClass) {
    if (object instanceof Farm) {
      farmsList.add((Farm) object);
      LOGGER.info("Added farm " + object.getName());
    } else if (object instanceof Field) {
      getNewFieldsFarm().getFields().add((Field) object);
      LOGGER.info("Added field " + object.getName());
    } else if (object instanceof Plot) {
      getNewPlotsField(getNewFieldsFarm()).getPlots().add((Plot) object);
      LOGGER.info("Added plot " + object.getName());
    }
    sendFarmDataChanged(object, skipClass);
  }

  private Field getNewPlotsField(Farm farm) {
    String newPlotsFieldName = mainBundle.getString("gui.control.objectTree.newPlotsFieldName");
    Field newPlotsField = null;
    for (Field field : farm.getFields()) {
      if (field.getName().equals(newPlotsFieldName)) {
        newPlotsField = field;
        break;
      }
    }
    if (newPlotsField == null) {
      newPlotsField = new Field(newPlotsFieldName, null);
      farm.getFields().add(newPlotsField);
    }
    return newPlotsField;
  }

  private Farm getNewFieldsFarm() {
    String newFieldsFarmName = mainBundle.getString("gui.control.objectTree.newFieldsFarmName");
    Farm newFieldsFarm = null;
    for (Farm farm : farmsList) {
      if (farm.getName().equals(newFieldsFarmName)) {
        newFieldsFarm = farm;
        break;
      }
    }
    if (newFieldsFarm == null) {
      newFieldsFarm = new Farm(newFieldsFarmName, null);
      farmsList.add(newFieldsFarm);
    }
    return newFieldsFarm;
  }

  /**
   * Sends a "FarmDataChanged" event to all listeners registered for that kind of event,
   * except the listeners that belong to the given "skipClass".
   *
   * @param object the object that initially caused the event to be sent
   * @param skipClass the event listener class to skip when iterating the listeners, or null
   */
  public void sendFarmDataChanged(Drawable object,
      Class<? extends GsehenEventListener<FarmDataChanged>> skipClass) {
    dataChanged = true;
    FarmDataChanged event = new FarmDataChanged();
    event.setFarms(farmsList);
    try {
      event.setViewPort(new Pair<>(
          new GeoPoint(object.getPolygon().getMinY(), object.getPolygon().getMinX()),
          new GeoPoint(object.getPolygon().getMaxY(), object.getPolygon().getMaxX())
      ));
    } catch (IllegalArgumentException e) {
      event.setViewPort(null);
    }
    notifyEventListeners(() -> {
      return event;
    }, skipClass);
  }

  /**
   * Notifies listeners registered for the (type of) event supplied by the given supplier.
   *
   * <p>
   * Is currently also called from JS (.../resources/de/hgu/gsehen/js/loadUserData.js).
   * </p>
   *
   * @param eventSupplier supplier for the actual event to be sent to the registered listeners
   * @param skipClass a listener class that shall be skipped when iterating the listeners, or null
   */
  @SuppressWarnings({"unchecked"})
  public <T extends GsehenEvent> void notifyEventListeners(Supplier<T> eventSupplier,
      Class<? extends GsehenEventListener<T>> skipClass) {
    T event = eventSupplier.get();
    List<GsehenEventListener<?>> farmDataChgListeners = eventListeners.get(event.getClass());
    if (farmDataChgListeners != null) {
      farmDataChgListeners.forEach(listener -> {
        if (skipClass != null && skipClass.equals(listener.getClass())) {
          return;
        }
        ((GsehenEventListener<T>) listener).handle(event);
      });
    }
  }

  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  public String readUTF8FileAsString(String dataFileName) throws IOException {
    return new String(Files.readAllBytes(Paths.get(dataFileName)), "utf-8");
  }

  @SuppressWarnings({"checkstyle:abbreviationaswordinname"})
  public void writeStringAsUTF8File(String data, String dataFileName) throws IOException {
    Files.write(Paths.get(dataFileName), data.getBytes("utf-8"));
  }

  public static Gsehen getInstance() {
    return instance;
  }

  public static Maps getMaps() {
    return maps;
  }

  public static Farms getFarms() {
    return farms;
  }

  public ResourceBundle getBundle() {
    return mainBundle;
  }

  public List<Farm> getFarmsList() {
    return farmsList;
  }

  public Scene getScene() {
    return scene;
  }

  public boolean isDataChanged() {
    return dataChanged;
  }
}
