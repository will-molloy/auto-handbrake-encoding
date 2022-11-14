package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Scenarios where unrelated complete (encoding/archive) files exist.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class UnrelatedFilesExistTest extends BaseIntegrationTest {

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void retainsUnrelatedCompleteEncodingsAndArchivesInAllDirectories(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

    // unrelated encodings
    createVideoAt(inputDirectory.resolve("recording.cfr.mp4"), encodedVideo1);
    createVideoAt(outputDirectory.resolve("recording.cfr.mp4"), encodedVideo1);
    createVideoAt(archiveDirectory.resolve("recording.cfr.mp4"), encodedVideo1);

    // unrelated archives
    createVideoAt(inputDirectory.resolve("recording.mp4"), unencodedVideo1);
    createVideoAt(outputDirectory.resolve("recording.mp4"), unencodedVideo1);
    createVideoAt(archiveDirectory.resolve("recording.mp4"), unencodedVideo1);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isFalse();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo1),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo1),
            // unrelated encodings
            pathAndContents(inputDirectory.resolve("recording.cfr.mp4"), encodedVideo1),
            pathAndContents(outputDirectory.resolve("recording.cfr.mp4"), encodedVideo1),
            pathAndContents(archiveDirectory.resolve("recording.cfr.mp4"), encodedVideo1),
            // unrelated archives
            pathAndContents(inputDirectory.resolve("recording.mp4"), unencodedVideo1),
            pathAndContents(outputDirectory.resolve("recording.mp4"), unencodedVideo1),
            pathAndContents(archiveDirectory.resolve("recording.mp4"), unencodedVideo1));
  }
}
