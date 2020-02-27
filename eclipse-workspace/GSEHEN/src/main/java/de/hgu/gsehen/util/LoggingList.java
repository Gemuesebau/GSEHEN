package de.hgu.gsehen.util;

import static de.hgu.gsehen.util.MessageUtil.logFilteredStackTrace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingList<T> implements Iterable<T> {
  private static final Logger LOGGER = Logger.getLogger(LoggingList.class.getName());

  List<T> backingList;

  public LoggingList() {
    backingList = new ArrayList<T>();
  }

  public LoggingList(List<T> list) {
    logFilteredStackTrace(LOGGER, Level.FINE);
    backingList = list;
  }

  @Override
  public Iterator<T> iterator() {
    Iterator<T> iterator = backingList.iterator();
    return new Iterator<T>() {

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public T next() {
        return iterator.next();
      }

      @Override
      public void remove() {
        logFilteredStackTrace(LOGGER, Level.FINE);
        iterator.remove();
      }
    };
  }

  public void add(T element) {
    logFilteredStackTrace(LOGGER, Level.FINE);
    backingList.add(element);
  }

  public void remove(T element) {
    logFilteredStackTrace(LOGGER, Level.FINE);
    backingList.remove(element);
  }

  public void removeAll(List<T> list) {
    logFilteredStackTrace(LOGGER, Level.FINE);
    backingList.remove(list);
  }

  public int size() {
    return backingList.size();
  }
}
