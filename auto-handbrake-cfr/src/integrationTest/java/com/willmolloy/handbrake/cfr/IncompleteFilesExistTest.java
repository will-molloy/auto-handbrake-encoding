package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Scenarios where incomplete (encoding/archive) files exist.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class IncompleteFilesExistTest extends BaseIntegrationTest {

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToSameDirectory.class)
  @ArgumentsSource(EncodeToDifferentDirectory.class)
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void deletesIncompleteEncodingsAndArchivesInAllDirectories(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws IOException {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording1.cfr.mp4.part"), unencodedVideo1);
    createVideoAt(outputDirectory.resolve("recording2.cfr.mp4.part"), unencodedVideo1);
    createVideoAt(archiveDirectory.resolve("recording3.cfr.mp4.part"), unencodedVideo1);

    // incomplete archives
    createVideoAt(inputDirectory.resolve("vid1.mp4.part"), unencodedVideo1);
    createVideoAt(outputDirectory.resolve("vid2.mp4.part"), unencodedVideo1);
    createVideoAt(archiveDirectory.resolve("vid3.mp4.part"), unencodedVideo1);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo1));
  }
}
