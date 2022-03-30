package com.wilmol.handbrake.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * HandBrakeTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class HandBrakeTest {

  @Mock private Cli mockCli;

  @InjectMocks private HandBrake handBrake;

  @Test
  void successfulEncodingReturnsTrue() {
    Path input = Path.of("input.mp4");
    Path output = Path.of("output.mp4");

    when(mockCli.execute(anyList(), any())).thenReturn(true);

    assertThat(handBrake.encode(input, output)).isTrue();
    verify(mockCli)
        .execute(
            eq(
                List.of(
                    "HandBrakeCLI",
                    "--preset",
                    "Production Standard",
                    "-i",
                    "input.mp4",
                    "-o",
                    "output.mp4")),
            any());
  }

  @Test
  void outputAlreadyExistsReturnsEarly() throws IOException {
    Path input = Path.of("input.mp4");
    Path output = Path.of("output.mp4");

    try {
      Files.createFile(output);
      assertThat(handBrake.encode(input, output)).isTrue();
      verify(mockCli, never()).execute(anyList(), any());
    } finally {
      Files.delete(output);
    }
  }

  @Test
  void unsuccessfulEncodingReturnsFalse() {
    Path input = Path.of("input.mp4");
    Path output = Path.of("output.mp4");

    when(mockCli.execute(anyList(), any())).thenReturn(false);

    assertThat(handBrake.encode(input, output)).isFalse();
  }

  @Test
  void exceptionThrownReturnsFalse() {
    Path input = Path.of("input.mp4");
    Path output = Path.of("output.mp4");

    when(mockCli.execute(anyList(), any())).thenThrow(new RuntimeException("error"));

    assertThat(handBrake.encode(input, output)).isFalse();
  }
}
