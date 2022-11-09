package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import com.google.common.io.Resources;
import com.google.common.truth.StreamSubject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * VideoArchiverTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class VideoArchiverTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path archiveDirectory;
  private Path testVideo;
  private Path testVideo2;

  private UnencodedVideo.Factory unencodedVideoFactory;

  private final VideoArchiver videoArchiver = new VideoArchiver();

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    inputDirectory = testDirectory.resolve("input/Videos/Gameplay");
    Path outputDirectory = testDirectory.resolve("output/Videos/Encoded Gameplay");
    archiveDirectory = testDirectory.resolve("archive/Videos/Gameplay");
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
  void movesInputFileToArchiveDirectory() throws IOException {
    // Given
    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));
    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    boolean result = videoArchiver.archiveAsync(unencodedVideo).join();

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory().containsExactly(archiveDirectory.resolve("file.archived.mp4"));
  }

  @Test
  void retainsDirectoryStructureRelativeToInputCreatingParentDirectoryIfNeeded()
      throws IOException {
    // Given
    Files.createDirectories(inputDirectory.resolve("Halo/Campaign"));
    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("Halo/Campaign/file.mp4"));
    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    boolean result = videoArchiver.archiveAsync(unencodedVideo).join();

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(archiveDirectory.resolve("Halo/Campaign/file.archived.mp4"));
  }

  @Test
  void whenArchiveFileAlreadyExists_deletesOriginal_andReturnsTrue() throws IOException {
    // Given
    Files.copy(testVideo, archiveDirectory.resolve("file.archived.mp4"));

    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));
    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    boolean result = videoArchiver.archiveAsync(unencodedVideo).join();

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory().containsExactly(archiveDirectory.resolve("file.archived.mp4"));
  }

  @Test
  void whenArchiveFileAlreadyExistsButContentsDiffer_retainsBothFiles_andReturnsFalse()
      throws IOException {
    // Given
    Files.copy(testVideo2, archiveDirectory.resolve("file.archived.mp4"));

    Path unencodedMp4File = Files.copy(testVideo, inputDirectory.resolve("file.mp4"));
    UnencodedVideo unencodedVideo = unencodedVideoFactory.newUnencodedVideo(unencodedMp4File);

    // When
    boolean result = videoArchiver.archiveAsync(unencodedVideo).join();

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            inputDirectory.resolve("file.mp4"), archiveDirectory.resolve("file.archived.mp4"));
  }

  @Test
  void exceptionCaughtReturnsFalse() {
    // When
    // null input forces NPE
    boolean result = videoArchiver.archiveAsync(null).join();

    // Then
    assertThat(result).isFalse();
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile));
  }
}
