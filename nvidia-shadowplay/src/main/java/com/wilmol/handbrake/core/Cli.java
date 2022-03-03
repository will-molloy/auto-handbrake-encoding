package com.wilmol.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

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
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
public class Cli {

  private static final Logger log = LogManager.getLogger();

  private final Supplier<ProcessBuilder> processBuilderSupplier;

  public Cli(Supplier<ProcessBuilder> processBuilderSupplier) {
    this.processBuilderSupplier = checkNotNull(processBuilderSupplier);
  }

  public Cli() {
    this(ProcessBuilder::new);
  }

  /**
   * Executes the given command.
   *
   * @param command command to execute
   * @return {@code true} if execution was successful
   */
  @SuppressFBWarnings("REC_CATCH_EXCEPTION")
  public boolean execute(List<String> command) {
    log.info("Executing: {}", command);

    Process process = null;
    CompletableFuture<?> processLoggerFuture = null;
    try {
      process = processBuilderSupplier.get().command(command).redirectErrorStream(true).start();
      processLoggerFuture = consumeStreamAsync(process.getInputStream(), log::debug);

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        log.error("Command executed with non-zero exit code: {}", exitCode);
        return false;
      }
      return true;
    } catch (Exception e) {
      log.error("Error executing: %s".formatted(command), e);
      return false;
    } finally {
      log.debug("Destroying: {}", command);
      if (processLoggerFuture != null) {
        processLoggerFuture.join();
      }
      if (process != null) {
        process.destroy();
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
