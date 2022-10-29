package com.willmolloy.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

import com.willmolloy.handbrake.core.options.Option;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HandBrake interface.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public class HandBrake {

  private static final Logger log = LogManager.getLogger();

  private static final Lock LOCK = new ReentrantLock();

  private final Cli cli;

  HandBrake(Cli cli) {
    this.cli = checkNotNull(cli);
  }

  public HandBrake() {
    this(new Cli());
  }

  /**
   * Runs HandBrake encoding.
   *
   * @param input input file
   * @param output output file
   * @return {@code true} if encoding was successful
   */
  public boolean encode(Path input, Path output, List<? extends Option> options) {
    if (Files.exists(output)) {
      log.warn("Output ({}) already exists", output);
      return true;
    }

    List<String> command =
        Stream.concat(
                Stream.of("HandBrakeCLI", "-i", input.toString(), "-o", output.toString()),
                options.stream().flatMap(option -> Stream.of(option.key(), option.value())))
            .toList();

    LOCK.lock();
    try {
      return cli.execute(command, new HandBrakeLogger(log));
    } catch (Exception e) {
      log.error("Error encoding: %s".formatted(input), e);
      return false;
    } finally {
      LOCK.unlock();
    }
  }
}
