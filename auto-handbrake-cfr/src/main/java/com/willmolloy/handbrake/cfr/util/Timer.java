package com.willmolloy.handbrake.cfr.util;

import com.google.common.base.Stopwatch;
import java.util.function.Supplier;
import org.apache.logging.log4j.Logger;

/**
 * Timer utility methods.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class Timer {

  /** Decorates {@link Supplier} with a {@link Stopwatch}. Logs the elapsed time. */
  public static <T> Supplier<T> time(Supplier<? extends T> supplier, Logger log) {
    return () -> {
      Stopwatch stopwatch = Stopwatch.createStarted();
      try {
        return supplier.get();
      } finally {
        log.info("Elapsed: {}", stopwatch.elapsed());
      }
    };
  }

  private Timer() {}
}
