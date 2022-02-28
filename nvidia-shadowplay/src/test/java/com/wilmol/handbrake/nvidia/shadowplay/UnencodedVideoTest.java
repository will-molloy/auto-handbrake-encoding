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
 * UnencodedVideoTest.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class UnencodedVideoTest {

  private final Path input = Path.of("input/Videos/Gameplay");
  private final Path output = Path.of("output/Videos/Encoded Gameplay");
  private final Path archive = Path.of("archive/Videos/Gameplay");

  @BeforeEach
  void setUp() throws IOException {
    Files.createDirectories(input);
    Files.createDirectories(output);
    Files.createDirectories(archive);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(input.toFile());
    FileUtils.deleteDirectory(output.toFile());
    FileUtils.deleteDirectory(archive.toFile());
  }

  @Test
  void acceptsUnencodedMp4FileAndGeneratesOutputAndArchivePaths() {
    Path unencodedMp4File = input.resolve("file.mp4");

    UnencodedVideo unencodedVideo = new UnencodedVideo(unencodedMp4File, input, output, archive);

    assertThat(unencodedVideo.originalPath()).isSameInstanceAs(unencodedMp4File);
    assertThat(unencodedVideo.encodedPath()).isEqualTo(output.resolve("file - CFR.mp4"));
    assertThat(unencodedVideo.tempEncodedPath())
        .isEqualTo(output.resolve("file - CFR (incomplete).mp4"));
    assertThat(unencodedVideo.archivedPath()).isEqualTo(archive.resolve("file - Archived.mp4"));
  }

  @Test
  void retainsDirectoryStructureForOutputAndArchivePaths() {
    Path mp4File = input.resolve("Halo/Campaign/file.mp4");

    UnencodedVideo unencodedVideo = new UnencodedVideo(mp4File, input, output, archive);

    assertThat(unencodedVideo.originalPath()).isSameInstanceAs(mp4File);
    assertThat(unencodedVideo.encodedPath())
        .isEqualTo(output.resolve("Halo/Campaign/file - CFR.mp4"));
    assertThat(unencodedVideo.tempEncodedPath())
        .isEqualTo(output.resolve("Halo/Campaign/file - CFR (incomplete).mp4"));
    assertThat(unencodedVideo.archivedPath())
        .isEqualTo(archive.resolve("Halo/Campaign/file - Archived.mp4"));
  }

  @Test
  void rejectsNonMp4File() {
    Path mp3File = input.resolve("file.mp3");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo(mp3File, input, output, archive));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("videoPath (%s) does not represent an .mp4 file".formatted(mp3File));
  }

  @Test
  void rejectsEncodedMp4File() {
    Path encodedMp4File = input.resolve("file - CFR.mp4");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo(encodedMp4File, input, output, archive));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("videoPath (%s) represents an encoded .mp4 file".formatted(encodedMp4File));
  }

  @Test
  void rejectsTempEncodedMp4File() {
    Path tempEncodedMp4File = input.resolve("file - CFR (incomplete).mp4");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo(tempEncodedMp4File, input, output, archive));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo(
            "videoPath (%s) represents an incomplete encoded .mp4 file"
                .formatted(tempEncodedMp4File));
  }

  @Test
  void rejectsArchivedMp4File() {
    Path archivedMp4File = input.resolve("file - Archived.mp4");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo(archivedMp4File, input, output, archive));
    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("videoPath (%s) represents an archived .mp4 file".formatted(archivedMp4File));
  }

  @Test
  void rejectsNonDirectoryInputDirectory() {
    Path mp4File = input.resolve("file.mp4");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo(mp4File, mp4File, output, archive));

    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("inputDirectory (%s) is not a directory".formatted(mp4File));
  }

  @Test
  void rejectsNonDirectoryOutputDirectory() {
    Path mp4File = input.resolve("file.mp4");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo(mp4File, input, mp4File, archive));

    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("outputDirectory (%s) is not a directory".formatted(mp4File));
  }

  @Test
  void rejectsNonDirectoryArchiveDirectory() {
    Path mp4File = input.resolve("file.mp4");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo(mp4File, input, output, mp4File));

    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo("archiveDirectory (%s) is not a directory".formatted(mp4File));
  }

  @Test
  void rejectsVideoPathIfNotChildOfInputDirectory() {
    Path mp4File = output.resolve("file.mp4");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> new UnencodedVideo(mp4File, input, output, archive));

    assertThat(thrown)
        .hasMessageThat()
        .isEqualTo(
            "videoPath (%s) is not a child of inputDirectory (%s)".formatted(mp4File, input));
  }
}
