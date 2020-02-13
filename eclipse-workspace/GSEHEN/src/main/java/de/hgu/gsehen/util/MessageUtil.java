package de.hgu.gsehen.util;

import de.hgu.gsehen.Gsehen;
import de.hgu.gsehen.gsbalance.RecommendedAction;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

  public static final char END_OF_I18N_DATA = '¦';
  public static final char END_OF_VALUE = '´';
  public static final String END_OF_VALUE_STR = "" + END_OF_VALUE;

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

  public static String addParameterToMessage(String logFileMessage, String parameter) {
    int indexOfPipe = logFileMessage.indexOf(END_OF_I18N_DATA);
    if (indexOfPipe == -1) {
      return logFileMessage + "\n" + parameter;
    } else {
      return logFileMessage.substring(0, indexOfPipe) + END_OF_VALUE
          + replaceSeparatorChars(parameter) + logFileMessage.substring(indexOfPipe);
    }
  }

  private static Pattern numberPattern = Pattern.compile("\\d+");
  private static Pattern parameterPattern = Pattern.compile("\\{(\\d+)");

  public static String localizedLogMessage(String logFileMessage) {
    int indexOfPipe = logFileMessage.indexOf(END_OF_I18N_DATA);
    if (indexOfPipe == -1) {
      return logFileMessage;
    }
    String[] msgParts = logFileMessage.substring(0, indexOfPipe).split(END_OF_VALUE_STR);
    Object[] parameters = new Object[msgParts.length - 1];
    for (int i = 0; i < parameters.length; i++) {
      Object msgPart = msgParts[i + 1];
      if (numberPattern.matcher((String)msgPart).matches()) {
        msgPart = Long.valueOf((String)msgPart);
      }
      parameters[i] = msgPart;
    }
    String parameterizedMessage = Gsehen.getInstance().getLogBundle().getString(msgParts[0]);
    int maxParameterIndex = -1;
    Matcher parametersMatcher = parameterPattern.matcher(parameterizedMessage);
    while (parametersMatcher.find()) {
      int parameterIndex = Integer.parseInt(parametersMatcher.group(1));
      if (parameterIndex > maxParameterIndex) {
        maxParameterIndex = parameterIndex;
      }
    }
    for (int i = maxParameterIndex + 1; i < parameters.length; i++) {
      parameterizedMessage += "\n{" + i + "}";
    }
    return MessageFormat.format(parameterizedMessage, parameters);
  }

  private static String replaceSeparatorChars(Object value) {
    return String.valueOf(value).replace(END_OF_VALUE, '\'').replace(END_OF_I18N_DATA, '|');
  }

  private static StringBuilder encodeBaseData(String logMessageKey, Object... parameters) {
    StringBuilder result = new StringBuilder(replaceSeparatorChars(logMessageKey));// "invalid" key!
    for (Object object : parameters) {
      result.append(END_OF_VALUE).append(replaceSeparatorChars(object));
    }
    return result.append(END_OF_I18N_DATA);
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

  /**
   * Logging wrapper.
   *
   * @param logger the logger to use for the actual logging
   * @param level the log level to use
   * @param logMessage the log message
   * @param parameters the parameters for message formatting
   */
  public static void logMessageRaw(Logger logger, Level level, String logMessage,
      Object... parameters) {
    logger.log(level, MessageFormat.format(logMessage, parameters));
  }
}
