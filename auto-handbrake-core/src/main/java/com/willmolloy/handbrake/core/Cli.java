package com.willmolloy.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Command Line Interface.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class Cli {

  private static final Logger log = LogManager.getLogger();

  private final Supplier<ProcessBuilder> processBuilderSupplier;

  @VisibleForTesting
  Cli(Supplier<ProcessBuilder> processBuilderSupplier) {
    this.processBuilderSupplier = checkNotNull(processBuilderSupplier);
  }

  Cli() {
    this(ProcessBuilder::new);
  }

  /**
   * Executes the given command.
   *
   * @param command command to execute
   * @param processLogConsumer consumer of the command's process logs (i.e. {@link
   *     Process#getInputStream})
   * @return {@code true} if execution was successful
   */
  @SuppressFBWarnings("REC_CATCH_EXCEPTION")
  boolean execute(List<String> command, Consumer<String> processLogConsumer) {
    log.info("Executing: {}", command);

    Process process = null;
    CompletableFuture<?> processLoggerFuture = null;
    try {
      process = processBuilderSupplier.get().command(command).redirectErrorStream(true).start();
      processLoggerFuture = consumeStreamAsync(process.getInputStream(), processLogConsumer);

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        log.error("Command ({}) executed with non-zero exit code: {}", command, exitCode);
        return false;
      }
      return true;
    } catch (Exception e) {
      log.error("Error executing: %s".formatted(command), e);
      return false;
    } finally {
      if (process != null) {
        process.destroy();
        if (process.isAlive()) {
          log.warn("Destroying forcibly: {}", command);
          process.destroyForcibly();
        }
      }
      if (processLoggerFuture != null) {
        processLoggerFuture.join();
      }
    }
  }

  private CompletableFuture<Void> consumeStreamAsync(
      InputStream inputStream, Consumer<String> consumer) {
    return CompletableFuture.runAsync(
        () -> {
          BufferedReader bufferedReader =
              new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
          bufferedReader.lines().forEach(consumer);
        });
  }
}
