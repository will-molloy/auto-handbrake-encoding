package com.willmolloy.handbrake.core;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * HandBrake logger. This class is NOT threadsafe. Use once per HandBrake process.
 *
 * <p>Logs all HandBrake output as DEBUG.
 *
 * <p>Logs HandBrake ETA every 10% of progress as INFO.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class HandBrakeLogger implements Consumer<String> {

  // HandBrake output logs look like:
  // Encoding: task 1 of 1, 1.63 % (60.56 fps, avg 83.01 fps, ETA 00h22m29s)
  // (always task 1 of 1 since HandBrakeImpl called 1 video at a time)
  private static final Pattern ENCODING_ETA_PATTERN =
      Pattern.compile(
          "Encoding: task 1 of 1, ((\\d+)[.]\\d+ % [(]\\d+[.]\\d+ fps, avg \\d+[.]\\d+ fps, ETA \\d+h\\d+m\\d+s[)])");

  private final HashSet<Integer> remainingProgressPercentsToLog =
      IntStream.iterate(0, i -> i <= 100, i -> i + 10)
          .boxed()
          .collect(Collectors.toCollection(HashSet::new));

  private final Logger log;

  @VisibleForTesting
  HandBrakeLogger(Logger log) {
    this.log = checkNotNull(log);
  }

  HandBrakeLogger() {
    this(LogManager.getLogger());
  }

  @Override
  public void accept(String logLine) {
    log.debug(logLine);

    Matcher m = ENCODING_ETA_PATTERN.matcher(logLine);
    if (m.matches()) {
      int percent = Integer.parseInt(m.group(2));
      if (remainingProgressPercentsToLog.remove(percent)) {
        log.info(m.group(1));
      }
    }
  }
}
