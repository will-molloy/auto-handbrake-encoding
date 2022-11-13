package com.willmolloy.handbrake.cfr.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.ForwardingExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Async helper methods.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class AsyncHelper {

  /** Runs the provided supplier asynchronously. */
  public static <T> CompletableFuture<T> executeAsync(Supplier<T> supplier, String threadName) {
    // yes it's not the best idea to create a new ExecutorService each time, but this is the only
    // way to guarantee a new thread is created each time
    // (want to do this so the logs are easier to follow, and don't want to set up structured
    // logging)
    // could use 'new Thread' (with FutureTask), but then you don't get the CompletableFuture API
    try (CloseableExecutorService executor =
        new CloseableExecutorService(
            Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, threadName)))) {
      return CompletableFuture.supplyAsync(supplier, executor);
    }
  }

  // TODO remove when we upgrade to JDK 19
  private static class CloseableExecutorService extends ForwardingExecutorService
      implements AutoCloseable {
    private final ExecutorService delegate;

    CloseableExecutorService(ExecutorService delegate) {
      this.delegate = checkNotNull(delegate);
    }

    @Override
    protected ExecutorService delegate() {
      return delegate;
    }

    @Override
    public void close() {
      // copy paste from JDK 19 EA
      boolean terminated = isTerminated();
      if (!terminated) {
        shutdown();
        boolean interrupted = false;
        while (!terminated) {
          try {
            terminated = awaitTermination(1L, TimeUnit.DAYS);
          } catch (InterruptedException e) {
            if (!interrupted) {
              shutdownNow();
              interrupted = true;
            }
          }
        }
        if (interrupted) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private AsyncHelper() {}
}
