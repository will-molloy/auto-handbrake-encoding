package com.willmolloy.handbrake.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.willmolloy.handbrake.core.options.Encoder;
import com.willmolloy.handbrake.core.options.FrameRateControl;
import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Output;
import com.willmolloy.handbrake.core.options.Preset;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * HandBrakeTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class HandBrakeImplTest {

  @Mock private Cli mockCli;

  @InjectMocks private HandBrakeImpl handBrake;

  @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
  private FileSystem fileSystem;

  private Path input;
  private Path output;

  @BeforeEach
  void setUp() {
    fileSystem = Jimfs.newFileSystem(Configuration.unix());

    input = fileSystem.getPath("input.mp4");
    output = fileSystem.getPath("output.mp4");
  }

  @AfterEach
  void tearDown() throws IOException {
    fileSystem.close();
  }

  @Test
  void successfulEncodingReturnsTrue() {
    when(mockCli.execute(anyList(), any())).thenReturn(true);

    assertThat(
            handBrake.encode(
                Input.of(input),
                Output.of(output),
                Preset.productionStandard(),
                Encoder.h264(),
                FrameRateControl.constant()))
        .isTrue();
    verify(mockCli)
        .execute(
            eq(
                List.of(
                    "HandBrakeCLI",
                    "--input",
                    "input.mp4",
                    "--output",
                    "output.mp4",
                    "--preset",
                    "Production Standard",
                    "--encoder",
                    "x264",
                    "--cfr")),
            isA(HandBrakeLogger.class));
  }

  @Test
  void outputAlreadyExistsOverwrites() throws IOException {
    when(mockCli.execute(anyList(), any())).thenReturn(true);
    Files.createFile(output);

    assertThat(handBrake.encode(Input.of(input), Output.of(output))).isTrue();
    verify(mockCli)
        .execute(
            eq(List.of("HandBrakeCLI", "--input", "input.mp4", "--output", "output.mp4")),
            isA(HandBrakeLogger.class));
  }

  @Test
  void unsuccessfulEncodingReturnsFalse() {
    when(mockCli.execute(anyList(), any())).thenReturn(false);

    assertThat(handBrake.encode(Input.of(input), Output.of(output))).isFalse();
  }

  @Test
  void exceptionThrownReturnsFalse() {
    when(mockCli.execute(anyList(), any())).thenThrow(new RuntimeException("error"));

    assertThat(handBrake.encode(Input.of(input), Output.of(output))).isFalse();
  }
}
