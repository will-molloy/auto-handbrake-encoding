package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.io.Resources;
import com.google.common.truth.StreamSubject;
import com.wilmol.handbrake.core.HandBrake;
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
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class VideoEncoderTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path testVideo;

  private UnencodedVideo.Factory unencodedVideoFactory;

  @Mock private HandBrake mockHandBrake;
  @InjectMocks private VideoEncoder videoEncoder;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    inputDirectory = testDirectory.resolve("input/Videos/Gameplay");
    outputDirectory = testDirectory.resolve("output/Videos/Encoded Gameplay");
    Path archiveDirectory = testDirectory.resolve("archive/Videos/Gameplay");
    testVideo = Path.of(Resources.getResource("test-video.mp4").toURI());

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
  void createsEncodedFile() throws IOException {
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

    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    videoEncoder.encode(unencodedVideo);

    // Then
    verify(mockHandBrake)
        .encode(unencodedMp4File, outputDirectory.resolve("file - CFR (incomplete).mp4"));
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("file.mp4"), outputDirectory.resolve("file - CFR.mp4"));
  }

  @Test
  void retainsDirectoryStructureRelativeToInputCreatingParentDirectoryIfNeeded()
      throws IOException {
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

    Files.createDirectories(inputDirectory.resolve("Halo/Campaign"));
    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("Halo/Campaign/file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    videoEncoder.encode(unencodedVideo);

    // Then
    verify(mockHandBrake)
        .encode(
            unencodedMp4File, outputDirectory.resolve("Halo/Campaign/file - CFR (incomplete).mp4"));
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("Halo/Campaign/file.mp4"),
            outputDirectory.resolve("Halo/Campaign/file - CFR.mp4"));
  }

  @Test
  void retainsOriginalIfEncodingFails() throws IOException {
    // Given
    when(mockHandBrake.encode(any(), any())).thenReturn(false);

    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    videoEncoder.encode(unencodedVideo);

    // Then
    assertThatTestDirectory().containsExactly(unencodedMp4File);
  }

  @Test
  void encodedFileAlreadyExistsReturnsEarly() throws IOException {
    // Given
    Files.copy(testVideo, outputDirectory.resolve("file - CFR.mp4"));

    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    videoEncoder.encode(unencodedVideo);

    // Then
    verify(mockHandBrake, never()).encode(any(), any());
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("file.mp4"), outputDirectory.resolve("file - CFR.mp4"));
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile));
  }
}
