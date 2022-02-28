package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

/**
 * UnencodedVideoTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class UnencodedVideoTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path archiveDirectory;
  private Path testVideo;

  @Mock private HandBrake mockHandBrake;

  private UnencodedVideo.Factory unencodedVideoFactory;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(UnencodedVideoTest.class.getSimpleName());
    inputDirectory = testDirectory.resolve("input/Videos/Gameplay");
    outputDirectory = testDirectory.resolve("output/Videos/Encoded Gameplay");
    archiveDirectory = testDirectory.resolve("archive/Videos/Gameplay");
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
  void encode_createsEncodedFileAndArchivesOriginal() throws IOException {
    // Given
    when(mockHandBrake.encode(any(), any()))
        .then(
            (Answer<Boolean>)
                invocation -> {
                  // bit of an ugly hack...
                  // need to create the temp encoded file as its expected as output from HandBrake
                  Path handBrakeOutput = invocation.getArgument(1);
                  Files.createDirectories(checkNotNull(handBrakeOutput.getParent()));
                  Files.createFile(handBrakeOutput);
                  return true;
                });

    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    unencodedVideo.encode(mockHandBrake);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("file - CFR.mp4"),
            archiveDirectory.resolve("file - Archived.mp4"));
  }

  @Test
  void encode_retainsDirectoryStructureRelativeToInputFile() throws IOException {
    // Given
    when(mockHandBrake.encode(any(), any()))
        .then(
            (Answer<Boolean>)
                invocation -> {
                  // bit of an ugly hack...
                  // need to create the temp encoded file as its expected as output from HandBrake
                  Path handBrakeOutput = invocation.getArgument(1);
                  Files.createDirectories(checkNotNull(handBrakeOutput.getParent()));
                  Files.createFile(handBrakeOutput);
                  return true;
                });

    Files.createDirectories(inputDirectory.resolve("Halo/Campaign"));
    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("Halo/Campaign/file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    unencodedVideo.encode(mockHandBrake);

    // Then
    assertThatTestDirectory()
        .containsExactly(
            outputDirectory.resolve("Halo/Campaign/file - CFR.mp4"),
            archiveDirectory.resolve("Halo/Campaign/file - Archived.mp4"));
  }

  @Test
  void encode_retainsOriginalIfEncodingFails() throws IOException {
    // Given
    when(mockHandBrake.encode(any(), any())).thenReturn(false);

    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    unencodedVideo.encode(mockHandBrake);

    // Then
    assertThatTestDirectory().containsExactly(unencodedMp4File);
  }

  @Test
  void archive_movesInputFileToArchiveDirectory() throws IOException {
    // Given
    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    unencodedVideo.archive();

    // Then
    assertThatTestDirectory().containsExactly(archiveDirectory.resolve("file - Archived.mp4"));
  }

  @Test
  void archive_retainsDirectoryStructureRelativeToInput() throws IOException {
    // Given
    Files.createDirectories(inputDirectory.resolve("Halo/Campaign"));
    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("Halo/Campaign/file.mp4"));

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    unencodedVideo.archive();

    // Then
    assertThatTestDirectory()
        .containsExactly(archiveDirectory.resolve("Halo/Campaign/file - Archived.mp4"));
  }

  @Test
  void toString_returnsOriginalPath() {
    // Given
    Path mp4File = inputDirectory.resolve("file.mp4");

    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(mp4File);

    // When & Then
    assertThat(unencodedVideo.toString()).isEqualTo(mp4File.toString());
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile));
  }
}
