package de.hgu.gsehen;

import static de.hgu.gsehen.util.CollectionUtil.addToMappedList;
import static de.hgu.gsehen.util.DBUtil.executeQuery;

import com.jfoenix.controls.JFXTabPane;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.event.DayDataChanged;
import de.hgu.gsehen.event.DrawableSelected;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEvent;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.event.GsehenViewEvent;
import de.hgu.gsehen.event.ManualDataChanged;
import de.hgu.gsehen.event.RecommendedActionChanged;
//import de.hgu.gsehen.gsbalance.DayDataCalculation;
import de.hgu.gsehen.gsbalance.Recommender;
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
import de.hgu.gsehen.model.Messages;
import de.hgu.gsehen.model.Plot;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.CollectionUtil;
import de.hgu.gsehen.util.DBUtil;
import de.hgu.gsehen.util.Pair;
import de.hgu.gsehen.util.PluginUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
  protected final ResourceBundle mainBundle;

  private static final String MAIN_FXML = "main.fxml";

  public static final String MAIN_SPLIT_PANE_ID = "#mainSplitPane";
  public static final String DEBUG_TEXTAREA_ID = "#debugTA";
  public static final String TAB_PANE_ID = "#tabPane";
  private static final String MAPS_WEB_VIEW_ID = "#mapsWebView";
  private static final String FIELDS_VIEW_ID = "#fieldsBorderPane";
  private static final String PLOTS_VIEW_ID = "#plotsBorderPane";
  private static final String LOGS_VIEW_ID = "#logsBorderPane";
  private static final String IMAGE_VIEW_ID = "#imageView";

  private static Maps maps;
  private static Farms farms;
  private static Fields fields;
  private static Plots plots;
  private static Logs logs;
  // private static DayDataCalculation dayDataCalculation;

  private GsehenTreeTable treeTable;

  @SuppressWarnings("unused")
  private SplitPane mainSplitPane;

  private List<Farm> farmsList = new ArrayList<>();

  private List<Farm> deletedFarms = new ArrayList<>();

  private Scene scene;
  private MainController mainController;

  private java.util.Map<Class<? extends GsehenEvent>, 
      List<GsehenEventListener<?>>> eventListeners = new HashMap<>();

  private boolean dataChanged;
  private List<SoilProfile> soilProfilesList;
  private List<WeatherDataSource> weatherDataSourcesList;
  private List<Crop> crops;
  private Map<String, Messages> messages;

  private static Gsehen instance;

  private Locale selectedLocale;
  private DecimalFormat oneDecimalNumberFormat;
  private DecimalFormat twoDecimalNumberFormat;
  private DecimalFormat moreDecimalNumberFormat;
  private SimpleDateFormat dateFormat;

  {
    instance = this;
    setSelectedLocale(Locale.GERMAN);

    soilProfilesList = loadAll(SoilProfile.class);
    weatherDataSourcesList = loadAll(WeatherDataSource.class);
    LOGGER.log(Level.INFO, "Loaded Croplist");
    crops = loadAll(Crop.class);
    messages = CollectionUtil.listToMap(loadAll(Messages.class),
        message -> message.getKey() + "." + message.getLocaleId());

    mainBundle = ResourceBundle.getBundle("i18n.main", getSelectedLocale());
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
    try {
      importCropData();
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

    scene = new Scene(root, 1366, 768);
    stage.setScene(scene);
    stage.setTitle("GSEHEN");
    stage.getIcons().add(new Image("/de/hgu/gsehen/images/Logo_UniGeisenheim_36x36.png"));
    stage.setMinWidth(root.minWidth(-1));
    stage.setMinHeight(root.minHeight(-1));
    stage.sizeToScene();
    stage.show();

    mainSplitPane = (SplitPane) scene.lookup(MAIN_SPLIT_PANE_ID);

    maps = new Maps(this, (WebView) scene.lookup(MAPS_WEB_VIEW_ID));
    fields = new Fields(this, (BorderPane) scene.lookup(FIELDS_VIEW_ID));
    plots = new Plots(this, (BorderPane) scene.lookup(PLOTS_VIEW_ID));
    logs = new Logs(this, (BorderPane) scene.lookup(LOGS_VIEW_ID));

    // dayDataCalculation = new DayDataCalculation();
    new Recommender();

    InputStream input = this.getClass()
        .getResourceAsStream("/de/hgu/gsehen/images/Logo_UniGeisenheim.png");
    Image image = new Image(input);
    ImageView imageView = (ImageView) scene.lookup(IMAGE_VIEW_ID);
    imageView.setImage(image);

    JFXTabPane tabPane = (JFXTabPane) stage.getScene().lookup(TAB_PANE_ID);
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
    treeTable.checkCalculation();
  }

  /**
   * PostgreSQL DB connection and storing in Persistence.
   *
   * @throws SQLException
   *           if SELECTing from PostgreSQL, our saving into local DB, fails
   */
  public static void importCropData() throws SQLException {
    final String url = "jdbc:postgresql:"
        + "//hs-geisenheim.cwliowbz3tsc.eu-west-1.rds.amazonaws.com/standard";
    final String user = "GSEHEN_user";
    final String password = "Yp4NiYiHYfmcHs7Fe2CEmTpLv";

    EntityManager em = Persistence.createEntityManagerFactory("GSEHEN").createEntityManager();
    CriteriaBuilder builder = em.getCriteriaBuilder();

    Connection connection = null;
    try {
      connection = DriverManager.getConnection(url, user, password);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Can't connect ", e);
    }
    try {
      em.getTransaction().begin();
      transfer(em, builder, connection, "SELECT * FROM crop;", new String[] { "cName" }, Crop.class,
          new String[] { "name" }, Gsehen::transferPropertiesFromPgToCrop);
      transfer(em, builder, connection, "SELECT * FROM messages;",
          new String[] { "key", "locale_id" }, Messages.class, new String[] { "key", "localeId" },
          Gsehen::transferPropertiesFromPgToMessages);
      em.getTransaction().commit();
    } catch (Exception e) {
      em.getTransaction().rollback();
    } finally {
      LOGGER.log(Level.INFO, "Loading from PostgreSQL was successful!");
      em.close();
    }
  }

  private static <T> void transfer(EntityManager em, CriteriaBuilder cb, Connection conn,
      final String sourceSql, final String[] sourceKey, final Class<T> targetClass,
      final String[] targetKey, BiConsumer<ResultSet, T> propertyTransfer) {
    if (sourceKey.length != targetKey.length) {
      throw new IllegalArgumentException("Transfer: source and target keys must have "
          + "the same number of elements! " + sourceKey.length + " != " + targetKey.length + "!");
    }
    CriteriaQuery<T> cq = cb.createQuery(targetClass);
    Root<T> cropRoot = cq.from(targetClass);
    List<ParameterExpression<String>> cropNameParameters = CollectionUtil.fillList(sourceKey.length,
        () -> cb.parameter(String.class));
    buildSelect(cb, cq, targetKey, cropRoot, cropNameParameters);

    TypedQuery<T> targetQuery = em.createQuery(cq);
    try (PreparedStatement statement = conn.prepareStatement(sourceSql)) {
      ResultSet sourceResultSet = executeQuery(statement);
      while (sourceResultSet.next()) {
        String[] keyParts = extractKeyParts(sourceResultSet, sourceKey);
        populateParameters(targetQuery, cropNameParameters, keyParts);
        T targetObject = getOrCreateTargetObject(keyParts, targetQuery, targetClass);
        propertyTransfer.accept(sourceResultSet, targetObject);
        em.persist(targetObject);
      }
    } catch (SQLException e) {
      System.out.println("no connection" + e.getLocalizedMessage());
    }
  }

  private static <T> T getOrCreateTargetObject(String[] keyParts, TypedQuery<T> targetQuery,
      final Class<T> targetClass) {
    T targetObject;
    try {
      // target object does already exist? => Update existing!
      targetObject = targetQuery.getSingleResult();
      LOGGER.log(Level.INFO,
          "Target " + targetClass.getSimpleName() + " " + keyParts + " existing: " + targetObject,
          targetObject);
    } catch (NoResultException e) {
      // target object does not exist yet? => Create new!
      try {
        targetObject = targetClass.newInstance();
      } catch (Exception e1) {
        throw new RuntimeException(
            "Can't create target " + targetClass.getSimpleName() + " instance", e);
      }
      LOGGER.log(Level.INFO,
          "Target " + targetClass.getSimpleName() + " " + keyParts + " new: " + targetObject,
          targetObject);
    }
    return targetObject;
  }

  private static String[] extractKeyParts(ResultSet sourceResultSet, String[] sourceKey)
      throws SQLException {
    String[] result = new String[sourceKey.length];
    int index = 0;
    for (String sourceKeyPart : sourceKey) {
      result[index++] = sourceResultSet.getString(sourceKeyPart);
    }
    return result;
  }

  private static <T> void populateParameters(TypedQuery<T> query,
      List<ParameterExpression<String>> parameters, String[] keyParts) {
    int index = 0;
    for (ParameterExpression<String> parameter : parameters) {
      query.setParameter(parameter, keyParts[index++]);
    }
  }

  private static <T> void buildSelect(CriteriaBuilder cb, CriteriaQuery<T> cq,
      final String[] targetKey, Root<T> root, List<ParameterExpression<String>> parameters) {
    List<Predicate> equalityParts = new ArrayList<>();
    int index = 0;
    for (String targetKeyPart : targetKey) {
      equalityParts.add(cb.equal(root.get(targetKeyPart), parameters.get(index++)));
    }
    cq.select(root).where(cb.and(equalityParts.toArray(new Predicate[equalityParts.size()])));
  }

  /**
   * Fill Crop with Data.
   * 
   * @param rs
   *          ResultSet from PostgreSQL.
   * @param crop
   *          New Crop
   * @throws SQLException
   *           if SELECTing from PostgreSQL, or saving into local DB, fails
   */
  private static void transferPropertiesFromPgToCrop(ResultSet rs, Crop crop) {
    try {
      crop.setName(rs.getString("cName"));
      crop.setActive(rs.getBoolean("cActive"));
      crop.setKc1(rs.getDouble("cKc1"));
      crop.setKc2(rs.getDouble("cKc2"));
      crop.setKc3(rs.getDouble("cKc3"));
      crop.setKc4(rs.getDouble("cKc4"));
      crop.setPhase1(rs.getInt("cPhase1"));
      crop.setPhase2(rs.getInt("cPhase2"));
      crop.setPhase3(rs.getInt("cPhase3"));
      crop.setPhase4(rs.getInt("cPhase4"));
      crop.setBbch1(rs.getString("cBbch1"));
      crop.setBbch2(rs.getString("cBbch2"));
      crop.setBbch3(rs.getString("cBbch3"));
      crop.setBbch4(rs.getString("cBbch4"));
      crop.setRootingZone1(rs.getInt("cRooting_Zone1"));
      crop.setRootingZone2(rs.getInt("cRooting_Zone2"));
      crop.setRootingZone3(rs.getInt("cRooting_Zone3"));
      crop.setRootingZone4(rs.getInt("cRooting_Zone4"));
      crop.setDescription(rs.getString("cDescription"));
    } catch (SQLException e) {
      throw new RuntimeException("Property transfer: can't get or set property", e);
    }
  }

  /**
   * Fills Messages with data.
   * 
   * @param rs
   *          ResultSet from PostgreSQL.
   * @param messages
   *          New Messages
   * @throws SQLException
   *           if SELECTing from PostgreSQL, or saving into local DB, fails
   */
  private static void transferPropertiesFromPgToMessages(ResultSet rs, Messages messages) {
    try {
      messages.setKey(rs.getString("key"));
      messages.setLocaleId(rs.getString("locale_id"));
      messages.setText(rs.getString("text"));
    } catch (SQLException e) {
      throw new RuntimeException("Property transfer: can't get or set property", e);
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
      Session session = em.unwrap(Session.class);
      Query<Farm> query = session.createQuery("from Farm");
      farmsList = query.list();
      // for (Farm farm : farmsList) {
      // System.out.println(farm.getUuid());
      // }
    } finally {
      em.close();
    }

    dataChanged = false;
    sendFarmDataChanged(null, null);
  }

  /**
   * Saves the user-created data (farms, fields, plots, ..)
   */
  public void saveUserData() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
    EntityManager em = emf.createEntityManager();

    try {
      em.getTransaction().begin();
      for (Farm farm : farmsList) {
        em.merge(farm);
      }
      for (SoilProfile soilProfile : soilProfilesList) {
        em.merge(soilProfile);
      }

      for (WeatherDataSource dataSource : weatherDataSourcesList) {
        em.merge(dataSource);
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

    dataChanged = false;
  }

  public <T extends GsehenEvent> void registerForEvent(Class<T> eventClass,
      GsehenEventListener<T> eventListener) {
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
   * Sends a "DayDataChanged" event to all listeners registered for that kind of event, except the
   * listeners that belong to the given "skipClass".
   *
   * @param dayData
   *          the current "DayData"
   * @param skipClass
   *          the event listener class to skip when iterating the listeners, or null
   */
  public void sendDayDataChanged(DayData dayData, WeatherDataSource weatherDataSource,
      Class<? extends GsehenEventListener<DayDataChanged>> skipClass) {
    DayDataChanged event = new DayDataChanged();
    event.setDayData(dayData);
    event.setWeatherDataSource(weatherDataSource);
    notifyEventListeners(event, skipClass);
  }

  /**
   * Sends a "ManualDataChanged" event to all listeners registered for that kind of event, except
   * the listeners that belong to the given "skipClass".
   *
   * @param field
   *          the field that is the parent of the given plot
   * @param plot
   *          the plot where the manual data has changed
   * @param skipClass
   *          the event listener class to skip when iterating the listeners, or null
   * @param date
   *          the date (day only) to which the manual data change applies
   */
  public void sendManualDataChanged(Field field, Plot plot, Date date,
      Class<? extends GsehenEventListener<DayDataChanged>> skipClass) {
    ManualDataChanged event = new ManualDataChanged();
    event.setField(field);
    event.setPlot(plot);
    event.setDate(date);
    notifyEventListeners(event, skipClass);
  }

  /**
   * Sends a "RecommendedActionChanged" event to all listeners registered for that kind of event,
   * except the listeners that belong to the given "skipClass".
   *
   * @param plot
   *          the plot for which the recommended action has changed
   * @param skipClass
   *          the event listener class to skip when iterating the listeners, or null
   */
  public void sendRecommendedActionChanged(Plot plot,
      Class<? extends GsehenEventListener<RecommendedActionChanged>> skipClass) {
    RecommendedActionChanged event = new RecommendedActionChanged();
    event.setPlot(plot);
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

  public void setFarmsList(List<Farm> farmsList) {
    this.farmsList = farmsList;
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

  public MainController getMainController() {
    return mainController;
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

  @SuppressWarnings({ "checkstyle:javadocmethod" })
  public static void updateDayData() {
    // dayDataCalculation.recalculateDayData();
    new PluginUtil().recalculateDayData();
  }

  @SuppressWarnings({ "checkstyle:javadocmethod" })
  public static <T> List<T> loadAll(final Class<T> queryRootClass) {
    EntityManager em = Persistence.createEntityManagerFactory("GSEHEN").createEntityManager();
    try {
      return DBUtil.createQueryAndList(em, queryRootClass);
    } finally {
      em.close();
    }
  }

  public List<SoilProfile> getSoilProfiles() {
    return soilProfilesList;
  }

  public List<WeatherDataSource> getWeatherDataSources() {
    return weatherDataSourcesList;
  }

  public List<Crop> getCrops() {
    return crops;
  }

  /**
   * Lookup method for a SoilProfile, using its UUID.
   *
   * @param soilProfileUuid
   *          the UUID of the SoilProfile to lookup
   * @return the SoilProfile with the given UUID, or null if no such SoilProfile exists
   */
  public SoilProfile getSoilProfileForUuid(String soilProfileUuid) {
    for (SoilProfile soilProfile : soilProfilesList) {
      if (soilProfile.getUuid().equals(soilProfileUuid)) {
        return soilProfile;
      }
    }
    return null;
  }

  /**
   * Lookup method for a WeatherDataSource, using its UUID.
   *
   * @param weatherDataSourceUuid
   *          the UUID of the WeatherDataSource to lookup
   * @return the WeatherDataSource with the given UUID, or null if no such WeatherDataSource exists
   */
  public WeatherDataSource getWeatherDataSourceForUuid(String weatherDataSourceUuid) {
    for (WeatherDataSource weatherDataSource : weatherDataSourcesList) {
      if (weatherDataSource.getUuid().equals(weatherDataSourceUuid)) {
        return weatherDataSource;
      }
    }
    return null;
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public void setSelectedLocale(Locale selectedLocale) {
    this.selectedLocale = selectedLocale;
    oneDecimalNumberFormat = (DecimalFormat) NumberFormat.getNumberInstance(selectedLocale);
    oneDecimalNumberFormat.applyPattern("#,##0.0");
    twoDecimalNumberFormat = (DecimalFormat) NumberFormat.getNumberInstance(selectedLocale);
    twoDecimalNumberFormat.applyPattern("#,##0.00");
    moreDecimalNumberFormat = (DecimalFormat) NumberFormat.getNumberInstance(selectedLocale);
    moreDecimalNumberFormat.applyPattern("#,#######0.0000000");
    dateFormat = new SimpleDateFormat("dd.MM.yyyy", selectedLocale);
  }

  public Locale getSelectedLocale() {
    return selectedLocale;
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public double parseDouble(String value) {
    try {
      return oneDecimalNumberFormat.parse(value).doubleValue();
    } catch (ParseException e) {
      throw new RuntimeException("Parsing double failed", e);
    }
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public String formatDoubleOneDecimal(double value) {
    return oneDecimalNumberFormat.format(value);
  }

  public String formatDoubleTwoDecimal(double value) {
    return twoDecimalNumberFormat.format(value);
  }

  public String formatDoubleMoreDecimal(double value) {
    return moreDecimalNumberFormat.format(value);
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public String localizeCropText(String messageKey) {
    final String messageKeyComplete = messageKey + "." + getSelectedLocale().getLanguage();
    final Messages message = messages.get(messageKeyComplete);
    if (message == null) {
      return "[" + messageKeyComplete + "]";
    }
    return message.getText();
  }

  public String formatDate(Date date) {
    return dateFormat.format(date);
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public boolean isParseable(String value) {
    try {
      parseDouble(value);
    } catch (RuntimeException e) {
      if (e.getCause() != null && e.getCause() instanceof ParseException) {
        return false;
      } else {
        throw e;
      }
    }
    return true;
  }

}
