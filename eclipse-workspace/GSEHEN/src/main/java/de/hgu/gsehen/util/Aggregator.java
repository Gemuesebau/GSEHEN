package de.hgu.gsehen.util;

import java.util.List;
import java.util.function.Function;

public interface Aggregator<T> extends Function<List<T>, T> {
}
