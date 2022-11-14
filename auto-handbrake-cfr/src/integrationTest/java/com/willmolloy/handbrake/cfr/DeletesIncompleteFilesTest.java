package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Scenarios where incomplete (encoding/archive) files exist.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class DeletesIncompleteFilesTest extends BaseIntegrationTest {

  @Test
  void singleVideo_deletesIncompleteEncodingsAndArchives() throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording.cfr.mp4.part"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("recording2.cfr.mp4.part"), unencodedVideo);

    // incomplete archives
    createVideoAt(inputDirectory.resolve("vid.mp4.part"), unencodedVideo);
    createVideoAt(inputDirectory.resolve("vid2.mp4.part"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, inputDirectory, inputDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(inputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(inputDirectory.resolve("my video.mp4"), unencodedVideo));
  }

  @Test
  void
      singleVideo_encodeAndArchiveToDifferentDirectory_deletesIncompleteEncodingsAndArchivesInAllDirectories()
          throws Exception {
    // Given
    // video to encode
    createVideoAt(inputDirectory.resolve("my video.mp4"), unencodedVideo);

    // incomplete encodings
    createVideoAt(inputDirectory.resolve("recording.cfr.mp4.part"), unencodedVideo);
    createVideoAt(outputDirectory.resolve("recording.cfr.mp4.part"), unencodedVideo);
    createVideoAt(archiveDirectory.resolve("recording.cfr.mp4.part"), unencodedVideo);

    // incomplete archives
    createVideoAt(inputDirectory.resolve("vid.mp4.part"), unencodedVideo);
    createVideoAt(outputDirectory.resolve("vid.mp4.part"), unencodedVideo);
    createVideoAt(archiveDirectory.resolve("vid.mp4.part"), unencodedVideo);

    // When
    boolean result = runApp(inputDirectory, outputDirectory, archiveDirectory);

    // Then
    assertThat(result).isTrue();
    assertThatTestDirectory()
        .containsExactly(
            // encoding
            pathAndContents(outputDirectory.resolve("my video.cfr.mp4"), encodedVideo),
            // archive
            pathAndContents(archiveDirectory.resolve("my video.mp4"), unencodedVideo));
  }
}
