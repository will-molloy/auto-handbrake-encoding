package com.wilmol.handbrake.core;

import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ComputerTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class ComputerTest {

  @Mock private Cli mockCli;

  @InjectMocks private Computer computer;

  @Test
  void shutdown_requestsShutdown() {
    // When
    computer.shutdown();

    // Then
    verify(mockCli).execute(List.of("shutdown", "-s", "-t", "30"));
  }
}
