package de.hgu.gsehen;

import static de.hgu.gsehen.util.CollectionUtil.addToMappedList;
import static de.hgu.gsehen.util.DBUtil.executeQuery;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.event.DrawableSelected;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEvent;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.event.GsehenViewEvent;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GsehenTreeTable;
import de.hgu.gsehen.gui.controller.MainController;
import de.hgu.gsehen.gui.view.FarmDataController;
import de.hgu.gsehen.gui.view.Farms;
import de.hgu.gsehen.gui.view.Fields;
import de.hgu.gsehen.gui.view.Logs;
import de.hgu.gsehen.gui.view.Maps;
import de.hgu.gsehen.gui.view.Plots;
import de.hgu.gsehen.model.Crop;
import de.hgu.gsehen.model.Drawable;
import de.hgu.gsehen.model.Farm;
import de.hgu.gsehen.model.Field;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.util.DBUtil;
import de.hgu.gsehen.util.DateUtil;
import de.hgu.gsehen.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.hibernate.Session;
import org.hibernate.query.Query;

/**
 * The GSEHEN main application.
 *
 * @author MO, AT, CW
 */
@SuppressWarnings({ "checkstyle:commentsindentation" })
public class Gsehen extends Application {
  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());
  private static final String WEATHER_DATA_JS = "/de/hgu/gsehen/js/weatherData.js";

  protected static final ResourceBundle mainBundle = ResourceBundle.getBundle("i18n.main",
      Locale.GERMAN);
  
  private static final String MAIN_FXML = "main.fxml";

  public static final String MAIN_SPLIT_PANE_ID = "#mainSplitPane";
  public static final String DEBUG_TEXTAREA_ID = "#debugTA";
  public static final String TAB_PANE_ID = "#tabPane";
  private static final String MAPS_WEB_VIEW_ID = "#mapsWebView";
  private static final String FARMS_WEB_VIEW_ID = "#farmsWebView";
  private static final String FIELDS_VIEW_ID = "#fieldsBorderPane";
  private static final String PLOTS_VIEW_ID = "#plotsBorderPane";
  private static final String LOGS_VIEW_ID = "#logsBorderPane";
  private static final String IMAGE_VIEW_ID = "#imageView";

  private static Maps maps;
  private static Farms farms;
  private static Fields fields;
  private static Plots plots;
  private static Logs logs;
  private GsehenTreeTable treeTable;

  private List<Farm> farmsList = new ArrayList<>();
  private List<Farm> deletedFarms = new ArrayList<>();

  private Scene scene;
  private MainController mainController;

  private java.util.Map<Class<? extends GsehenEvent>,
      List<GsehenEventListener<?>>> eventListeners = new HashMap<>();
  private boolean dataChanged;

  private static DayData dayData = null;

  private static Gsehen instance;

  {

    instance = this;

  }

  public List<Farm> getDeletedFarms() {
    return this.deletedFarms;
  }

  /**
   * Main method.
   *
   * @param args
   *          the command line arguments
   */
  @SuppressWarnings({ "checkstyle:rightcurly" })
  public static void main(String[] args) {
    System.setProperty("java.util.logging.config.class", "de.hgu.gsehen.logging.Configurator");
    try {
      LogManager.getLogManager().readConfiguration();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Application.launch(args);
  }

  /**
   * Generate the Mainframe.
   * 
   * @see javafx.application.Application#start(javafx.stage.Stage)
   */
  @SuppressWarnings({ "checkstyle:rightcurly" })
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
    fields = new Fields(this, (BorderPane) scene.lookup(FIELDS_VIEW_ID));
    plots = new Plots(this, (BorderPane) scene.lookup(PLOTS_VIEW_ID));
    logs = new Logs(this, (BorderPane) scene.lookup(LOGS_VIEW_ID));

    InputStream input = this.getClass()
        .getResourceAsStream("/de/hgu/gsehen/images/Logo_UniGeisenheim.png");
    Image image = new Image(input);
    ImageView imageView = (ImageView) scene.lookup(IMAGE_VIEW_ID);
    imageView.setImage(image);

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

    treeTable = new GsehenTreeTable() {
      @Override
      public void handle(GsehenViewEvent event) {
      }
    };
    treeTable.addFarmTreeView(GsehenTreeTable.class);
  }

  /**
   * PostgreSQL DB connection and storing in Persistence.
   */
  public static void crop() {
    Crop crops = new Crop();
    final String url = "jdbc:postgresql:"
        + "//hs-geisenheim.cwliowbz3tsc.eu-west-1.rds.amazonaws.com/standard";
    final String user = "GSEHEN_user";
    final String password = "Yp4NiYiHYfmcHs7Fe2CEmTpLv";

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
    EntityManager em = emf.createEntityManager();

    Connection connection = null;
    {
      try {
        connection = DriverManager.getConnection(url, user, password);
      } catch (SQLException e) {
        System.out.println(e.getMessage());
      }
      try (PreparedStatement selectcrop = connection.prepareStatement("SELECT * FROM crop;")) {
        ResultSet rs = executeQuery(selectcrop);
        
//        em.getTransaction().begin();
//        javax.persistence.Query query = em.createQuery("DELETE FROM Crop e ");
//        int rowsDeleted = query.executeUpdate();
//        em.getTransaction().commit();
        while (rs.next()) {

          em.getTransaction().begin();

          crops = new Crop(rs.getString("cName"), rs.getBoolean("cActive"), rs.getDouble("cKC1"),
              rs.getDouble("cKC2"), rs.getDouble("cKC3"), rs.getDouble("cKC4"),
              rs.getInt("cPhase1"), rs.getInt("cPhase2"), rs.getInt("cPhase3"),
              rs.getInt("cPhase4"), rs.getString("cBBCH1"), rs.getString("cBBCH2"),
              rs.getString("cBBCH3"), rs.getString("cBBCH4"), rs.getInt("cRooting_Zone1"),
              rs.getInt("cRooting_Zone2"), rs.getInt("cRooting_Zone3"), rs.getInt("cRooting_Zone4"),
              rs.getString("cDescription"));

          em.merge(crops);
          em.getTransaction().commit();

        } 

        
      } catch (SQLException e) {
        System.out.println("no connection" + e.getLocalizedMessage());
      }

      em.close();
    }
  }

  
  /**
   * Loads the user-created data (farms, fields, plots, ..)
   */
  @SuppressWarnings("unchecked")
  public void loadUserData() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
    EntityManager em = emf.createEntityManager();
    try {

      // möglichkeit 1, mit bekannter ID
      // em.getTransaction();
      // Farm testfarm = em.find(Farm.class, 132l);
      // if(testfarm != null) {
      // System.out.print(testfarm.getName());
      // }

      // möglichkeit 2, alle möglichen objekte
      Session session = em.unwrap(Session.class);
      Query<Farm> query = session.createQuery("from Farm"); // You will get Weayher object
      farmsList = query.list(); // You are accessing as list<WeatherModel>
    } finally {
      em.close();
    }

    // ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
    // try {
    // engine.put("instance", this);
    // engine.put("LOGGER", LOGGER);
    // engine.put("farms", farmsList);
    // engine.eval(getReaderForUtf8(LOAD_USER_DATA_JS));

    dataChanged = false;
    // } catch (Exception e) {
    // LOGGER.log(Level.SEVERE, "Can't evaluate " + LOAD_USER_DATA_JS, e);
    sendFarmDataChanged(null, null);
    // }
  }

  

    
  /**
   * Saves the user-created data (farms, fields, plots, ..)
   */
  public void saveUserData() {

    try {

      EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
      EntityManager em = emf.createEntityManager();

      try {
        em.getTransaction().begin();
        for (Farm farm : farmsList) {
          em.merge(farm);
        }

        for (Farm deletedFarm : this.getDeletedFarms()) {
          em.remove(em.contains(deletedFarm) ? deletedFarm : em.merge(deletedFarm));
        }
        this.getDeletedFarms().clear();

        em.getTransaction().commit();
      } catch (Exception e) {
        System.out.println("Problem: " + e.getMessage());
        em.getTransaction().rollback();
      } finally {
        em.close();
      }

      // speichern in datei wird ersetzt durch speichern in DB
      // engine.eval(getReaderForUtf8(SAVE_USER_DATA_JS));
      // engine.put("instance", this);
      // engine.put("LOGGER", LOGGER);
      // engine.put("farms", farmsList);

      // engine.eval(getReaderForUtf8(SAVE_USER_DATA_JS));
      dataChanged = false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't evaluate ", e);
    }
  }

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
  public String getUtf8ResourceAsOneString(String resourceName) throws IOException {
    try (BufferedReader buffer = new BufferedReader(getReaderForUtf8(resourceName))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  public void registerForEvent(Class<? extends GsehenEvent> eventClass,
      GsehenEventListener<?> eventListener) {
    addToMappedList(eventListeners, eventClass, eventListener);
  }

  /**
   * Notifies the application about a newly added object (farm, field, ..).
   *
   * @param object
   *          the newly added object
   * @param skipClass
   *          a listener class to skip when notifying; typically the class that originally created
   *          the new object
   */
  public void drawableAdded(Drawable object,
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
   * Sends a "FarmDataChanged" event to all listeners registered for that kind of event, except the
   * listeners that belong to the given "skipClass".
   *
   * @param object
   *          the object that initially caused the event to be sent, or null
   * @param skipClass
   *          the event listener class to skip when iterating the listeners, or null
   */
  public void sendFarmDataChanged(Drawable object,
      Class<? extends GsehenEventListener<FarmDataChanged>> skipClass) {
    dataChanged = true;
    FarmDataChanged event = new FarmDataChanged();
    event.setFarms(farmsList);
    sendViewEvent(object, skipClass, event);
  }

  /**
   * Sends a "DrawableSelected" event to all listeners registered for that kind of event, except the
   * listeners that belong to the given "skipClass".
   *
   * @param subject
   *          the "Drawable" that initially caused the event to be sent
   * @param skipClass
   *          the event listener class to skip when iterating the listeners, or null
   */
  public void sendDrawableSelected(Drawable subject,
      Class<? extends GsehenEventListener<DrawableSelected>> skipClass) {
    DrawableSelected event = new DrawableSelected();
    event.setSubject(subject);
    sendViewEvent(subject, skipClass, event);
  }

  /**
   * Delegate method for sending prepared "view" events.
   *
   * @param drawable
   *          the "Drawable" that is subject of the event, or null
   * @param skipClass
   *          the event listener class to skip when iterating the listeners, or null
   * @param event
   *          the prepared event, lacking viewport data, which is determined here
   */
  public void sendViewEvent(Drawable drawable,
      Class<? extends GsehenEventListener<? extends GsehenViewEvent>> skipClass,
      GsehenViewEvent event) {
    try {
      event.setViewport(
          new Pair<>(new GeoPoint(drawable.getPolygon().getMinY(), drawable.getPolygon().getMinX()),
              new GeoPoint(drawable.getPolygon().getMaxY(), drawable.getPolygon().getMaxX())));
    } catch (IllegalArgumentException | NullPointerException e) {
      event.setViewport(null);
    }
    notifyEventListeners(event, skipClass);
  }

  /**
   * Notifies listeners registered for the (type of) event supplied by the given supplier.
   *
   * @param event
   *          the actual event to be sent to the registered listeners
   * @param skipClass
   *          a listener class that shall be skipped when iterating the listeners, or null
   */
  @SuppressWarnings({ "unchecked" })
  private <T extends GsehenEvent> void notifyEventListeners(T event,
      Class<? extends GsehenEventListener<? extends T>> skipClass) {
    List<GsehenEventListener<?>> farmDataChgListeners = eventListeners.get(event.getClass());
    if (farmDataChgListeners != null) {
      farmDataChgListeners.forEach(listener -> {
        if (skipClass != null && skipClass.equals(listener.getClass())
            || skipClass != null && skipClass.equals(listener.getClass().getEnclosingClass())) {
          return;
        }
        ((GsehenEventListener<T>) listener).handle(event);
      });
    }
  }

  @SuppressWarnings({ "checkstyle:abbreviationaswordinname" })
  public String readUTF8FileAsOneString(String dataFileName) throws IOException {
    return new String(Files.readAllBytes(Paths.get(dataFileName)), "utf-8");
  }

  @SuppressWarnings({ "checkstyle:abbreviationaswordinname" })
  public void writeStringAsUTF8File(String data, String dataFileName) throws IOException {
    Files.write(Paths.get(dataFileName), data.getBytes("utf-8"));
  }

  public SplitPane getMainSplitPane() {
    return (SplitPane) scene.lookup(MAIN_SPLIT_PANE_ID);
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

  public void setMapViewportFromFarm() {
    maps.reloadWithViewport(farms.getLastViewport());
  }

  public void setFarmViewportFromMap() {
    farms.reloadWithViewport(maps.getLastViewport());
  }

  public static Fields getFields() {
    return fields;
  }

  public static Plots getPlots() {
    return plots;
  }

  public static Logs getLogs() {
    return logs;
  }

  /**
   * Prompts for JavaScript to be run in a WebView.
   *
   * @param controller
   *          the controller that belongs to the target web view
   */
  public static void jsPrompt(FarmDataController controller) {
    final String contentTextKey = "gui.dialog.developer.js.prompt."
        + controller.getClass().getSimpleName().toLowerCase();
    String javaScript = textInputDialog(contentTextKey,
        instance.getBundle().getString("gui.dialog.developer.js.prompt.header"));
    Object result;
    while (javaScript != null && (result = controller.runJavaScript(javaScript)) != null) {
      javaScript = textInputDialog(contentTextKey, String.valueOf(result));
    }
  }

  private static String textInputDialog(String contentTextKey, String headerText) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("GSEHEN");
    dialog.setContentText(instance.getBundle().getString(contentTextKey));
    dialog.setHeaderText(headerText);
    dialog.showAndWait();
    return dialog.getResult();
  }

  @SuppressWarnings({"checkstyle:javadocmethod", "checkstyle:rightcurly"})
  public static void updateDayData() {
    final Date today = DateUtil.truncToDay(new Date());
    dayData = loadDayDataForDay(today, true);
    boolean success = false;
    ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
    try {
      engine.put("LOGGER", LOGGER);
      engine.put("dayData", dayData);
      engine.eval(getReaderForUtf8(WEATHER_DATA_JS));
      success = (boolean)((Invocable)engine).invokeFunction("updateDayData");
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Can't evaluate " + WEATHER_DATA_JS, e);
    }
    LOGGER.log(Level.INFO, "Weather data import was " + (success ? "" : "NOT ") + "successful");
    if (success) {
      dayData = DBUtil.saveEntity(dayData);
      LOGGER.log(Level.INFO, "Day data saved");
    }
    else {
      dayData = loadDayDataForDay(today, false);
    }
    if (dayData != null) {
      //     sendDayDataChanged(..); ---> water balance algorithm should listen to that event!
    }
  }

  @SuppressWarnings({"checkstyle:rightcurly"})
  private static DayData loadDayDataForDay(Date date, boolean create) {
    EntityManager em = Persistence.createEntityManagerFactory("GSEHEN").createEntityManager();
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<DayData> criteria = builder.createQuery(DayData.class);
    Root<DayData> dayDataRoot = criteria.from(DayData.class);
    criteria.select(dayDataRoot);
    criteria.where(builder.equal(dayDataRoot.get("date"), date));
    try {
      return em.createQuery(criteria).getSingleResult();
    }
    catch (NoResultException nre) {
      if (create) {
        DayData result = new DayData();
        result.setDate(date);
        return result;
      }
      else {
        return null;
      }
    }
  }
}
