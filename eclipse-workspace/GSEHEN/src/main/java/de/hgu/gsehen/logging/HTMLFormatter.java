package de.hgu.gsehen.logging;

import de.hgu.gsehen.util.MessageUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

/**
 * A log formatter that produces HTML.
 *
 * @author AT
 */
@SuppressWarnings({"checkstyle:abbreviationaswordinname", "checkstyle:rightcurly"})
public class HTMLFormatter extends SimpleFormatter {
  private SimpleDateFormat simpleDateFormat = Configurator.newSimpleDateFormat();

  private Pattern leadingWhitespace = Pattern.compile("^\\s");
  private static final String NO_BREAK_SPACE = "\u00A0";

  private Pattern newline = Pattern.compile("\\r?\\n");

  @Override
  public synchronized String format(LogRecord record) {
    String message = record.getMessage();
    if (message == null) {
      message = "";
    }
    Throwable thrown = record.getThrown();
    if (thrown != null) {
      message = MessageUtil.addParameterToMessage(message, completeMessage(thrown));
      record.setThrown(null);
    }
    record.setMessage(leadingWhitespace.matcher(
            newline.matcher(message).replaceAll(Configurator.NEWLINE_REPLACE)
    ).replaceFirst(NO_BREAK_SPACE));
    StringBuilder resultSB = new StringBuilder();
    resultSB.append(simpleDateFormat.format(new Date(record.getMillis())))
        .append(" ")
        .append(record.getLevel())
        .append(" ")
        .append(record.getMessage())
        .append("\n");
    return resultSB.toString();
  }

  private String completeMessage(Throwable thrown) {
    StringWriter thrownStackTrace = new StringWriter();
    thrown.printStackTrace(new PrintWriter(thrownStackTrace));
    String thrownCompleteMessage = NO_BREAK_SPACE + "\n"
              + thrown.getMessage() + "\n" + thrownStackTrace.toString();
    return thrownCompleteMessage;
  }
}
