package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Scenarios where unrelated files exist.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class UnrelatedFilesExistTest extends BaseIntegrationTest {

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToSameDirectory.class)
  @ArgumentsSource(EncodeToDifferentDirectory.class)
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void retainsUnrelatedFilesInAllDirectories(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // unrelated files (copy videos so it has some contents to compare)
    createVideoAt(inputDirectory.resolve("song.mp3"), unencodedVideo2);
    createVideoAt(outputDirectory.resolve("picture.jpg"), encodedVideo1);
    createVideoAt(archiveDirectory.resolve("document.txt"), encodedVideo2);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo1),
            // unrelated files
            pathAndContents(inputDirectory.resolve("song.mp3"), unencodedVideo2),
            pathAndContents(outputDirectory.resolve("picture.jpg"), encodedVideo1),
            pathAndContents(archiveDirectory.resolve("document.txt"), encodedVideo2));
  }

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToSameDirectory.class)
  @ArgumentsSource(EncodeToDifferentDirectory.class)
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void retainsUnrelatedCompleteEncodingsAndArchives(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // unrelated encodings
    createVideoAt(outputDirectory.resolve("recording1.cfr.mp4"), encodedVideo1);
    createVideoAt(outputDirectory.resolve("recording2.cfr.mp4"), encodedVideo1);
    createVideoAt(outputDirectory.resolve("recording3.cfr.mp4"), encodedVideo1);

    // unrelated archives
    createVideoAt(archiveDirectory.resolve("recording1.mp4"), unencodedVideo1);
    createVideoAt(archiveDirectory.resolve("recording2.mp4"), unencodedVideo1);
    createVideoAt(archiveDirectory.resolve("recording3.mp4"), unencodedVideo1);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo1),
            // unrelated encodings
            pathAndContents(outputDirectory.resolve("recording1.cfr.mp4"), encodedVideo1),
            pathAndContents(outputDirectory.resolve("recording2.cfr.mp4"), encodedVideo1),
            pathAndContents(outputDirectory.resolve("recording3.cfr.mp4"), encodedVideo1),
            // unrelated archives
            pathAndContents(archiveDirectory.resolve("recording1.mp4"), unencodedVideo1),
            pathAndContents(archiveDirectory.resolve("recording2.mp4"), unencodedVideo1),
            pathAndContents(archiveDirectory.resolve("recording3.mp4"), unencodedVideo1));
  }
}
