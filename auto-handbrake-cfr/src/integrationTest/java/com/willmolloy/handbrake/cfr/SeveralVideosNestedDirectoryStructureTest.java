package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Encoding several videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class SeveralVideosNestedDirectoryStructureTest extends BaseIntegrationTest {

  @ParameterizedTest
  @ArgumentsSource(EncodeAndArchiveToSameDirectory.class)
  @ArgumentsSource(EncodeToDifferentDirectory.class)
  @ArgumentsSource(ArchiveToDifferentDirectory.class)
  @ArgumentsSource(EncodeAndArchiveToDifferentDirectory.class)
  void canEncodeSeveralVideos_and_maintainNestedDirectoryStructures(
      Path inputDirectory, Path outputDirectory, Path archiveDirectory) throws Exception {
    // Given
    // videos to encode
    createVideoAt(inputDirectory.resolve("Nested1/Nested2/recording1.mp4"), unencodedVideo2);
    createVideoAt(inputDirectory.resolve("Nested/recording2.mp4"), unencodedVideo1);
    createVideoAt(inputDirectory.resolve("recording3.mp4"), unencodedVideo1);
    createVideoAt(inputDirectory.resolve("recording4.mp4"), unencodedVideo2);

    // When
    boolean result = app.run(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encodings
            pathAndContents(
                outputDirectory.resolve("Nested1/Nested2/recording1.cfr.mp4"), encodedVideo2),
            pathAndContents(outputDirectory.resolve("Nested/recording2.cfr.mp4"), encodedVideo1),

            pathAndContents(outputDirectory.resolve("recording3.cfr.mp4"), encodedVideo1),
            pathAndContents(outputDirectory.resolve("recording4.cfr.mp4"), encodedVideo2),

            // archives
            pathAndContents(
                archiveDirectory.resolve("Nested1/Nested2/recording1.mp4"), unencodedVideo2),
            pathAndContents(archiveDirectory.resolve("Nested/recording2.mp4"), unencodedVideo1),
            pathAndContents(archiveDirectory.resolve("recording3.mp4"), unencodedVideo1),
            pathAndContents(archiveDirectory.resolve("recording4.mp4"), unencodedVideo2)
           );
  }
}
