package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.common.truth.StreamSubject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AppTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@ExtendWith(MockitoExtension.class)
class AppTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path archiveDirectory;
  private Path testVideo;

  @Mock private VideoEncoder mockVideoEncoder;
  @Mock private VideoArchiver mockVideoArchiver;
  @InjectMocks private App app;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    inputDirectory = testDirectory.resolve("input");
    outputDirectory = testDirectory.resolve("output");
    archiveDirectory = testDirectory.resolve("archive");

    testVideo = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_1MB.mp4").toURI());

    Files.createDirectories(inputDirectory);
    Files.createDirectories(outputDirectory);
    Files.createDirectories(archiveDirectory);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }

  @Test
  void encodesVideoFilesAndArchivesOriginals() throws Exception {
    // Given
    when(mockVideoEncoder.encode(any())).thenReturn(true);
    when(mockVideoArchiver.archiveAsync(any())).thenReturn(CompletableFuture.completedFuture(null));

    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video3.mp4"));

    // When
    app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video1.mp4"))));
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video2.mp4"))));
    verify(mockVideoEncoder)
        .encode(
            argThat(
                video ->
                    video
                        .originalPath()
                        .equals(inputDirectory.resolve("NestedFolder/video3.mp4"))));
    verify(mockVideoArchiver)
        .archiveAsync(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video1.mp4"))));
    verify(mockVideoArchiver)
        .archiveAsync(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video2.mp4"))));
    verify(mockVideoArchiver)
        .archiveAsync(
            argThat(
                video ->
                    video
                        .originalPath()
                        .equals(inputDirectory.resolve("NestedFolder/video3.mp4"))));
  }

  @Test
  void skipsArchivingOriginalIfEncodingFails() throws Exception {
    // Given
    when(mockVideoEncoder.encode(any())).thenReturn(false);

    Files.copy(testVideo, inputDirectory.resolve("video1.mp4"));

    // When
    app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video1.mp4"))));
    verify(mockVideoArchiver, never())
        .archiveAsync(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video1.mp4"))));
  }

  @Test
  void deletesIncompleteEncodings() throws Exception {
    // Given
    Files.copy(testVideo, outputDirectory.resolve("video1 - CFR (incomplete).mp4"));

    // When
    app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory().isEmpty();
  }

  @Test
  void deletesIncompleteArchives() throws Exception {
    // Given
    Files.copy(testVideo, archiveDirectory.resolve("video1 - Archived (incomplete).mp4"));

    // When
    app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThatTestDirectory().isEmpty();
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile));
  }
}
