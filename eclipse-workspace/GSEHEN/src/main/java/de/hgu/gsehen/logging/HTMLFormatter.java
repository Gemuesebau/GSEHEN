package de.hgu.gsehen.logging;

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
  private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  private Pattern leadingWhitespace = Pattern.compile("^\\s");
  private static final String LEADING_WHITESPACE_REPLACE = "\u00A0";

  private Pattern newline = Pattern.compile("\\r?\\n");
  private static final String NEWLINE_REPLACE = "<br>";

  @Override
  public synchronized String format(LogRecord record) {
    if (record.getThrown() != null) {
      StringWriter stringWriter = new StringWriter();
      record.getThrown().printStackTrace(new PrintWriter(stringWriter));
      record.setMessage(record.getMessage() + "\n\u00A0\n"
          + record.getThrown().getMessage() + "\n"
          + stringWriter.toString());
      record.setThrown(null);
    }
    record.setMessage(
        leadingWhitespace.matcher(
          newline.matcher(
            record.getMessage()
          ).replaceAll(NEWLINE_REPLACE)
        ).replaceAll(LEADING_WHITESPACE_REPLACE)
    );
    StringBuilder resultSB = new StringBuilder();
    resultSB.append(simpleDateFormat.format(new Date(record.getMillis())))
      .append(" ")
      .append(record.getLevel())
      .append(" ")
      .append(record.getMessage())
      .append("\n");
    return resultSB.toString();
  }
}
