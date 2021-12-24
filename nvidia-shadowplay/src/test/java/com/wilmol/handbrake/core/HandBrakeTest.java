package com.wilmol.handbrake.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
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
    Path preset = Path.of("preset.json");

    when(mockCli.execute(anyList())).thenReturn(true);

    assertThat(handBrake.encode(input, output, preset)).isTrue();
    verify(mockCli)
        .execute(
            List.of(
                "HandBrakeCLI",
                "--preset-import-file",
                "\"preset.json\"",
                "-i",
                "\"input.mp4\"",
                "-o",
                "\"output.mp4\""));
  }

  @Test
  void outputAlreadyExistsReturnsTrue() throws IOException {
    Path input = Path.of("input.mp4");
    Path output = Path.of("output.mp4");
    Path preset = Path.of("preset.json");

    try {
      Files.createFile(output);
      assertThat(handBrake.encode(input, output, preset)).isTrue();
    } finally {
      Files.delete(output);
    }
  }

  @Test
  void unsuccessfulEncodingReturnsFalse() {
    Path input = Path.of("input.mp4");
    Path output = Path.of("output.mp4");
    Path preset = Path.of("preset.json");

    when(mockCli.execute(anyList())).thenReturn(false);

    assertThat(handBrake.encode(input, output, preset)).isFalse();
  }

  @Test
  void exceptionThrownReturnsFalse() {
    Path input = Path.of("input.mp4");
    Path output = Path.of("output.mp4");
    Path preset = Path.of("preset.json");

    when(mockCli.execute(anyList())).thenThrow(new RuntimeException("error"));

    assertThat(handBrake.encode(input, output, preset)).isFalse();
  }
}