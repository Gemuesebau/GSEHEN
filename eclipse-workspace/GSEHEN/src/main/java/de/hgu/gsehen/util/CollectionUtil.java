package de.hgu.gsehen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
   * Creates a map that contains the given classes as values, mapped by their respective "simpleName".
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
}
