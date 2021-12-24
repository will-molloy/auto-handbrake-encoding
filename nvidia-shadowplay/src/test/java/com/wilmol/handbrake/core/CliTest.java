package com.wilmol.handbrake.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CliTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class CliTest {

  @Mock private ProcessBuilder mockProcessBuilder;

  @Mock private Process mockProcess;

  @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  private Cli cli;

  @BeforeEach
  void setUp() throws IOException {
    when(mockProcessBuilder.command(anyList())).thenReturn(mockProcessBuilder);
    when(mockProcessBuilder.redirectErrorStream(anyBoolean())).thenReturn(mockProcessBuilder);
    when(mockProcessBuilder.start()).thenReturn(mockProcess);

    when(mockProcess.getInputStream()).thenReturn(new EmptyInputStream());

    cli = new Cli(() -> mockProcessBuilder);
  }

  @Test
  void successfulExecutionOfCommandReturnsTrue() throws Exception {
    when(mockProcess.waitFor()).thenReturn(0);

    assertThat(cli.execute(List.of("ls"))).isTrue();
  }

  @Test
  void nonZeroExitCodeReturnsFalse() throws Exception {
    when(mockProcess.waitFor()).thenReturn(1);

    assertThat(cli.execute(List.of("ls"))).isFalse();
  }

  @Test
  void exceptionThrownReturnsFalse() throws Exception {
    when(mockProcess.waitFor()).thenThrow(new RuntimeException("error"));

    assertThat(cli.execute(List.of("ls"))).isFalse();
  }

  private static final class EmptyInputStream extends InputStream {
    @Override
    public int read() {
      return -1;
    }
  }
}
