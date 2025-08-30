package com.willmolloy.handbrake.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CliTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class CliTest {

  @Mock private ProcessBuilder mockProcessBuilder;

  @Mock private Process mockProcess;

  private Cli cli;

  @BeforeEach
  void setUp() throws IOException {
    when(mockProcessBuilder.command(anyList())).thenReturn(mockProcessBuilder);
    when(mockProcessBuilder.redirectErrorStream(anyBoolean())).thenReturn(mockProcessBuilder);
    when(mockProcessBuilder.start()).thenReturn(mockProcess);

    when(mockProcess.getInputStream()).thenReturn(new EmptyInputStream());

    cli = new Cli(() -> mockProcessBuilder);
  }

  @AfterEach
  void tearDown() throws IOException {
    verify(mockProcessBuilder).redirectErrorStream(true);
    verify(mockProcessBuilder).start();
    verify(mockProcess).getInputStream();
    verify(mockProcess).destroy();
  }

  @Test
  void successfulExecutionOfCommandReturnsTrue() throws InterruptedException {
    when(mockProcess.waitFor()).thenReturn(0);

    assertThat(cli.execute(List.of("ls"), new EmptyConsumer())).isTrue();
    verify(mockProcessBuilder).command(List.of("ls"));
  }

  @Test
  void nonZeroExitCodeReturnsFalse() throws InterruptedException {
    when(mockProcess.waitFor()).thenReturn(1);

    assertThat(cli.execute(List.of("abc"), new EmptyConsumer())).isFalse();
    verify(mockProcessBuilder).command(List.of("abc"));
  }

  @Test
  void exceptionThrownReturnsFalse() throws InterruptedException {
    when(mockProcess.waitFor()).thenThrow(new RuntimeException("error"));

    assertThat(cli.execute(List.of("xyz"), new EmptyConsumer())).isFalse();
    verify(mockProcessBuilder).command(List.of("xyz"));
  }

  private static final class EmptyInputStream extends InputStream {
    @Override
    public int read() {
      return -1;
    }
  }

  private static final class EmptyConsumer implements Consumer<String> {
    @Override
    public void accept(String s) {}
  }
}
