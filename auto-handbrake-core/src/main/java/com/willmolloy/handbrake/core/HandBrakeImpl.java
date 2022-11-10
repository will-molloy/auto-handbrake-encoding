package com.willmolloy.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Option;
import com.willmolloy.handbrake.core.options.Output;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HandBrake implementation.
 *
 * @see HandBrake#newInstance
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class HandBrakeImpl implements HandBrake {

  private static final Logger log = LogManager.getLogger();

  private static final Lock LOCK = new ReentrantLock();

  private final Cli cli;

  HandBrakeImpl(Cli cli) {
    this.cli = checkNotNull(cli);
  }

  public boolean encode(Input input, Output output, Option... options) {
    if (Files.exists(output.value())) {
      log.warn("Output ({}) already exists", output.value());
    }

    List<String> command =
        Stream.concat(
                Stream.of(
                    "HandBrakeCLI",
                    input.key(),
                    input.value().toString(),
                    output.key(),
                    output.value().toString()),
                Arrays.stream(options)
                    .flatMap(
                        option -> {
                          // TODO exhaustive switch for sealed type
                          // TODO record deconstructor - can't since we have interfaces??
                          if (option instanceof Option.KeyOnlyOption o) {
                            return Stream.of(o.key());
                          }
                          if (option instanceof Option.KeyValueOption<?> o) {
                            return Stream.of(o.key(), o.value().toString());
                          }
                          return Stream.of();
                        }))
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
