package de.hgu.gsehen.util;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

@SuppressWarnings({"checkstyle:abbreviationaswordinname"})
public class DBUtil {

  /**
   * Parses a date string in format yyyy-MM-dd.
   *
   * @param source a date string without time and timezone information(!)
   * @return a java.sql.Date corresponding to the given local date
   */
  @SuppressWarnings({"checkstyle:rightcurly"})
  public static Date parseYmd(String source) {
    try {
      return new Date(new SimpleDateFormat("yyyy-MM-dd").parse(source).getTime());
    }
    catch (ParseException e) {
      throw new RuntimeException(source + " couldn't be parsed as a local DATE", e);
    }
  }

  /**
   * Executes an update for a given connection, and wraps any checked exceptions.
   *
   * @param con a database connection
   * @param sqlString an SQL string
   * @param exceptionMessage the message for the runtime exception wrapped around any
   *     occurring checked exception
   */
  @SuppressWarnings({"checkstyle:rightcurly"})
  public static void executeUpdate(Connection con,
      String sqlString, String exceptionMessage) {
    try (Statement stmt = con.createStatement()) {
      stmt.executeUpdate(sqlString);
    }
    catch (SQLException e) {
      throw new RuntimeException(exceptionMessage, e);
    }
  }

  /**
   * Executes an update for a given prepared statement, setting the given parameters.
   *
   * @param preparedStatement a prepared statement with place-holders (question marks)
   * @param parameters the parameters to set - must match the statement's place-holders!
   * @throws SQLException if issued by delegate call to preparedStatement.executeUpdate
   */
  public static void executeUpdate(PreparedStatement preparedStatement,
      Object... parameters) throws SQLException {
    for (int parameterIndex = 1; parameterIndex <= parameters.length; parameterIndex++) {
      preparedStatement.setObject(parameterIndex, parameters[parameterIndex - 1]);
    }
    preparedStatement.executeUpdate();
  }

  /**
   * Executes an update for a given prepared statement, setting the given parameters.
   *
   * @param preparedStatement a prepared statement with place-holders (question marks)
   * @param parameters the parameters to set - must match the statement's place-holders!
   * @return the query's result set
   * @throws SQLException if issued by delegate call to preparedStatement.executeUpdate
   */
  public static ResultSet executeQuery(PreparedStatement preparedStatement,
      Object... parameters) throws SQLException {
    for (int parameterIndex = 1; parameterIndex <= parameters.length; parameterIndex++) {
      preparedStatement.setObject(parameterIndex, parameters[parameterIndex - 1]);
    }
    return preparedStatement.executeQuery();
  }

  /**
   * Saves a JPA Entity.
   *
   * @param entity the entity to save
   */
  @SuppressWarnings("checkstyle:rightcurly")
  public static <T> T saveEntity(T entity) {
    EntityManager em = Persistence.createEntityManagerFactory("GSEHEN").createEntityManager();
    T merged = null;
    try {
      em.getTransaction().begin();
      merged = em.merge(entity);
      em.getTransaction().commit();
    }
    catch (Exception e) {
      em.getTransaction().rollback();
    }
    finally {
      em.close();
    }
    return merged;
  }
}
