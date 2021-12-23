package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Verify.verify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Command Line Interface.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class Cli {

  private static final Logger log = LogManager.getLogger();

  void executeCommand(String command) throws IOException, InterruptedException {
    log.info("Executing: {}", command);

    Runtime runtime = Runtime.getRuntime();

    Process process = runtime.exec(command);
    runtime.addShutdownHook(new Thread(process::destroy));

    consumeStream(process.getInputStream(), log::debug);
    consumeStream(process.getErrorStream(), log::debug);

    int exitCode = process.waitFor();
    verify(exitCode == 0, "Command (%s) executed with non-zero exit code: %s", command, exitCode);
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
