package com.willmolloy.handbrake.core;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * HandBrakeLoggerTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class HandBrakeLoggerTest {

  @Mock private Logger mockLogger;

  @InjectMocks private HandBrakeLogger handBrakeEtaLogger;

  @Test
  void logsEverythingAsDebug_and_logsEtaEvery10PercentOfProgressAsInfo() {
    // Given
    List<String> fakeHandBrakeLogs =
        List.of(
            "HandBrake 1.4.2 (2021100300) - MinGW x86_64 - https://handbrake.fr",
            "32 CPUs detected",
            "1 job(s) to process",
            "json job:",
            "",
            "Starting Task: Encoding Pass",
            "Encoding: task 1 of 1, 0.63 % (61.80 fps, avg 62.30 fps, ETA 00h10m00s)",
            "Encoding: task 1 of 1, 0.75 % (61.80 fps, avg 62.30 fps, ETA 00h09m59s)",
            "Encoding: task 1 of 1, 5.15 % (61.80 fps, avg 62.30 fps, ETA 00h09m30s)",
            "Encoding: task 1 of 1, 10.44 % (61.80 fps, avg 62.30 fps, ETA 00h09m00s)",
            "Encoding: task 1 of 1, 15.15 % (61.80 fps, avg 62.30 fps, ETA 00h08m30s)",
            "Encoding: task 1 of 1, 20.63 % (61.80 fps, avg 62.30 fps, ETA 00h08m00s)",
            "Encoding: task 1 of 1, 25.24 % (61.80 fps, avg 62.30 fps, ETA 00h07m30s)",
            "Encoding: task 1 of 1, 30.63 % (61.80 fps, avg 62.30 fps, ETA 00h07m00s)",
            "Encoding: task 1 of 1, 35.23 % (61.80 fps, avg 62.30 fps, ETA 00h06m30s)",
            "Encoding: task 1 of 1, 40.36 % (61.80 fps, avg 62.30 fps, ETA 00h06m00s)",
            "Encoding: task 1 of 1, 45.81 % (61.80 fps, avg 62.30 fps, ETA 00h05m30s)",
            "Encoding: task 1 of 1, 50.72 % (61.80 fps, avg 62.30 fps, ETA 00h05m00s)",
            "Encoding: task 1 of 1, 55.31 % (61.80 fps, avg 62.30 fps, ETA 00h04m30s)",
            "Encoding: task 1 of 1, 60.45 % (61.80 fps, avg 62.30 fps, ETA 00h04m00s)",
            "Encoding: task 1 of 1, 65.12 % (61.80 fps, avg 62.30 fps, ETA 00h03m30s)",
            "Encoding: task 1 of 1, 70.06 % (61.80 fps, avg 62.30 fps, ETA 00h03m00s)",
            "Encoding: task 1 of 1, 75.80 % (61.80 fps, avg 62.30 fps, ETA 00h02m30s)",
            "Encoding: task 1 of 1, 80.77 % (61.80 fps, avg 62.30 fps, ETA 00h02m00s)",
            "Encoding: task 1 of 1, 85.56 % (61.80 fps, avg 62.30 fps, ETA 00h01m30s)",
            "Encoding: task 1 of 1, 90.32 % (61.80 fps, avg 62.30 fps, ETA 00h01m00s)",
            "Encoding: task 1 of 1, 95.21 % (61.80 fps, avg 62.30 fps, ETA 00h00m30s)",
            "Encoding: task 1 of 1, 100.00 % (61.80 fps, avg 62.30 fps, ETA 00h00m00s)",
            "Encode done!",
            "HandBrake has exited.");

    // When
    fakeHandBrakeLogs.forEach(handBrakeEtaLogger);

    // Then
    InOrder inOrder = inOrder(mockLogger);

    for (String log : fakeHandBrakeLogs) {
      inOrder.verify(mockLogger).debug(log);
    }

    inOrder = inOrder(mockLogger);
    inOrder.verify(mockLogger).info("0.63 % (61.80 fps, avg 62.30 fps, ETA 00h10m00s)");
    inOrder.verify(mockLogger).info("10.44 % (61.80 fps, avg 62.30 fps, ETA 00h09m00s)");
    inOrder.verify(mockLogger).info("20.63 % (61.80 fps, avg 62.30 fps, ETA 00h08m00s)");
    inOrder.verify(mockLogger).info("30.63 % (61.80 fps, avg 62.30 fps, ETA 00h07m00s)");
    inOrder.verify(mockLogger).info("40.36 % (61.80 fps, avg 62.30 fps, ETA 00h06m00s)");
    inOrder.verify(mockLogger).info("50.72 % (61.80 fps, avg 62.30 fps, ETA 00h05m00s)");
    inOrder.verify(mockLogger).info("60.45 % (61.80 fps, avg 62.30 fps, ETA 00h04m00s)");
    inOrder.verify(mockLogger).info("70.06 % (61.80 fps, avg 62.30 fps, ETA 00h03m00s)");
    inOrder.verify(mockLogger).info("80.77 % (61.80 fps, avg 62.30 fps, ETA 00h02m00s)");
    inOrder.verify(mockLogger).info("90.32 % (61.80 fps, avg 62.30 fps, ETA 00h01m00s)");
    inOrder.verify(mockLogger).info("100.00 % (61.80 fps, avg 62.30 fps, ETA 00h00m00s)");
    verify(mockLogger, times(11)).info(anyString());

    verifyNoMoreInteractions(mockLogger);
  }
}
