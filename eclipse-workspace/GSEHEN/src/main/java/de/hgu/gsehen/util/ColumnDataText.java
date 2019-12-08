package de.hgu.gsehen.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnDataText {

  public static void main(String[] args) throws IOException {
    new ColumnDataText(
        ";",      // ;
        "\"",     // "
        "\"\"",   // ""
        m -> "\"" // "
    ).process(
        "test.csv",
        "utf-8",
        15,
        (i, a) -> System.out.println("#" + i + " " + a)
    );
  }

  private char quoteChar = '"';
  private char separatorChar = ';';
  private Pattern pattern;
  private Function<Matcher, String> replacer;

  public ColumnDataText(String separatorCharString, String quoteCharString,
      String quotedStringRE, Function<Matcher, String> replacer) {
    if (quoteCharString != null && quoteCharString.length() == 1) {
      this.quoteChar = quoteCharString.charAt(0);
    }
    if (separatorCharString != null && separatorCharString.length() == 1) {
      this.separatorChar = separatorCharString.charAt(0);
    }
    pattern = Pattern.compile("^" + quotedStringRE);
    this.replacer = replacer;
  }

  private class FieldStatus {
    private StringBuilder currentField;
    private boolean isQuoted;
    private boolean atFieldStart;

    private FieldStatus() {
      reset();
    }

    private void reset() {
      currentField = new StringBuilder();
      isQuoted = false;
      atFieldStart = true;
    }
  }

  private List<String> parseLine(String line) {
    List<String> parsed = new ArrayList<>();
    final int lineLength = line.length();
    FieldStatus status = new FieldStatus();
    for (int i = 0; i < lineLength; i++) {
      if (status.atFieldStart) {
        status.atFieldStart = false;
        if (line.charAt(i) == quoteChar) {
          status.isQuoted = true;
          continue;
        }
      }
      if (status.isQuoted) {
        Matcher matcher = pattern.matcher(line.substring(i));
        if (matcher.matches()) {
          status.currentField.append(replacer.apply(matcher));
          i += (matcher.group().length() - 1);
          continue;
        }
        if (line.charAt(i) == quoteChar) {
          status.isQuoted = false;
          continue;
        }
      }
      if (line.charAt(i) == separatorChar) {
        parsed.add(status.currentField.toString());
        status.reset();
        continue;
      }
      status.currentField.append(line.charAt(i));
    }
    parsed.add(status.currentField.toString());
    return parsed;
  }

  public void process(String fileName, String charsetName, int maxLinesCount,
      BiConsumer<Integer, List<String>> lineResultHandler) throws IOException {
    process(new FileInputStream(fileName), charsetName, maxLinesCount, lineResultHandler);
  }

  public void process(InputStream in, String charsetName, int maxLinesCount,
      BiConsumer<Integer, List<String>> lineResultHandler) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetName));
    int lineNum = 0;
    String line;
    while ((maxLinesCount < 0 || lineNum < maxLinesCount) && (line = reader.readLine()) != null) {
      lineResultHandler.accept(lineNum++, parseLine(line));
    }
    reader.close();
  }
}
