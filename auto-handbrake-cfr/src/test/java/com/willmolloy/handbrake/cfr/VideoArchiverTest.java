package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.truth.StreamSubject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * VideoArchiverTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class VideoArchiverTest {

  private FileSystem fileSystem;
  private Path inputDirectory;
  private Path archiveDirectory;
  private Path testVideo;
  private Path testVideo2;

  private UnencodedVideo.Factory unencodedVideoFactory;

  private final VideoArchiver videoArchiver = new VideoArchiver();

  @BeforeEach
  void setUp() throws IOException, URISyntaxException {
    fileSystem = Jimfs.newFileSystem(Configuration.unix());

    inputDirectory = fileSystem.getPath("/input/Videos/Gameplay");
    Path outputDirectory = fileSystem.getPath("/output/Videos/Encoded Gameplay");
    archiveDirectory = fileSystem.getPath("/archive/Videos/Gameplay");

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
    fileSystem.close();
  }

  @Test
  void movesInputFileToArchiveDirectory() throws IOException {
    // Given
    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    boolean result = videoArchiver.archive(unencodedVideo);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory().containsExactly(unencodedVideo.archivedPath());
  }

  @Test
  void retainsDirectoryStructureRelativeToInputCreatingParentDirectoryIfNeeded()
      throws IOException {
    // Given
    Files.createDirectories(inputDirectory.resolve("Halo/Campaign"));
    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("Halo/Campaign/file.mp4")));

    // When
    boolean result = videoArchiver.archive(unencodedVideo);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory().containsExactly(unencodedVideo.archivedPath());
  }

  @Test
  void whenArchiveFileAlreadyExists_deletesOriginal_andReturnsTrue() throws IOException {
    // Given
    Files.copy(testVideo, archiveDirectory.resolve("file.mp4"));

    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    boolean result = videoArchiver.archive(unencodedVideo);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory().containsExactly(unencodedVideo.archivedPath());
  }

  @Test
  void whenArchiveFileAlreadyExistsButContentsDiffer_retainsBothFiles_andReturnsFalse()
      throws IOException {
    // Given
    Files.copy(testVideo2, archiveDirectory.resolve("file.mp4"));

    UnencodedVideo unencodedVideo =
        unencodedVideoFactory.newUnencodedVideo(
            Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    boolean result = videoArchiver.archive(unencodedVideo);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(unencodedVideo.originalPath(), unencodedVideo.archivedPath());
  }

  @Test
  void whenInputDirectoryIsArchiveDirectory_retainsOriginal_andReturnsTrue() throws IOException {
    // Given
    UnencodedVideo.Factory factory =
        new UnencodedVideo.Factory(inputDirectory, inputDirectory, inputDirectory);
    UnencodedVideo unencodedVideo =
        factory.newUnencodedVideo(Files.copy(testVideo, inputDirectory.resolve("file.mp4")));

    // When
    boolean result = videoArchiver.archive(unencodedVideo);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory().containsExactly(unencodedVideo.originalPath());
  }

  @Test
  void exceptionCaughtReturnsFalse() {
    // When
    // null input forces NPE
    boolean result = videoArchiver.archive(null);

    // Then
    assertThat(result).isFalse();
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    try (Stream<Path> testFiles = Files.walk(fileSystem.getPath("/"))) {
      return assertThat(testFiles.filter(Files::isRegularFile));
    }
  }
}
