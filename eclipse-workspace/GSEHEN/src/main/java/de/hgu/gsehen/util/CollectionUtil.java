package de.hgu.gsehen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;

public class CollectionUtil {

  /**
   * Adds a value to a list that is itself contained in a map.
   * If there isn't yet a list mapped by the given key, it is created.
   *
   * @param map the map containing the lists (as map values)
   * @param key the key for looking up (or putting) the mapped list
   * @param listValue the value to add to the mapped list
   */
  public static <K, V> void addToMappedList(java.util.Map<K, List<V>> map, K key, V listValue) {
    List<V> list = map.get(key);
    if (list == null) {
      list = new ArrayList<V>();
      map.put(key, list);
    }
    list.add(listValue);
  }

  /**
   * Creates a map that contains the given classes as values,
   * mapped by their respective "simpleName".
   *
   * @param clazzes the Class objects to map
   * @return a map containing the given classes, by their respective simple names
   */
  public static Map<String, Class<?>> simpleClassMap(Class<?>[] clazzes) {
    Map<String, Class<?>> result = new TreeMap<>();
    for (Class<?> clazz : clazzes) {
      result.put(clazz.getSimpleName(), clazz);
    }
    return result;
  }

  /**
   * Returns the first non-null object in the given array.
   *
   * @param objects an arbitrary number of objects
   * @return the first of the given objects that is not null
   */
  public static <T> T nvl(@SuppressWarnings("unchecked") T... objects) {
    for (T obj : objects) {
      if (obj != null) {
        return obj;
      }
    }
    return null;
  }

  /**
   * Builds an object array of the given size, using the given function as a
   * producer for the objects put into the array at the respective positions.
   *
   * @param arraySize the desired array size
   * @param objectProducerFunction a function that produces the objects to
   *     put at the respective array position each
   * @return the array with objects at each position, according to the given
   *     producer function
   */
  public static Object[] fillObjectArray(int arraySize,
      Function<Integer, Object> objectProducerFunction) {
    Object[] result = new Object[arraySize];
    for (int i = 0; i < result.length; i++) {
      result[i] = objectProducerFunction.apply(i);
    }
    return result;
  }

  public static <K, V> K getKeyForValue(final V value, Map<K, V> map) {
    for (Entry<K, V> entry : map.entrySet()) {
      if (entry.getValue().equals(value)) {
        return entry.getKey();
      }
    }
    return null;
  }
}
