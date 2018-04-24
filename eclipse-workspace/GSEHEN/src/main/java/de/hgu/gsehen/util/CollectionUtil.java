package de.hgu.gsehen.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtil {

  public static <K, V> void addToMappedList(java.util.Map<K, List<V>> map, K key, V listValue) {
    List<V> list = map.get(key);
    if (list == null) {
      list = new ArrayList<V>();
      map.put(key, list);
    }
    list.add(listValue);
  }

}
