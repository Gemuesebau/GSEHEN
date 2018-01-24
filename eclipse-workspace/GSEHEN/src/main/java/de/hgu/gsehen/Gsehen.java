package de.hgu.gsehen;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * The GSEHEN main application.
 *
 * @author MO, AT
 */
public class Gsehen extends Application {

  private static final String DAYDATA_TABLE = "DAYDATA";
  private static final String GSEHEN_H2_LOCAL_DB = "gsehen-h2-local.db";
  private static final String WEB_VIEW_ID = "#webView";
  private static final String DEBUG_TEXTAREA_ID = "#debugTA";
  private static final String TAB_PANE_ID = "#tabPane";
  private static final String MAIN_FXML = "main.fxml";

  /**
   * Main method.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Application.launch(args);
  }

  @SuppressWarnings({"checkstyle:rightcurly"})
  private Date parseYmd(String source) {
    try {
      return new Date(new SimpleDateFormat("yyyy-MM-dd").parse(source).getTime());
    }
    catch (ParseException e) {
      throw new RuntimeException(source + " couldn't be parsed as a local DATE", e);
    }
  }

  private void executeUpdateIntDateDouble(PreparedStatement prepStmt,
      int param1, Date param2, double param3) throws SQLException {
    prepStmt.setInt(1, param1);
    prepStmt.setDate(2, param2);
    prepStmt.setDouble(3, param3);
    prepStmt.executeUpdate();
  }

  private ResultSet executeWhereDate(PreparedStatement prepStmt,
      Date whereDate) throws SQLException {
    prepStmt.setDate(1, whereDate);
    return prepStmt.executeQuery();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javafx.application.Application#start(javafx.stage.Stage)
   */
  @SuppressWarnings({"checkstyle:rightcurly", "checkstyle:commentsindentation"})
  @Override
  public void start(Stage stage) {
    Parent root;
    try {
      root = FXMLLoader.load(getClass().getResource(MAIN_FXML));
    }
    catch (IOException e) {
      throw new RuntimeException(MAIN_FXML + " couldn't be loaded", e);
    }
    Scene scene = new Scene(root, 1280, 768);
    stage.setScene(scene);
    stage.sizeToScene();
    stage.show();
//    WebEngine engine = ((WebView) stage.getScene().lookup(WEB_VIEW_ID)).getEngine();
//    engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
//      if (newState == State.SUCCEEDED) {
//        engine.executeScript("initialize({"
//            + " center: new google.maps.LatLng(52.266344, 10.519835),"
//            + " zoom: 16, fullscreenControl: false"
//            + " }); draw()");
//      }
//    });
//    engine.loadContent(Map.getMapHtml());
    TabPane tabPane = (TabPane) stage.getScene().lookup(TAB_PANE_ID);
    tabPane.getSelectionModel().select(1);
    TextArea debugTextArea = (TextArea) stage.getScene().lookup(DEBUG_TEXTAREA_ID);
    
    Connection con = null;
    Properties connectionProps = new Properties();
//    connectionProps.put("user", this.userName);
//    connectionProps.put("password", this.password);
    try {
      con = DriverManager.getConnection(
        "jdbc:h2:./" + GSEHEN_H2_LOCAL_DB,
        connectionProps);
    }
    catch (SQLException e) {
      throw new RuntimeException(GSEHEN_H2_LOCAL_DB + " couldn't be opened", e);
    }
    // in h2, the DATE column type has no time information!
    try (Statement stmt = con.createStatement()) {
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS "
          + DAYDATA_TABLE
          + "(id INT PRIMARY KEY, date DATE, t_min DOUBLE)");
    }
    catch (SQLException e) {
      throw new RuntimeException(DAYDATA_TABLE + " couldn't be created", e);
    }
    try (PreparedStatement insertDayData = con.prepareStatement("INSERT INTO "
        + DAYDATA_TABLE
        + " VALUES(?, ?, ?)")) {
      executeUpdateIntDateDouble(insertDayData, 1, parseYmd("2018-01-21"), 12.1);
      executeUpdateIntDateDouble(insertDayData, 2, parseYmd("2018-01-22"), 12.2);
      executeUpdateIntDateDouble(insertDayData, 3, parseYmd("2018-01-23"), 12.3);
      con.commit();
    }
    catch (SQLException e) {
      throw new RuntimeException(DAYDATA_TABLE + " values couldn't be inserted", e);
    }
    try (PreparedStatement selectDayData = con.prepareStatement("SELECT * FROM "
        + DAYDATA_TABLE
        + " WHERE date > ?")) {
      ResultSet rs = executeWhereDate(selectDayData, parseYmd("2018-01-20"));
      while (rs.next()) {
        debugTextArea.appendText("["
            + rs.getInt("id")
            + ", "
            + rs.getDate("date")
            + ", "
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
