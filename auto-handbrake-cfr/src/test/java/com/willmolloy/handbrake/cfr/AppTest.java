package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static java.util.stream.IntStream.rangeClosed;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AppTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
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
    when(mockVideoArchiver.archive(any())).thenReturn(true);

    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video3.mp4"));

    // When
    boolean result = app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    verify(mockVideoEncoder)
        .encode(
            argThat(
                video ->
                    video
                        .originalPath()
                        .equals(inputDirectory.resolve("NestedFolder/video1.mp4"))));
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video2.mp4"))));
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video3.mp4"))));
    verify(mockVideoArchiver)
        .archive(
            argThat(
                video ->
                    video
                        .originalPath()
                        .equals(inputDirectory.resolve("NestedFolder/video1.mp4"))));
    verify(mockVideoArchiver)
        .archive(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video2.mp4"))));
    verify(mockVideoArchiver)
        .archive(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video3.mp4"))));
  }

  @ParameterizedTest
  @MethodSource("anyEncodeOrArchiveFailed")
  void
      whenEncodingFails_skipsArchiving_andStillEncodesAndArchivesOtherVideos_andReturnsFalseOverall(
          boolean firstEncodingSuccessful,
          boolean secondEncodingSuccessful,
          boolean thirdEncodingSuccessful)
          throws Exception {
    // Given
    when(mockVideoEncoder.encode(any()))
        .thenReturn(firstEncodingSuccessful, secondEncodingSuccessful, thirdEncodingSuccessful);
    when(mockVideoArchiver.archive(any())).thenReturn(true);

    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video3.mp4"));

    // When
    boolean result = app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    verify(mockVideoEncoder)
        .encode(
            argThat(
                video ->
                    video
                        .originalPath()
                        .equals(inputDirectory.resolve("NestedFolder/video1.mp4"))));
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video2.mp4"))));
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video3.mp4"))));
    verify(mockVideoArchiver, times(firstEncodingSuccessful ? 1 : 0))
        .archive(
            argThat(
                video ->
                    video
                        .originalPath()
                        .equals(inputDirectory.resolve("NestedFolder/video1.mp4"))));
    verify(mockVideoArchiver, times(secondEncodingSuccessful ? 1 : 0))
        .archive(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video2.mp4"))));
    verify(mockVideoArchiver, times(thirdEncodingSuccessful ? 1 : 0))
        .archive(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video3.mp4"))));
  }

  static Stream<Arguments> anyEncodeOrArchiveFailed() {
    return Stream.of(
        Arguments.of(true, true, false),
        Arguments.of(true, false, true),
        Arguments.of(false, true, true));
  }

  @ParameterizedTest
  @MethodSource("anyEncodeOrArchiveFailed")
  void whenArchivingFails_stillEncodesAndArchivesOtherVideos_andReturnsFalseOverall(
      boolean firstArchingSuccessful,
      boolean secondArchivingSuccessful,
      boolean thirdArchivingSuccessful)
      throws Exception {
    // Given
    when(mockVideoEncoder.encode(any())).thenReturn(true);
    when(mockVideoArchiver.archive(any()))
        .thenReturn(firstArchingSuccessful, secondArchivingSuccessful, thirdArchivingSuccessful);

    Files.createDirectories(inputDirectory.resolve("NestedFolder"));
    Files.copy(testVideo, inputDirectory.resolve("NestedFolder/video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video3.mp4"));

    // When
    boolean result = app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    verify(mockVideoEncoder)
        .encode(
            argThat(
                video ->
                    video
                        .originalPath()
                        .equals(inputDirectory.resolve("NestedFolder/video1.mp4"))));
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video2.mp4"))));
    verify(mockVideoEncoder)
        .encode(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video3.mp4"))));
    verify(mockVideoArchiver)
        .archive(
            argThat(
                video ->
                    video
                        .originalPath()
                        .equals(inputDirectory.resolve("NestedFolder/video1.mp4"))));
    verify(mockVideoArchiver)
        .archive(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video2.mp4"))));
    verify(mockVideoArchiver)
        .archive(
            argThat(video -> video.originalPath().equals(inputDirectory.resolve("video3.mp4"))));
  }

  @Test
  void encodesInOrder() throws Exception {
    // Given
    when(mockVideoEncoder.encode(any())).thenReturn(true);
    when(mockVideoArchiver.archive(any())).thenReturn(true);

    for (int i : rangeClosed(1, 100).toArray()) {
      Files.copy(testVideo, inputDirectory.resolve("video%03d.mp4".formatted(i)));
    }

    // When
    boolean result = app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();

    InOrder inOrder = inOrder(mockVideoEncoder);
    for (int i : rangeClosed(1, 100).toArray()) {
      inOrder
          .verify(mockVideoEncoder)
          .encode(
              argThat(
                  video ->
                      video
                          .originalPath()
                          .equals(inputDirectory.resolve("video%03d.mp4".formatted(i)))));
    }
  }
}
