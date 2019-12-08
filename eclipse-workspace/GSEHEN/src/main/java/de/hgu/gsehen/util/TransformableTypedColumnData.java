package de.hgu.gsehen.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import de.hgu.gsehen.model.SimpleWeatherData;

public class TransformableTypedColumnData<D> {

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

  private D createDataObject() {
    try {
      return dataClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Can't create new instance of class " + dataClass, e);
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
      }
      catch (Exception e) {
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

  public static Function<String, Integer> intParser(String numberLocaleId) {
    final NumberFormat format = newNumberFormat(numberLocaleId);
    return s -> ((Number)parse(format, s)).intValue();
  }

  public static Function<String, Double> doubleParser(Locale numberLocale) {
    final NumberFormat format = newNumberFormat(numberLocale);
    return s -> ((Number)parse(format, s)).doubleValue();
  }

  public static Function<String, Integer> intParser(Locale numberLocale) {
    final NumberFormat format = newNumberFormat(numberLocale);
    return s -> ((Number)parse(format, s)).intValue();
  }

  public static Function<Double, String> doubleFormatter(String numberLocaleId) {
    final NumberFormat format = newNumberFormat(numberLocaleId);
    return v -> format.format(v);
  }

  public static Function<Integer, String> intFormatter(String numberLocaleId) {
    final NumberFormat format = newNumberFormat(numberLocaleId);
    return v -> format.format(v);
  }

  public static Function<Double, String> doubleFormatter(Locale numberLocale) {
    final NumberFormat format = newNumberFormat(numberLocale);
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
    private Class<T> type;
    private Function<String, T> parser;
    private Function<T, T> transformer;
    private Function<T, String> formatter;
    private BiConsumer<D, T> setter;

    private ColumnDefinition(String key, Class<T> type, Function<String, T> parser,
        Function<T, T> transformer, Function<T, String> formatter, BiConsumer<D, T> setter) {
      this.key = key;
      this.type = type;
      this.parser = parser;
      this.transformer = transformer;
      this.formatter = formatter;
      this.setter = setter;
    }

    private void setTo(String field, D data) {
      setter.accept(data, transformer.apply(parser.apply(field)));
    }

    private String stringToString(String field) {
      return formatter.apply(transformer.apply(parser.apply(field)));
    }
  }

  public static void main(String[] args) throws IOException {
    TransformableTypedColumnData<SimpleWeatherData> columnData =
        new TransformableTypedColumnData<SimpleWeatherData>(
            new ColumnDataText(
                ";",      // ;
                "\"",     // "
                "\"\"",   // ""
                m -> "\"" // "
                ),
            SimpleWeatherData.class
        );
    columnData.addColumnDefinition(0, "datetime", "Date", dateParser("d.M.y H:m:s"), null,
        dateFormatter("dd.MM.yyyy, HH:mm:ss"), (d, v) -> d.setDateTime(v));
    columnData.addColumnDefinition(6, "batterymV", "Double", doubleParser("GERMAN"), v -> 1000 * v,
        doubleFormatter(Locale.forLanguageTag("de")),
        (d, v) -> System.out.println("Data object of type " + d.getClass().getSimpleName() + "has "
            + "no property to take battery millivolts " + v));
    columnData.process(
        null,//"C:\\Users\\Alex\\Google Drive\\CGS\\GSEHEN\\Wetterdaten\\GSEHENWetter.csv",
        "utf-8",
        15
    );
  }

  private ColumnDataText columnDataText;
  private TreeMap<Integer, ColumnDefinition<?>> columnDefinitionMap;
  private Class<D> dataClass;

  public TransformableTypedColumnData(ColumnDataText columnDataText, Class<D> dataClass) {
    this.columnDataText = columnDataText;
    this.dataClass = dataClass;
    columnDefinitionMap = new TreeMap<>();
  }

  public <T> void addColumnDefinition(Integer inputColumnIndex,
      ColumnDefinition<T> columnDefinition) {
    columnDefinitionMap.put(inputColumnIndex, columnDefinition);
  }

  public <T> void addColumnDefinition(Integer inputColumnIndex, String key, Class<T> type,
      Function<String, T> parser, Function<T, T> transformer, Function<T, String> formatter,
      BiConsumer<D, T> setter) {
    addColumnDefinition(inputColumnIndex, new ColumnDefinition<T>(key, type, parser, transformer, formatter, setter));
  }

  public <T> void addColumnDefinition(Integer inputColIndex, String key, String javaTypeSimpleName,
      Function<String, T> parser, Function<T, T> transformer, Function<T, String> formatter,
      BiConsumer<D, T> setter) {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>)findClass(javaTypeSimpleName);
    addColumnDefinition(inputColIndex, key, type, parser, transformer, formatter, setter);
  }

//  public void process(String fileName, String charsetName, int maxLinesCount,
//      BiConsumer<Integer, List<String>> lineResultHandler) throws IOException {
//    process(new FileInputStream(fileName), charsetName, maxLinesCount, lineResultHandler);
//  }

  public void process(InputStream in, String charsetName, int maxLinesCount/*,
      BiConsumer<Integer, List<String>> lineResultHandler*/) throws IOException {
    columnDataText.process(in, charsetName, maxLinesCount, (lineNumber, columnStrings) -> {
      D dataObject = createDataObject();
      int columnCount = columnStrings.size();
      for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
        String columnString = columnStrings.get(columnIndex);
        TransformableTypedColumnData<D>.ColumnDefinition<?> columnDefinition =
            columnDefinitionMap.get(columnIndex);
        columnDefinition.stringToString(columnString);
      }
    });
  }
}
