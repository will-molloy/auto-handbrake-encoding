package com.wilmol.handbrake.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Command Line Interface.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
public class Cli {

  private static final Logger log = LogManager.getLogger();

  /**
   * Executes the given command.
   *
   * @param command command to execute
   * @return {@code true} if execution was successful
   */
  boolean executeCommand(String... command) throws Exception {
    log.info("Executing: {}", List.of(command));

    Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

    Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));

    consumeStream(process.getInputStream(), log::debug);

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      log.error("Command executed with non-zero exit code: {}", exitCode);
      return false;
    }
    return true;
  }

  private void consumeStream(InputStream inputStream, Consumer<String> consumer) {
    new Thread(
            () -> {
              BufferedReader bufferedReader =
                  new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
              bufferedReader.lines().forEach(consumer);
            })
        .start();
  }
}
