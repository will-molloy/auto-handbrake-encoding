package com.willmolloy.handbrake.cfr.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Async utility methods.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class Async {

  /** Runs the provided supplier asynchronously. */
  public static <T> CompletableFuture<T> executeAsync(Supplier<T> supplier, String threadName) {
    // yes it's not the best idea to create a new ExecutorService each time, but this is the only
    // way to guarantee a new thread is created each time
    // (want to do this so the logs are easier to follow, and don't want to set up structured
    // logging)
    // could use 'new Thread' (with FutureTask), but then you don't get the CompletableFuture API
    // TODO newVirtualThreadPerTaskExecutor
    try (ExecutorService executor =
        Executors.newSingleThreadExecutor(r -> new Thread(r, threadName))) {
      return CompletableFuture.supplyAsync(supplier, executor);
    }
  }

  private Async() {}
}
