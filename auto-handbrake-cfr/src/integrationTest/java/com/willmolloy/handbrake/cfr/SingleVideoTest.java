package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Single video scenarios (simplest case).
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class SingleVideoTest extends BaseIntegrationTest {

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToSameDirectory.class)
  @ArgumentsSource(EncodeToDifferentDirectory.class)
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void canEncodeASingleVideo(Path inputDirectory, Path outputDirectory, Path archiveDirectory)
      throws IOException {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo1);

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
