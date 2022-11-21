package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Nested directory structure scenarios.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class SingleVideoNestedDirectoryStructureTest extends BaseIntegrationTest {

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToSameDirectory.class)
  @ArgumentsSource(EncodeToDifferentDirectory.class)
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void canEncodeASingleVideo_and_maintainNestedDirectoryStructure(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("League of Legends/ranked_game1.mp4"), unencodedVideo1);

    // When
    boolean result = app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(
                outputDirectory.resolve("League of Legends/ranked_game1.cfr.mp4"), encodedVideo1),
            // archive
            pathAndContents(
                archiveDirectory.resolve("League of Legends/ranked_game1.mp4"), unencodedVideo1));
  }
}
