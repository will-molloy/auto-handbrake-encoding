package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * UnencodedVideoTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class UnencodedVideoTest {

  private FileSystem fileSystem;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path archiveDirectory;

  private UnencodedVideo.Factory factory;

  @BeforeEach
  void setUp() throws Exception {
    fileSystem = Jimfs.newFileSystem(Configuration.unix());

    inputDirectory = fileSystem.getPath("input");
    outputDirectory = fileSystem.getPath("output");
    archiveDirectory = fileSystem.getPath("archive");

    Files.createDirectories(inputDirectory);
    Files.createDirectories(outputDirectory);
    Files.createDirectories(archiveDirectory);

    factory = new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);
  }

  @AfterEach
  void tearDown() throws IOException {
    fileSystem.close();
  }

  @Test
  void factory_newUnencodedVideo_computesOutputAndArchivePaths() {
    // Given
    Path mp4File = inputDirectory.resolve("file.mp4");

    // When
    UnencodedVideo unencodedVideo = factory.newUnencodedVideo(mp4File);

    // Then
    assertThat(unencodedVideo.originalPath()).isSameInstanceAs(mp4File);

    assertThat(unencodedVideo.encodedPath()).isEqualTo(outputDirectory.resolve("file.cfr.mp4"));
    assertThat(unencodedVideo.tempEncodedPath())
        .isEqualTo(outputDirectory.resolve("file.cfr.mp4.part"));

    assertThat(unencodedVideo.archivedPath()).isEqualTo(archiveDirectory.resolve("file.mp4"));
    assertThat(unencodedVideo.tempArchivedPath())
        .isEqualTo(archiveDirectory.resolve("file.mp4.part"));
  }

  @Test
  void factory_newUnencodedVideo_retainsDirectoryStructureRelativeToInput() {
    // Given
    Path mp4File = inputDirectory.resolve("Nested/Nested2/file.mp4");

    // When
    UnencodedVideo unencodedVideo = factory.newUnencodedVideo(mp4File);

    // Then
    assertThat(unencodedVideo.originalPath()).isSameInstanceAs(mp4File);

    assertThat(unencodedVideo.encodedPath())
        .isEqualTo(outputDirectory.resolve("Nested/Nested2/file.cfr.mp4"));
    assertThat(unencodedVideo.tempEncodedPath())
        .isEqualTo(outputDirectory.resolve("Nested/Nested2/file.cfr.mp4.part"));

    assertThat(unencodedVideo.archivedPath())
        .isEqualTo(archiveDirectory.resolve("Nested/Nested2/file.mp4"));
    assertThat(unencodedVideo.tempArchivedPath())
        .isEqualTo(archiveDirectory.resolve("Nested/Nested2/file.mp4.part"));
  }

  @Test
  void factory_newUnencodedVideo_rejectsNonMp4File() {
    // Given
    Path mp3File = inputDirectory.resolve("file.mp3");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> factory.newUnencodedVideo(mp3File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("videoPath (%s) does not represent an .mp4 file".formatted(mp3File));
  }

  @Test
  void factory_newUnencodedVideo_rejectsEncodedMp4File() {
    // Given
    Path encodedMp4File = inputDirectory.resolve("file.cfr.mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class, () -> factory.newUnencodedVideo(encodedMp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("videoPath (%s) represents an encoded .mp4 file".formatted(encodedMp4File));
  }

  @Test
  void factory_newUnencodedVideo_rejectsTempEncodedMp4File() {
    // Given
    Path tempEncodedMp4File = inputDirectory.resolve("file.cfr.mp4.part");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class, () -> factory.newUnencodedVideo(tempEncodedMp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo(
            "videoPath (%s) represents an incomplete encoded .mp4 file"
                .formatted(tempEncodedMp4File));
  }

  @Test
  void factory_newUnencodedVideo_rejectsTempArchivedMp4File() {
    // Given
    Path tempArchivedMp4File = inputDirectory.resolve("file.mp4.part");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class, () -> factory.newUnencodedVideo(tempArchivedMp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo(
            "videoPath (%s) represents an incomplete archived .mp4 file"
                .formatted(tempArchivedMp4File));
  }

  @Test
  void factory_newUnencodedVideo_rejectsVideoPathIfNotChildOfInputDirectory() {
    // Given
    Path mp4File = outputDirectory.resolve("file.mp4");

    // When & Then
    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> factory.newUnencodedVideo(mp4File));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo(
            "videoPath (%s) is not a child of inputDirectory (%s)"
                .formatted(mp4File, inputDirectory));
  }

  @Test
  void factory_constructor_rejectsNonDirectoryInputDirectory() {
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
  void factory_constructor_rejectsNonDirectoryOutputDirectory() {
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
  void factory_constructor_rejectsNonDirectoryArchiveDirectory() {
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
  void toString_returnsOriginalPath() {
    // Given
    Path mp4File = inputDirectory.resolve("file.mp4");

    UnencodedVideo unencodedVideo = factory.newUnencodedVideo(mp4File);

    // When & Then
    assertThat(unencodedVideo.toString()).isEqualTo(mp4File.toString());
  }
}
