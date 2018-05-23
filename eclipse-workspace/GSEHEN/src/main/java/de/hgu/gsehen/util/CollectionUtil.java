package de.hgu.gsehen.util;

import java.util.ArrayList;
import java.util.List;

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

}
