package de.hgu.gsehen.util;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.query.Query;

@SuppressWarnings({"checkstyle:abbreviationaswordinname","checkstyle:commentsindentation"})
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

  @SuppressWarnings({ "checkstyle:rightcurly", "checkstyle:javadocmethod" })
  public static <T> void saveEntity(T entity) {
    EntityManager em = Persistence.createEntityManagerFactory("GSEHEN").createEntityManager();
    try {
      em.getTransaction().begin();
      em.persist(entity);
      em.getTransaction().commit();
    }
    catch (Exception e) {
      em.getTransaction().rollback();
    }
    finally {
      em.close();
    }
  }

  @SuppressWarnings({ "checkstyle:javadocmethod", "unchecked" })
  public static <T> Query<T> createQuery(EntityManager em, Class<T> queryRoot) {
    return (Query<T>) em.unwrap(Session.class).createQuery("from " + queryRoot.getSimpleName());
  }

  @SuppressWarnings({ "checkstyle:javadocmethod" })
  public static <T> List<T> createQueryAndList(EntityManager em, Class<T> queryRoot) {
    return createQuery(em, queryRoot).list();
  }

  @SuppressWarnings({ "checkstyle:javadocmethod" })
  public static String generateUuid() {
    return UUID.randomUUID().toString();
  }
}
