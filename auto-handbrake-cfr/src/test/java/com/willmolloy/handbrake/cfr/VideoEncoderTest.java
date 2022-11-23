package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.common.truth.StreamSubject;
import com.willmolloy.handbrake.core.HandBrake;
import com.willmolloy.handbrake.core.options.Encoder;
import com.willmolloy.handbrake.core.options.FrameRateControl;
import com.willmolloy.handbrake.core.options.Input;
import com.willmolloy.handbrake.core.options.Output;
import com.willmolloy.handbrake.core.options.Preset;
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
 * VideoEncoderTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class VideoEncoderTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path testVideo;
  private Path testVideo2;

  private UnencodedVideo.Factory unencodedVideoFactory;

  @Mock private HandBrake mockHandBrake;
  @InjectMocks private VideoEncoder videoEncoder;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    inputDirectory = testDirectory.resolve("input/Videos/Gameplay");
    outputDirectory = testDirectory.resolve("output/Videos/Encoded Gameplay");
    Path archiveDirectory = testDirectory.resolve("archive/Videos/Gameplay");
    testVideo = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_1MB.mp4").toURI());
    testVideo2 = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_2MB.mp4").toURI());

    Files.createDirectories(inputDirectory);
    Files.createDirectories(outputDirectory);
    Files.createDirectories(archiveDirectory);

    unencodedVideoFactory =
        new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }

  @Test
  void invokesHandBrakeCreatingEncodedFile() throws IOException {
    // Given
    whenHandBrakeReturns(true);

    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    videoEncoder.acquire();
    boolean result = videoEncoder.encode(unencodedVideo);

    // Then
    assertThat(result).isTrue();
    verifyHandBrakeCalled(unencodedVideo);
    assertThatTestDirectory()
        .containsExactly(unencodedVideo.originalPath(), unencodedVideo.encodedPath());
  }

  @Test
  void retainsDirectoryStructureRelativeToInputCreatingParentDirectoryIfNeeded()
      throws IOException {
    // Given
    whenHandBrakeReturns(true);

    Files.createDirectories(inputDirectory.resolve("Halo/Campaign"));
    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("Halo/Campaign/file.mp4")));

    // When
    videoEncoder.acquire();
    boolean result = videoEncoder.encode(unencodedVideo);

    // Then
    assertThat(result).isTrue();
    verifyHandBrakeCalled(unencodedVideo);
    assertThatTestDirectory()
        .containsExactly(unencodedVideo.originalPath(), unencodedVideo.encodedPath());
  }

  @Test
  void whenHandBrakeUnsuccessful_retainsUnencodedFile_andReturnsFalse() throws IOException {
    // Given
    whenHandBrakeReturns(false);

    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    videoEncoder.acquire();
    boolean result = videoEncoder.encode(unencodedVideo);

    // Then
    assertThat(result).isFalse();
    verifyHandBrakeCalled(unencodedVideo);
    assertThatTestDirectory()
        .containsExactly(unencodedVideo.originalPath(), unencodedVideo.tempEncodedPath());
  }

  @Test
  void whenHandBrakeThrowsException_retainsUnencodedFile_andReturnsFalse() throws IOException {
    // Given
    when(mockHandBrake.encode(any(), any(), any())).thenThrow(new RuntimeException());

    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    videoEncoder.acquire();
    boolean result = videoEncoder.encode(unencodedVideo);

    // Then
    assertThat(result).isFalse();
    verifyHandBrakeCalled(unencodedVideo);
    assertThatTestDirectory().containsExactly(unencodedVideo.originalPath());
  }

  @Test
  void whenEncodedFileAlreadyExists_overwrites_andReturnsTrue() throws IOException {
    // Given
    Files.copy(testVideo, outputDirectory.resolve("file.cfr.mp4"));

    whenHandBrakeReturns(true);

    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    videoEncoder.acquire();
    boolean result = videoEncoder.encode(unencodedVideo);

    // Then
    assertThat(result).isTrue();
    verifyHandBrakeCalled(unencodedVideo);
    assertThatTestDirectory()
        .containsExactly(unencodedVideo.originalPath(), unencodedVideo.encodedPath());
  }

  @Test
  void whenEncodedFileAlreadyExistsButContentsDiffer_retainsTempFile_andReturnsFalse()
      throws IOException {
    // Given
    Files.copy(testVideo2, outputDirectory.resolve("file.cfr.mp4"));

    whenHandBrakeReturns(true);

    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    videoEncoder.acquire();
    boolean result = videoEncoder.encode(unencodedVideo);

    // Then
    assertThat(result).isFalse();
    verifyHandBrakeCalled(unencodedVideo);
    assertThatTestDirectory()
        .containsExactly(
            unencodedVideo.originalPath(),
            unencodedVideo.tempEncodedPath(),
            unencodedVideo.encodedPath());
  }

  @Test
  void whenNotAcquired_throwsException() {
    // When
    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> videoEncoder.encode(null));

    // Then
    assertThat(thrown).hasMessageThat().isEqualTo("Not acquired");
  }

  private void whenHandBrakeReturns(boolean result) {
    when(mockHandBrake.encode(any(), any(), any()))
        .then(
            (Answer<Boolean>)
                invocation -> {
                  // bit of an ugly hack...
                  // need to create the temp encoded file as its expected as output from HandBrake
                  Path originalPath = ((Input) invocation.getArgument(0)).path();
                  Path tempEncodedPath = ((Output) invocation.getArgument(1)).path();
                  Files.copy(originalPath, tempEncodedPath);
                  return result;
                });
  }

  private void verifyHandBrakeCalled(UnencodedVideo unencodedVideo) {
    verify(mockHandBrake)
        .encode(
            Input.of(unencodedVideo.originalPath()),
            Output.of(unencodedVideo.tempEncodedPath()),
            Preset.productionStandard(),
            Encoder.h264(),
            FrameRateControl.constant());
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile));
  }
}
