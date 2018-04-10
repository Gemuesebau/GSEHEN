package de.hgu.gsehen;

import static de.hgu.gsehen.jdbc.DatabaseUtils.executeQuery;
import static de.hgu.gsehen.jdbc.DatabaseUtils.executeUpdate;
import static de.hgu.gsehen.jdbc.DatabaseUtils.parseYmd;

import de.hgu.gsehen.webview.Map;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * The GSEHEN main application.
 *
 * @author MO, AT
 */
@SuppressWarnings({"checkstyle:commentsindentation"})
public class Gsehen extends Application {

  private static final String GSEHEN_H2_LOCAL_DB = "gsehen-h2-local.db";
  private static final String DAYDATA_TABLE = "DAYDATA";

  private static final String MAIN_FXML = "main.fxml";

  public static final String WEB_VIEW_ID = "#webView";
  public static final String DEBUG_TEXTAREA_ID = "#debugTA";
  public static final String TAB_PANE_ID = "#tabPane";

  private static final Logger LOGGER = Logger.getLogger(Gsehen.class.getName());

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
//  @SuppressWarnings({"checkstyle:rightcurly"})
  public static void main(String[] args) {
//    try {
//      Server server = Server.createWebServer();
//      server.start();
//    }
//    catch (SQLException e) {
//      e.printStackTrace();
//    }
//    server.stop();
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
      root = FXMLLoader.load(getClass().getResource(MAIN_FXML));
    }
    catch (IOException e) {
      throw new RuntimeException(MAIN_FXML + " couldn't be loaded", e);
    }
    Scene scene = new Scene(root, 1280, 800);
    stage.setScene(scene);
    stage.sizeToScene();
    stage.show();

    WebEngine engine = ((WebView) stage.getScene().lookup(WEB_VIEW_ID)).getEngine();
    engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
      if (newState == State.SUCCEEDED) {
        engine.executeScript("initialize({"
            + " center: new google.maps.LatLng(52.266344, 10.519835),"
            + " zoom: 16, fullscreenControl: false"
            + " }); draw()");
      }
    });
    engine.loadContent(Map.getMapHtml());
    TabPane tabPane = (TabPane) stage.getScene().lookup(TAB_PANE_ID);
    tabPane.getSelectionModel().select(0);

    //TextArea debugTextArea = (TextArea) stage.getScene().lookup(DEBUG_TEXTAREA_ID);
    //testDatabase(debugTextArea);
  }

  @SuppressWarnings({"unused", "checkstyle:rightcurly"})
  private void testDatabase(TextArea debugTextArea) {
    Connection con = null;
    try {
      String jdbcUrl = "jdbc:h2:./" + GSEHEN_H2_LOCAL_DB + ";CIPHER=AES";
      con = DriverManager.getConnection(jdbcUrl, "", "OCddpvUe ");
      // PW: space is important! But this is just a test, must be supplied by user or the like
      LOGGER.info("Opened local H2 database at url " + jdbcUrl);
    }
    catch (SQLException e) {
      throw new RuntimeException(GSEHEN_H2_LOCAL_DB + " couldn't be opened", e);
    }
    // in h2, the DATE column type has no time information!
    //  id: http://www.h2database.com/html/datatypes.html#identity_type
    executeUpdate(con,
        "CREATE TABLE IF NOT EXISTS "
            + DAYDATA_TABLE
            + "(id IDENTITY, date DATE, t_min DOUBLE)",
        DAYDATA_TABLE
            + " couldn't be created");
    try (PreparedStatement insertDayData = con.prepareStatement(
        "INSERT INTO "
            + DAYDATA_TABLE
            + " (date, t_min)"
            + " VALUES(?, ?)")) {
      executeUpdate(insertDayData, parseYmd("2018-01-21"), 12.1);
      executeUpdate(insertDayData, parseYmd("2018-01-22"), 12.2);
      executeUpdate(insertDayData, parseYmd("2018-01-23"), 12.3);
      con.commit();
    }
    catch (SQLException e) {
      throw new RuntimeException(DAYDATA_TABLE + " values couldn't be inserted", e);
    }
    try (PreparedStatement selectDayData = con.prepareStatement("SELECT * FROM "
        + DAYDATA_TABLE
        + " WHERE date > ?")) {
      ResultSet rs = executeQuery(selectDayData, parseYmd("2018-01-20"));
      while (rs.next()) {
        debugTextArea.appendText("["
            + rs.getInt("id") + ", "
            + rs.getDate("date") + ", "
            + rs.getDouble("t_min")
            + "]\n");
      }
    }
    catch (SQLException e) {
      throw new RuntimeException(DAYDATA_TABLE + " values couldn't be selected", e);
    }
    
    if (con != null) {
      try {
        con.close();
      }
      catch (SQLException e) {
        throw new RuntimeException("DB connection couldn't be closed", e);
      }
    }
  }
}
