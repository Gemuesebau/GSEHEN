package de.hgu.gsehen;

import static de.hgu.gsehen.util.CollectionUtil.addToMappedList;
import static de.hgu.gsehen.util.DBUtil.executeQuery;
import static de.hgu.gsehen.util.MessageUtil.logException;
import static de.hgu.gsehen.util.MessageUtil.logMessage;

import com.jfoenix.controls.JFXTabPane;

import de.hgu.gsehen.evapotranspiration.DayData;
import de.hgu.gsehen.event.DayDataChanged;
import de.hgu.gsehen.event.DrawableFilterChanged;
import de.hgu.gsehen.event.DrawableSelected;
import de.hgu.gsehen.event.FarmDataChanged;
import de.hgu.gsehen.event.GsehenEvent;
import de.hgu.gsehen.event.GsehenEventListener;
import de.hgu.gsehen.event.GsehenViewEvent;
import de.hgu.gsehen.event.ManualDataChanged;
import de.hgu.gsehen.event.RecommendedActionChanged;
import de.hgu.gsehen.gsbalance.Recommender;
import de.hgu.gsehen.gui.GeoPoint;
import de.hgu.gsehen.gui.GsehenTreeTable;
import de.hgu.gsehen.gui.controller.MainController;
import de.hgu.gsehen.gui.view.DataExport;
import de.hgu.gsehen.gui.view.FarmDataController;
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
import de.hgu.gsehen.model.Preferences;
import de.hgu.gsehen.model.SoilProfile;
import de.hgu.gsehen.model.WeatherDataSource;
import de.hgu.gsehen.util.CollectionUtil;
import de.hgu.gsehen.util.DBUtil;
import de.hgu.gsehen.util.Pair;
import de.hgu.gsehen.util.PluginUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
  private static final String SPLASH_WARNING_PROPNAME = "splashWarning";
  private static final String SPLASH_WARNING_FALSE = "false";
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
  private static final String EXPORTS_VIEW_ID = "#exportsBorderPane";
  private static final String IMAGE_VIEW_ID = "#imageView";

  private static Maps maps;
  private static Fields fields;
  private static Plots plots;
  private static Logs logs;
  private static DataExport exports;

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
  private List<Preferences> preferences;
  private Map<String, String> preferencesMap;
  
  private Double lat = 0.0;
  private Double lng = 0.0;

  {
    instance = this;

    soilProfilesList = loadAll(SoilProfile.class);
    weatherDataSourcesList = loadAll(WeatherDataSource.class);
    logMessage(LOGGER, Level.INFO, "loaded.croplist");
    crops = loadAll(Crop.class);
    messages = CollectionUtil.listToMap(loadAll(Messages.class),
        message -> message.getKey() + "." + message.getLocaleId());

    loadPreferences();
    setSelectedLocale(Locale.forLanguageTag(
        getPreferenceValue("locale", System.getProperty("locale", Locale.GERMAN.toLanguageTag()))
    ));
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
      importCropData(); // why here and not later (start, near loadFarmData, ...)? GSEH-16
    } catch (Exception e) {
      e.printStackTrace();
    }
    Application.launch(args);
  }

  private static void hideLaunch4JSplashScreen() {
    if (SPLASH_WARNING_FALSE.equalsIgnoreCase(System.getProperty(SPLASH_WARNING_PROPNAME))) {
      return;
    }
    try {
      Class.forName("com.install4j.api.launcher.SplashScreen").getMethod("hide", new Class[0])
        .invoke(null, new Object[0]);
    } catch (Exception e) {
      errorDialog(
          AlertType.WARNING,
          "gui.dialog.exception",
          "splash.screen.close.error.dialog.text",
          e.getClass().getName(),
          e.getMessage(),
          SPLASH_WARNING_PROPNAME,
          SPLASH_WARNING_FALSE
      );
    }
  }

  /**
   * Generate the Mainframe.
   * 
   * @see javafx.application.Application#start(javafx.stage.Stage)
   */
  @SuppressWarnings({ "checkstyle:rightcurly" })
  @Override
  public void start(Stage stage) {
    Thread.setDefaultUncaughtExceptionHandler((thread, e) -> errorDialog(
        AlertType.ERROR, "gui.dialog.exception", "unknown.application.error.dialog.text",
        renderWithCausesAndTargets(e)
    ));
    Parent root;
    try {
      logMessage(LOGGER, Level.INFO, "gsehen.about.to.load", MAIN_FXML);
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
    exports = new DataExport(this, (BorderPane) scene.lookup(EXPORTS_VIEW_ID));
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

    loadFarmData();

    treeTable = new GsehenTreeTable() {
      @Override
      public void handle(GsehenViewEvent event) {
      }
    };
    treeTable.addFarmTreeView(GsehenTreeTable.class);
    treeTable.checkCalculation();

    hideLaunch4JSplashScreen();
  }

  private StringBuilder renderWithCausesAndTargets(Throwable e) {
    return renderWithCausesAndTargets(e, "", "");
  }

  private StringBuilder renderWithCausesAndTargets(Throwable e, String title, String indenting) {
    StringBuilder builder = new StringBuilder(indenting + title + e.toString() + "\n");
    if (e.getCause() != null) {
      builder.append(renderWithCausesAndTargets(e.getCause(), "Cause: ", "  " + indenting));
    }
    if (e instanceof InvocationTargetException) {
      Throwable target = ((InvocationTargetException)e).getTargetException();
      if (target != null) {
        builder.append(renderWithCausesAndTargets(target, "Target: ", "  " + indenting));
      }
    }
    return builder;
  }

  /**
   * PostgreSQL DB connection and storing in Persistence.
   *
   * @throws SQLException
   *           if SELECTing from PostgreSQL, or saving into local DB, fails
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
    } catch (Exception e) {
      logMessage(LOGGER, Level.SEVERE, "import.crop.data.connection.error.heading", e);
      logException(LOGGER, Level.FINE, e, "import.crop.data.connection.error.details");
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
      logMessage(LOGGER, Level.INFO, "loading.from.postgresql.was.successful");
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
      logMessage(LOGGER, Level.WARNING, "gsehen.sql.connection.transfer.error", e);
    }
  }

  private static <T> T getOrCreateTargetObject(String[] keyParts, TypedQuery<T> targetQuery,
      final Class<T> targetClass) {
    T targetObject;
    try {
      // target object does already exist? => Update existing!
      targetObject = targetQuery.getSingleResult();
      logMessage(LOGGER, Level.INFO, "target.object.existing",
          targetClass.getSimpleName(), keyParts, targetObject);
    } catch (NoResultException e) {
      // target object does not exist yet? => Create new!
      try {
        targetObject = targetClass.newInstance();
      } catch (Exception e1) {
        throw new RuntimeException(
            "Can't create target " + targetClass.getSimpleName() + " instance", e);
      }
      logMessage(LOGGER, Level.INFO, "target.object.new",
          targetClass.getSimpleName(), keyParts, targetObject);
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
   * Loads the preferences (user-created data).
   */
  public void loadPreferences() {
    preferences = loadEntities(Preferences.class);
    preferencesMap = CollectionUtil.listToMap(preferences, p -> p.getKey(), p -> p.getValue());
  }

  /**
   * Loads the farms (fields, plots (water balance (day data))) (user-created data).
   */
  public void loadFarmData() {
    farmsList = loadEntities(Farm.class);
    dataChanged = false;
    sendFarmDataChanged(null, null);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> loadEntities(Class<T> entityClass) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
    EntityManager em = emf.createEntityManager();
    try {
      Session session = em.unwrap(Session.class);
      Query<T> query = session.createQuery("from " + entityClass.getSimpleName());
      return query.list();
    } finally {
      em.close();
    }
  }

  /**
   * Saves the user-created data (farms, fields, plots, ..)
   */
  public void saveUserData() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("GSEHEN");
    EntityManager em = emf.createEntityManager();

    try {
      em.getTransaction().begin();
      preferences = CollectionUtil.mapToList(preferencesMap,
          me -> new Preferences(me.getKey(), me.getValue()));
      processPreferences(em);
      processFarmsEtc(em);
      em.getTransaction().commit();
    } catch (Exception e) {
      logMessage(LOGGER, Level.WARNING, "save.user.data.db.error", e);
      em.getTransaction().rollback();
    } finally {
      em.close();
    }

    dataChanged = false;
  }

  private void processPreferences(EntityManager entityManager) {
    for (Preferences preferences : preferences) {
      entityManager.merge(preferences);
    }
  }

  private void processFarmsEtc(EntityManager entityManager) {
    for (Farm farm : farmsList) {
      entityManager.merge(farm);
    }
    for (SoilProfile soilProfile : soilProfilesList) {
      entityManager.merge(soilProfile);
    }
    for (WeatherDataSource dataSource : weatherDataSourcesList) {
      entityManager.merge(dataSource);
    }
    for (Farm deletedFarm : this.getDeletedFarms()) {
      entityManager.remove(
          entityManager.contains(deletedFarm) ? deletedFarm : entityManager.merge(deletedFarm));
    }
    getDeletedFarms().clear();
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
      logMessage(LOGGER, Level.INFO, "added.drawable.farm", object.getName());
    } else if (object instanceof Field) {
      Field fieldObj = (Field) object;
      getNewFieldsFarm().getFields().add(fieldObj);
      setLocation(fieldObj);
      fieldObj.setArea(fieldObj.getPolygon().calculateArea(fieldObj.getPolygon().getGeoPoints()));
      logMessage(LOGGER, Level.INFO, "added.drawable.field", object.getName());
    } else if (object instanceof Plot) {
      Plot plotObjc = (Plot) object;
      plotObjc.setIsActive(true);
      plotObjc.setArea(plotObjc.getPolygon().calculateArea(plotObjc.getPolygon().getGeoPoints()));
      getNewPlotsField(getNewFieldsFarm()).getPlots().add(plotObjc);
      logMessage(LOGGER, Level.INFO, "added.drawable.plot", object.getName());
    }
    sendFarmDataChanged(object, skipClass);
  }

  private void setLocation(Field field) {
    DecimalFormat df = new DecimalFormat("#.######");
    for (int y = 0; y < field.getPolygon().getGeoPoints().size(); y++) {
      lat += field.getPolygon().getGeoPoints().get(y).getLat();
      if (y == field.getPolygon().getGeoPoints().size() - 1) {
        lat = lat / field.getPolygon().getGeoPoints().size();
      }
    }
    for (int z = 0; z < field.getPolygon().getGeoPoints().size(); z++) {
      lng += field.getPolygon().getGeoPoints().get(z).getLng();
      if (z == field.getPolygon().getGeoPoints().size() - 1) {
        lng = lng / field.getPolygon().getGeoPoints().size();
      }
    }
    GeoPoint location = new GeoPoint(parseDouble(df.format(lat)),
        parseDouble(df.format(lng)));
    field.setLocation(location);
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

  public String getPreferenceValue(String key) {
    return preferencesMap.get(key);
  }

  public String getPreferenceValue(String key, String def) {
    return preferencesMap.containsKey(key) ? getPreferenceValue(key) : def;
  }

  public void setPreferenceValue(String key, String value) {
    preferencesMap.put(key, value);
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
  public void sendDayDataChanged(List<DayData> dayData, WeatherDataSource weatherDataSource,
      Class<? extends GsehenEventListener<DayDataChanged>> skipClass) {
    DayDataChanged event = new DayDataChanged();
    event.setDayData(dayData);
    event.setWeatherDataSource(weatherDataSource);
    notifyEventListeners(event, skipClass);
  }

  @SuppressWarnings("checkstyle:javadocmethod")
  public void sendDrawableFilterChanged(java.util.function.Predicate<Drawable> filter,
      Class<? extends GsehenEventListener<DrawableFilterChanged>> skipClass) {
    DrawableFilterChanged event = new DrawableFilterChanged();
    event.setFilter(filter);
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
   * Notifies listeners registered for the given (type of) event.
   *
   * @param event
   *          the actual event to be sent to the registered listeners
   * @param skipClass
   *          a listener class that shall be skipped when iterating the listeners, or null
   */
  @SuppressWarnings({ "unchecked" })
  private <T extends GsehenEvent> void notifyEventListeners(T event,
      Class<? extends GsehenEventListener<? extends T>> skipClass) {
    List<GsehenEventListener<?>> listeners = eventListeners.get(event.getClass());
    if (listeners != null) {
      listeners.forEach(listener -> {
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

  public static Fields getFields() {
    return fields;
  }

  public static Plots getPlots() {
    return plots;
  }

  public static Logs getLogs() {
    return logs;
  }

  public static DataExport getExports() {
    return exports;
  }

  public static void setExports(DataExport exports) {
    Gsehen.exports = exports;
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

  private static void errorDialog(AlertType alertType, String headerTextKey, String contentTextKey,
      Object... contentTextParameters) {
    Alert dialog = new Alert(alertType);
    dialog.getDialogPane().setMinWidth(600);
    dialog.setTitle(instance.getBundle().getString("gsehen.name"));
    dialog.setHeaderText(instance.getBundle().getString(headerTextKey));
    dialog.setContentText(MessageFormat.format(instance.getBundle().getString(contentTextKey),
        contentTextParameters));
    dialog.showAndWait();
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
    new PluginUtil().recalculateDayData(wdsUuid -> Recommender.clearDayData(wdsUuid));
    // man könnte auch die in Zeile 247 gebaute Instanz des Recommenders (s.o.) speichern,
    //   und alle Methoden dort non-static machen.
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
  public void setSelectedLocale(final Locale selectedLocale) {
    this.selectedLocale = selectedLocale;
    oneDecimalNumberFormat = (DecimalFormat) NumberFormat.getNumberInstance(selectedLocale);
    oneDecimalNumberFormat.applyPattern("#,##0.0");
    twoDecimalNumberFormat = (DecimalFormat) NumberFormat.getNumberInstance(selectedLocale);
    twoDecimalNumberFormat.applyPattern("#,##0.00");
    moreDecimalNumberFormat = (DecimalFormat) NumberFormat.getNumberInstance(selectedLocale);
    moreDecimalNumberFormat.applyPattern("#,#######0.0000000");
    dateFormat = new SimpleDateFormat("dd.MM.yyyy", selectedLocale);
    logMessage(LOGGER, Level.INFO, "gsehen.locale.applied", selectedLocale.toLanguageTag());
  }

  /**
   * Getter for selectedLocale.
   *
   * @return the currently selected application locale
   */
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
