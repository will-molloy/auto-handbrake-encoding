package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth8.assertThat;

import com.google.common.io.Resources;
import com.google.common.truth.StreamSubject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * DirectoryScannerTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class DirectoryScannerTest {

  private Path testDirectory;
  private Path inputDirectory;
  private Path outputDirectory;
  private Path archiveDirectory;
  private Path testVideo;

  private DirectoryScanner directoryScanner;

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

    directoryScanner = new DirectoryScanner(inputDirectory, outputDirectory, archiveDirectory);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
  }

  @Test
  void deleteIncompleteEncodingsAndArchives_deletesIncompleteEncodings() throws Exception {
    // Given
    Files.copy(testVideo, inputDirectory.resolve("video1.cfr.mp4.part"));
    Files.copy(testVideo, outputDirectory.resolve("video2.cfr.mp4.part"));
    Files.copy(testVideo, archiveDirectory.resolve("video3.cfr.mp4.part"));

    // When
    directoryScanner.deleteIncompleteEncodingsAndArchives();

    // Then
    assertThatTestDirectory().isEmpty();
  }

  @Test
  void deleteIncompleteEncodingsAndArchives_deletesIncompleteArchives() throws Exception {
    // Given
    Files.copy(testVideo, inputDirectory.resolve("video1.mp4.part"));
    Files.copy(testVideo, outputDirectory.resolve("video2.mp4.part"));
    Files.copy(testVideo, archiveDirectory.resolve("video3.mp4.part"));

    // When
    directoryScanner.deleteIncompleteEncodingsAndArchives();

    // Then
    assertThatTestDirectory().isEmpty();
  }

  @Test
  void getUnencodedVideos_getsVideosToEncodeFromInputDirectory() throws Exception {
    // Given
    // expected
    Files.copy(testVideo, inputDirectory.resolve("video1.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video2.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video3.mp4"));
    // ignore - input
    Files.copy(testVideo, inputDirectory.resolve("video.cfr.mp4"));
    Files.copy(testVideo, inputDirectory.resolve("video.mp4.part"));
    Files.copy(testVideo, inputDirectory.resolve("video.cfr.mp4.part"));
    // ignore - output
    Files.copy(testVideo, outputDirectory.resolve("video.mp4"));
    Files.copy(testVideo, outputDirectory.resolve("video.cfr.mp4"));
    Files.copy(testVideo, outputDirectory.resolve("video.mp4.part"));
    Files.copy(testVideo, outputDirectory.resolve("video.cfr.mp4.part"));
    // ignore - archive
    Files.copy(testVideo, archiveDirectory.resolve("video.mp4"));
    Files.copy(testVideo, archiveDirectory.resolve("video.cfr.mp4"));
    Files.copy(testVideo, archiveDirectory.resolve("video.mp4.part"));
    Files.copy(testVideo, archiveDirectory.resolve("video.cfr.mp4.part"));

    // When
    List<UnencodedVideo> videos = directoryScanner.getUnencodedVideos();

    // Then
    assertThat(videos.stream().map(UnencodedVideo::originalPath))
        .containsExactly(
            inputDirectory.resolve("video1.mp4"),
            inputDirectory.resolve("video2.mp4"),
            inputDirectory.resolve("video3.mp4"));
  }

  private StreamSubject assertThatTestDirectory() throws IOException {
    try (Stream<Path> directoryWalk = Files.walk(testDirectory)) {
      return assertThat(directoryWalk.filter(Files::isRegularFile));
    }
  }
}
