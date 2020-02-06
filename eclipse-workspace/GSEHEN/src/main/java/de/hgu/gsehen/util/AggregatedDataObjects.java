package de.hgu.gsehen.util;

import static de.hgu.gsehen.util.TransformableColumnData.dateFormatter;
import static de.hgu.gsehen.util.TransformableColumnData.dateParser;

import de.hgu.gsehen.evapotranspiration.DayData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AggregatedDataObjects<D> {
  private TransformableColumnData columnData;
  private Map<Integer, List<?>> listsMap;
  private Map<Integer, Aggregator<?>> aggregatorMap;
  private Map<Integer, BiConsumer<D, ?>> setterMap;
  private Map<String, Integer> columnsByKey;

  public static Aggregator<Double> doubleMin() {
    return dList -> dList.stream().mapToDouble(d -> d).min().getAsDouble();
  }

  public static Aggregator<Double> doubleMax() {
    return dList -> dList.stream().mapToDouble(d -> d).max().getAsDouble();
  }

  public static Aggregator<Double> doubleMean() {
    return dList -> dList.stream().mapToDouble(d -> d).average().getAsDouble();
  }

  public static Aggregator<Double> doubleSum() {
    return dList -> dList.stream().mapToDouble(d -> d).sum();
  }

  public <T> void addColumnDefinition(Integer inputColIndex, String key, String javaType,
      Function<String, T> parser, Function<T, T> transformer, Function<T, String> formatter,
      Aggregator<T> aggregator, BiConsumer<D, T> setter) {
    columnData.addColumnDefinition(inputColIndex, key, javaType, parser, transformer, formatter);
    aggregatorMap.put(inputColIndex, aggregator);
    setterMap.put(inputColIndex, setter);
    columnsByKey.put(key, inputColIndex);
  }

  public AggregatedDataObjects(TransformableColumnData columnData) {
    this.columnData = columnData;
    listsMap = new TreeMap<>();
    aggregatorMap = new TreeMap<>();
    setterMap = new TreeMap<>();
    columnsByKey = new TreeMap<>();
  }

  public class ColumnValues {
    private Object[] values;

    public ColumnValues() {
      this.values = null;
    }

    public ColumnValues(Object[] values) {
      this.values = values;
    }

    public Object getValue(String key) {
      try {
        return values[columnsByKey.get(key)];
      } catch (Exception e) {
        return null;
      }
    }

    public void setValues(ColumnValues other) {
      values = other.values;
    }

    public boolean hasValues() {
      return values != null;
    }
  }

  public void process(String fileName, String charsetName, int maxLinesCount,
      BiPredicate<Integer, List<String>> headingRowCheck,
      BiPredicate<ColumnValues, ColumnValues> newGroup,
      Supplier<D> newDataObjectSupplier, Consumer<D> newDataObjectHandler)
          throws IOException {
    initializeLists();
    ColumnValues last = new ColumnValues();
    columnData.process(fileName, charsetName, maxLinesCount,
        values -> {
          ColumnValues current = new ColumnValues(values);
          if (last.hasValues() && newGroup.test(last, current)) {
            aggregateToObject(newDataObjectSupplier, newDataObjectHandler);
          }
          last.setValues(current);
        }, (columnIndex, value) -> {
          @SuppressWarnings("unchecked")
          List<Object> list = (List<Object>)listsMap.get(columnIndex);
          if (list != null) {
            list.add(value);
          }
        }, headingRowCheck);
    aggregateToObject(newDataObjectSupplier, newDataObjectHandler);
  }

  @SuppressWarnings("unchecked")
  private void aggregateToObject(Supplier<D> dataObjectSupplier, Consumer<D> newDataObjectHandler) {
    D dataObject = dataObjectSupplier.get();
    forAllColumns(inputColIndex -> {
      List<?> list = listsMap.get(inputColIndex);
      if (list == null || list.isEmpty()) {
        return;
      } else {
        ((BiConsumer<D, Object>)setterMap.get(inputColIndex)).accept(dataObject,
            ((Aggregator<Object>)aggregatorMap.get(inputColIndex)).apply((List<Object>)list));
      }
    });
    initializeLists();
    if (newDataObjectHandler != null) {
      newDataObjectHandler.accept(dataObject);
    }
  }

  private void initializeLists() {
    forAllColumns(inputColIndex -> {
      List<?> list = listsMap.get(inputColIndex);
      if (list == null) {
        listsMap.put(inputColIndex, new ArrayList<Object>());
      } else {
        list.clear();
      }
    });
  }

  private void forAllColumns(Consumer<Integer> columnIndexHandler) {
    for (Integer inputColIndex : aggregatorMap.keySet()) {
      columnIndexHandler.accept(inputColIndex);
    }
  }

  public static void main(String[] args) throws IOException {
    AggregatedDataObjects<DayData> objects =
        new AggregatedDataObjects<>(
            new TransformableColumnData(
                new ColumnDataText(
                    ";",      // ;
                    "\"",     // "
                    "\"\"",   // ""
                    m -> "\"" // "
                )
            )
        );
    objects.addColumnDefinition(0, "datetime", "Date", dateParser("d.M.y H:m:s"), null,
        dateFormatter("dd.MM.yyyy, HH:mm:ss"),
        dtList -> dtList.get(dtList.size() - 1), (dd, dt) -> dd.setDate(dt));
    objects.process(
        "",
        "utf-8",
        -1,
        (i, l) -> l.get(0).length() > 0 && Character.isLetter(l.get(0).charAt(0)),
        (last, current) -> !DateUtil.sameDay(
            (Date)current.getValue("datetime"), (Date)last.getValue("datetime")),
        () -> new DayData(),
        d -> System.out.println(d.getDate())
    );
  }
}
