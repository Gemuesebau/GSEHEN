package de.hgu.gsehen.util;

import de.hgu.gsehen.gsbalance.RecommendedAction;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageUtil {

  private static ResourceBundle logMessageBundle =
      ResourceBundle.getBundle("i18n.logmessages", Locale.ENGLISH);

  @SuppressWarnings("checkstyle:javadocmethod")
  public static String renderMessage(Locale locale, ResourceBundle bundle, RecommendedAction a) {
    DecimalFormat numberFormat = (DecimalFormat)NumberFormat.getNumberInstance(locale);
    numberFormat.applyPattern("#,##0.00");
    return MessageFormat.format(
        bundle.getString(a.getRecommendation().getMessagePropertyKey()),
        CollectionUtil.fillObjectArray(
            3,
            index -> formatDouble(a.getParameterValue(index), numberFormat)
        )
    );
  }

  private static Object formatDouble(Object value, NumberFormat numberFormat) {
    if (value instanceof Double) {
      return numberFormat.format((Double)value);
    } else {
      return value;
    }
  }

  /**
   * Localizable logging wrapper.
   *
   * @param logger the logger to use for the actual logging
   * @param level the log level to use
   * @param e the throwable that is the reason for this logging
   * @param logMessageKey the message bundle key of the log message
   * @param parameters the parameters for message formatting
   */
  public static void logException(Logger logger, Level level, Throwable e, String logMessageKey,
      Object... parameters) {
    logger.log(level, encodeBaseData(logMessageKey, parameters) + " " + MessageFormat.format(
        logMessageBundle.getString(logMessageKey),
        parameters
    ), e);
  }

  /**
   * Localizable logging wrapper.
   *
   * @param logger the logger to use for the actual logging
   * @param level the log level to use
   * @param logMessageKey the message bundle key of the log message
   * @param parameters the parameters for message formatting
   */
  public static void logMessage(Logger logger, Level level, String logMessageKey,
      Object... parameters) {
    logger.log(level, encodeBaseData(logMessageKey, parameters) + " " + MessageFormat.format(
        logMessageBundle.getString(logMessageKey),
        parameters
    ));
  }

  private static String encodeBaseData(String logMessageKey, Object... parameters) {
    List<Object> result = new ArrayList<>();
    result.add(logMessageKey);
    result.addAll(Arrays.asList(parameters));
    return result.toString();
  }
}
