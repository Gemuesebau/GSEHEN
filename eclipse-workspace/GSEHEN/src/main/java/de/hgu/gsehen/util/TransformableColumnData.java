package de.hgu.gsehen.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransformableColumnData {

  private static final String JAVA_LANG = "java.lang";
  private static final String JAVA_UTIL = "java.util";

  private static Class<?> findClass(String javaTypeSimpleName) {
    try {
      return Class.forName(JAVA_LANG + "." + javaTypeSimpleName);
    } catch (ClassNotFoundException e1) {
      try {
        return Class.forName(JAVA_UTIL + "." + javaTypeSimpleName);
      } catch (ClassNotFoundException e2) {
        throw new IllegalArgumentException("\"" + javaTypeSimpleName + "\" is not a valid \""
            + JAVA_LANG + "\" or \"" + JAVA_UTIL + "\" type simple name", e2);
      }
    }
  }

  private static Object parse(Format format, String input) {
    try {
      return format.parseObject(input);
    } catch (ParseException e) {
      throw new IllegalArgumentException("\"" + input + "\" is no valid input for " + format, e);
    }
  }

  public static NumberFormat newNumberFormat(String numberLocaleId) {
    Locale locale = Locale.ENGLISH; // useful if not re-throwing exception below
    try {
      locale = (Locale)Locale.class.getField(numberLocaleId).get(null);
    } catch (Exception e) {
      throw new IllegalArgumentException("\"" + numberLocaleId + "\" is not a valid "
            + "java Locale", e);
    }
    return newNumberFormat(locale);
  }

  private static NumberFormat newNumberFormat(Locale locale) {
    return NumberFormat.getNumberInstance(locale);
  }

  public static Function<String, Double> doubleParser(String numberLocaleId) {
    final NumberFormat format = newNumberFormat(numberLocaleId);
    return s -> ((Number)parse(format, s)).doubleValue();
  }

  public static Function<String, Double> doubleParser(Locale numberLocale) {
    final NumberFormat format = newNumberFormat(numberLocale);
    return s -> ((Number)parse(format, s)).doubleValue();
  }

  public static Function<String, Integer> intParser(String numberLocaleId) {
    final NumberFormat format = newNumberFormat(numberLocaleId);
    return s -> ((Number)parse(format, s)).intValue();
  }

  public static Function<String, Integer> intParser(Locale numberLocale) {
    final NumberFormat format = newNumberFormat(numberLocale);
    return s -> ((Number)parse(format, s)).intValue();
  }

  public static Function<Double, String> doubleFormatter(String numberLocaleId) {
    final NumberFormat format = newNumberFormat(numberLocaleId);
    return v -> format.format(v);
  }

  public static Function<Double, String> doubleFormatter(Locale numberLocale) {
    final NumberFormat format = newNumberFormat(numberLocale);
    return v -> format.format(v);
  }

  public static Function<Integer, String> intFormatter(String numberLocaleId) {
    final NumberFormat format = newNumberFormat(numberLocaleId);
    return v -> format.format(v);
  }

  public static Function<Integer, String> intFormatter(Locale numberLocale) {
    final NumberFormat format = newNumberFormat(numberLocale);
    return v -> format.format(v);
  }

  public static DateFormat newDateFormat(String dateFormatString) {
    return new SimpleDateFormat(dateFormatString);
  }

  public static Function<String, Date> dateParser(String dateFormat) {
    final DateFormat format = newDateFormat(dateFormat);
    return s -> (Date)parse(format, s);
  }

  public static Function<Date, String> dateFormatter(String dateFormat) {
    final DateFormat format = newDateFormat(dateFormat);
    return d -> format.format(d);
  }

  private class ColumnDefinition<T> {
    private String key;
    private Function<String, T> parser;
    private Function<T, T> transformer;
    private Function<T, String> formatter;

    private ColumnDefinition(String key, Class<T> type, Function<String, T> parser,
        Function<T, T> transformer, Function<T, String> formatter) {
      this.key = key;
      this.parser = parser;
      this.transformer = transformer;
      this.formatter = formatter;
    }

    private T transform(T value) {
      return transformer != null ? transformer.apply(value) : value;
    }

    private String stringToString(String columnString) {
      return formatter.apply(transform(parser.apply(columnString)));
    }
  }

  public static void main(String[] args) throws IOException {
    TransformableColumnData columnData =
        new TransformableColumnData(
            new ColumnDataText(
                ";",      // ;
                "\"",     // "
                "\"\"",   // ""
                m -> "\"" // "
                )
        );
    columnData.addColumnDefinition(0, "datetime", "Date", dateParser("d.M.y H:m:s"), null,
        dateFormatter("dd.MM.yyyy, HH:mm:ss"));
    columnData.addColumnDefinition(6, "batterymV", "Double", doubleParser("GERMAN"), v -> 1000 * v,
        doubleFormatter(Locale.forLanguageTag("de")));
    columnData.processAsRows(
        "C:\\Users\\atappe\\Desktop\\GSEHENWetter.csv",
        "utf-8",
        15,
        a -> System.out.println(Arrays.asList(a)),
        (i, l) -> l.get(0).length() > 0 && Character.isLetter(l.get(0).charAt(0))
    );
    //columnData.processAsDataObjects(
    //    "GSEHENWetter.csv",
    //    "utf-8",
    //    15,
    //    a -> System.out.println(a.getDateTime()),
    //    (i, l) -> l.get(0).length() > 0 && Character.isLetter(l.get(0).charAt(0))
    //);
  }

  private ColumnDataText columnDataText;
  private TreeMap<Integer, ColumnDefinition<?>> columnDefinitionMap;

  public TransformableColumnData(ColumnDataText columnDataText) {
    this.columnDataText = columnDataText;
    columnDefinitionMap = new TreeMap<>();
  }

  public <T> void addColumnDefinition(Integer inputColumnIndex,
      ColumnDefinition<T> columnDefinition) {
    columnDefinitionMap.put(inputColumnIndex, columnDefinition);
  }

  public <T> void addColumnDefinition(Integer inputColumnIndex, String key, Class<T> type,
      Function<String, T> parser, Function<T, T> transformer,
      Function<T, String> formatter) {
    addColumnDefinition(inputColumnIndex, new ColumnDefinition<T>(key, type, parser, transformer,
        formatter));
  }

  public <T> void addColumnDefinition(Integer inputColIndex, String key, String javaTypeSimpleName,
      Function<String, T> parser, Function<T, T> transformer, Function<T, String> formatter) {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>)findClass(javaTypeSimpleName);
    addColumnDefinition(inputColIndex, key, type, parser, transformer, formatter);
  }

  private void iterateOverColumns(List<String> columnStrings,
      BiConsumer<Integer, TransformableColumnData.ColumnDefinition<?>> columnHandler) {
    int columnCount = columnStrings.size();
    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      TransformableColumnData.ColumnDefinition<?> columnDefinition =
          columnDefinitionMap.get(columnIndex);
      columnHandler.accept(columnIndex, columnDefinition);
    }
  }

  public String[] getKeysRow(int columnCount) {
    String[] row = new String[columnCount];
    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      TransformableColumnData.ColumnDefinition<?> columnDefinition =
          columnDefinitionMap.get(columnIndex);
      row[columnIndex] = columnDefinition != null
          ? columnDefinition.key :
            null;
    }
    return row;
  }

  public void processAsRows(InputStream in, String charsetName, int maxLinesCount,
      Consumer<String[]> rowHandler, BiPredicate<Integer, List<String>> headingRowCheck)
          throws IOException {
    columnDataText.process(in, charsetName, maxLinesCount, (lineNumber, columnStrings) -> {
      boolean isHeadingRow = headingRowCheck.test(lineNumber, columnStrings);
      String[] row = new String[columnStrings.size()];
      iterateOverColumns(columnStrings, (columnIndex, columnDefinition) -> {
        String columnString = columnStrings.get(columnIndex);
        row[columnIndex] = !isHeadingRow && columnDefinition != null
            ? columnString + " -> " + columnDefinition.stringToString(columnString) :
              columnString;
      });
      rowHandler.accept(row);
    });
  }

  public void processAsRows(String fileName, String charsetName, int maxLinesCount,
      Consumer<String[]> rowHandler, BiPredicate<Integer, List<String>> headingRowCheck)
          throws IOException {
    processAsRows(new FileInputStream(fileName), charsetName, maxLinesCount, rowHandler,
        headingRowCheck);
  }
}
