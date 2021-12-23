package com.wilmol.handbrake.util;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Wraps an object with a corresponding index.
 *
 * @param object object to wrap
 * @param index corresponding index
 * @param <T> type of object
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
public record IndexedObject<T>(T object, int index) {

  /**
   * Constructs a list of one-indexed objects.
   *
   * @param list the original list
   * @param <T> type of object
   * @return list of one-indexed objects
   */
  public static <T> List<IndexedObject<T>> oneIndexed(List<T> list) {
    return IntStream.range(0, list.size())
        .mapToObj(i -> new IndexedObject<>(list.get(i), i + 1))
        .toList();
  }
}
