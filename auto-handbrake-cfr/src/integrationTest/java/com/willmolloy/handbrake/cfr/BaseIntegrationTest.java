package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import com.google.common.truth.Correspondence;
import com.google.common.truth.IterableSubject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.willmolloy.handbrake.core.HandBrake;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Various scenarios testing {@link App} as a black box. Read the logs to understand each scenario
 * further.
 *
 * <p>Requires HandBrakeCLI to be installed.
 *
 * <p>May require re-encoding the .cfr.mp4 file in resources directory.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
abstract class BaseIntegrationTest {

  protected Path testDirectory;
  protected Path inputDirectory;
  protected Path outputDirectory;
  protected Path archiveDirectory;
  protected Path unencodedVideo;
  protected Path encodedVideo;
  protected Path unencodedVideo2;

  @BeforeEach
  void setUp() throws Exception {
    testDirectory = Path.of(this.getClass().getSimpleName());
    inputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay"));
    outputDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Encoded"));
    archiveDirectory = createDirectoryAt(testDirectory.resolve("Gameplay Archive"));
    unencodedVideo = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_1MB.mp4").toURI());
    encodedVideo = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_1MB.cfr.mp4").toURI());
    unencodedVideo2 = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_2MB.mp4").toURI());
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.deleteDirectory(testDirectory.toFile());
    FileUtils.deleteDirectory(inputDirectory.toFile());
    FileUtils.deleteDirectory(outputDirectory.toFile());
    FileUtils.deleteDirectory(archiveDirectory.toFile());
  }

  @CanIgnoreReturnValue
  private Path createDirectoryAt(Path path) throws IOException {
    // not returning result of Files.createDirectories - it's absolute rather than relative, which
    // breaks the tests
    Files.createDirectories(path);
    return path;
  }

  @CanIgnoreReturnValue
  protected Path createVideoAt(Path path, Path videoToCopy) throws IOException {
    Files.createDirectories(checkNotNull(path.getParent()));
    Files.copy(videoToCopy, path);
    return path;
  }

  protected boolean runApp(Path inputDirectory, Path outputDirectory, Path archiveDirectory) {
    App app = new App(new VideoEncoder(HandBrake.newInstance()), new VideoArchiver());
    return app.run(inputDirectory, outputDirectory, archiveDirectory);
  }

  protected IterableSubject.UsingCorrespondence<Path, PathAndContents> assertThatTestDirectory()
      throws IOException {
    return assertThat(Files.walk(testDirectory).filter(Files::isRegularFile).toList())
        .comparingElementsUsing(PathAndContents.EQUIVALENCE);
  }

  protected PathAndContents pathAndContents(Path path, Path contents) {
    return new PathAndContents(path, contents);
  }

  /**
   * Represents path and path contents separately.
   *
   * <p>Useful so we can compare them separately and avoid creating files. E.g. we can say "this
   * file should exist with this path and contents matching this other file".
   *
   * @param path path to compare
   * @param contents path contents to compare
   */
  protected record PathAndContents(Path path, Path contents) {
    private static final Correspondence<Path, PathAndContents> EQUIVALENCE =
        Correspondence.from(PathAndContents::recordsEquivalent, "is equivalent to")
            .formattingDiffsUsing(PathAndContents::formatRecordDiff);

    private static final double PERCENT_MISMATCH_TOLERANCE = 0.01;

    private static boolean recordsEquivalent(Path actual, PathAndContents expected) {
      return actual.equals(expected.path)
          && percentMismatch(actual, expected.contents) < PERCENT_MISMATCH_TOLERANCE;
    }

    private static String formatRecordDiff(Path actual, PathAndContents expected) {
      if (!actual.equals(expected.path)) {
        return "paths not equal";
      }
      double percentMismatch = percentMismatch(actual, expected.contents);
      if (percentMismatch >= PERCENT_MISMATCH_TOLERANCE) {
        return "contents not similar, percentMismatch=%s%%".formatted(percentMismatch * 100);
      }
      throw new AssertionError("Unreachable");
    }

    // HandBrake is not deterministic (encoding doesn't always produce the exact same output) so
    // need a method to test file contents are similar.
    private static double percentMismatch(Path path1, Path path2) {
      try {
        byte[] bytes1 = Files.readAllBytes(path1);
        byte[] bytes2 = Files.readAllBytes(path2);
        // pad arrays to same length
        byte[] paddedBytes1 = Arrays.copyOf(bytes1, Math.max(bytes1.length, bytes2.length));
        byte[] paddedBytes2 = Arrays.copyOf(bytes2, Math.max(bytes1.length, bytes2.length));

        long mismatchCount =
            IntStream.range(0, paddedBytes1.length)
                .filter(i -> paddedBytes1[i] != paddedBytes2[i])
                .count();

        return (double) mismatchCount / paddedBytes1.length;
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }
}
