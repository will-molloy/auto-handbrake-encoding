package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * UnencodedVideoFactoryTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class UnencodedVideoFactoryTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path archiveDirectory;

  private UnencodedVideo.Factory unencodedVideoFactory;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(UnencodedVideoFactoryTest.class.getSimpleName());
    inputDirectory = testDirectory.resolve("input");
    outputDirectory = testDirectory.resolve("output");
    archiveDirectory = testDirectory.resolve("archive");

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
  void constructor_rejectsNonDirectoryInputDirectory() {
    // Given
    Path mp4File = inputDirectory.resolve("file.mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo.Factory(mp4File, outputDirectory, archiveDirectory));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("inputDirectory (%s) is not a directory".formatted(mp4File));
  }

  @Test
  void constructor_rejectsNonDirectoryOutputDirectory() {
    // Given
    Path mp4File = inputDirectory.resolve("file.mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo.Factory(inputDirectory, mp4File, archiveDirectory));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("outputDirectory (%s) is not a directory".formatted(mp4File));
  }

  @Test
  void constructor_rejectsNonDirectoryArchiveDirectory() {
    // Given
    Path mp4File = inputDirectory.resolve("file.mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo.Factory(inputDirectory, outputDirectory, mp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("archiveDirectory (%s) is not a directory".formatted(mp4File));
  }

  @Test
  void newUnencodedVideo_rejectsNonMp4File() {
    // Given
    Path mp3File = inputDirectory.resolve("file.mp3");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class, () -> unencodedVideoFactory.newUnencodedVideo(mp3File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("videoPath (%s) does not represent an .mp4 file".formatted(mp3File));
  }

  @Test
  void newUnencodedVideo_rejectsEncodedMp4File() {
    // Given
    Path encodedMp4File = inputDirectory.resolve("file - CFR.mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> unencodedVideoFactory.newUnencodedVideo(encodedMp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("videoPath (%s) represents an encoded .mp4 file".formatted(encodedMp4File));
  }

  @Test
  void newUnencodedVideo_rejectsTempEncodedMp4File() {
    // Given
    Path tempEncodedMp4File = inputDirectory.resolve("file - CFR (incomplete).mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> unencodedVideoFactory.newUnencodedVideo(tempEncodedMp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo(
            "videoPath (%s) represents an incomplete encoded .mp4 file"
                .formatted(tempEncodedMp4File));
  }

  @Test
  void newUnencodedVideo_rejectsArchivedMp4File() {
    // Given
    Path archivedMp4File = inputDirectory.resolve("file - Archived.mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> unencodedVideoFactory.newUnencodedVideo(archivedMp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("videoPath (%s) represents an archived .mp4 file".formatted(archivedMp4File));
  }

  @Test
  void newUnencodedVideo_rejectsVideoPathIfNotChildOfInputDirectory() {
    // Given
    Path mp4File = outputDirectory.resolve("file.mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class, () -> unencodedVideoFactory.newUnencodedVideo(mp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo(
            "videoPath (%s) is not a child of inputDirectory (%s)"
                .formatted(mp4File, inputDirectory));
  }
}
