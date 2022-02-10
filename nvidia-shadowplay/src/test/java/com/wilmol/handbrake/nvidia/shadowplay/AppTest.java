package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.wilmol.handbrake.core.Cli;
import com.wilmol.handbrake.core.HandBrake;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

/**
 * AppTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@ExtendWith(MockitoExtension.class)
class AppTest {

  private Path testDirectory;
  private Path testVideo;

  @Mock private HandBrake mockHandBrake;

  @Mock private Cli mockCli;

  @InjectMocks private App app;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of("AppTest");
    testVideo = Path.of(Resources.getResource("test-video.mp4").toURI());
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }

  @Test
  void encodesVideoFilesAndArchivesOriginals() throws Exception {
    // Given
    when(mockHandBrake.encode(any(), any()))
        .then(
            (Answer<Boolean>)
                invocation -> {
                  // bit of an ugly hack...
                  // need to create the temp encoded file as its expected as output from HandBrake
                  Path handBrakeOutput = invocation.getArgument(1);
                  Files.createFile(handBrakeOutput);
                  return true;
                });

    Files.createDirectories(testDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, testDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, testDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, testDirectory.resolve("NestedFolder/video3.mp4"));

    // When
    app.run(testDirectory, false);

    // Then
    assertThat(Files.walk(testDirectory).filter(Files::isRegularFile))
        .containsExactly(
            testDirectory.resolve("video1 - Archived.mp4"),
            testDirectory.resolve("video2 - Archived.mp4"),
            testDirectory.resolve("NestedFolder/video3 - Archived.mp4"),
            testDirectory.resolve("video1 - CFR.mp4"),
            testDirectory.resolve("video2 - CFR.mp4"),
            testDirectory.resolve("NestedFolder/video3 - CFR.mp4"));
  }

  @Test
  void archivesAlreadyEncodedVideos() throws Exception {
    // Given
    Files.createDirectories(testDirectory);
    Files.copy(testVideo, testDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, testDirectory.resolve("video1 - CFR.mp4"));

    // When
    app.run(testDirectory, false);

    // Then
    assertThat(Files.walk(testDirectory).filter(Files::isRegularFile))
        .containsExactly(
            testDirectory.resolve("video1 - Archived.mp4"),
            testDirectory.resolve("video1 - CFR.mp4"));
  }

  @Test
  void retainsOriginalIfEncodingFails() throws Exception {
    // Given
    when(mockHandBrake.encode(any(), any())).thenReturn(false);

    Files.createDirectories(testDirectory);
    Files.copy(testVideo, testDirectory.resolve("video1.mp4"));

    // When
    app.run(testDirectory, false);

    // Then
    assertThat(Files.walk(testDirectory).filter(Files::isRegularFile))
        .containsExactly(testDirectory.resolve("video1.mp4"));
  }

  @Test
  void deletesIncompleteEncodings() throws Exception {
    // Given
    Files.createDirectories(testDirectory);
    Files.copy(testVideo, testDirectory.resolve("video1 - CFR (incomplete).mp4"));

    // When
    app.run(testDirectory, false);

    // Then
    assertThat(Files.walk(testDirectory).filter(Files::isRegularFile)).isEmpty();
  }
}
