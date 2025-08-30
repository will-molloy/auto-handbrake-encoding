package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.common.io.Resources;
import com.google.common.truth.Correspondence;
import com.google.common.truth.IterableSubject;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.willmolloy.handbrake.cfr.util.Files2;
import com.willmolloy.handbrake.core.HandBrake;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

/**
 * Various scenarios testing {@link App} as a black box.
 *
 * <p>Requires HandBrakeCLI to be installed.
 *
 * <p>May require re-encoding the encoded files in resources directory.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
abstract class BaseIntegrationTest {

  protected static App app;
  private static Path testParentDirectory;
  protected static Path unencodedVideo1;
  protected static Path encodedVideo1;
  protected static Path unencodedVideo2;
  protected static Path encodedVideo2;
  private static final Random RANDOM = new Random();

  @BeforeAll
  static void setUp() throws URISyntaxException {
    testParentDirectory = Path.of("out/Test" + RANDOM.nextLong());
    unencodedVideo1 = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_1MB.mp4").toURI());
    encodedVideo1 = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_1MB.cfr.mp4").toURI());
    unencodedVideo2 = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_2MB.mp4").toURI());
    encodedVideo2 = Path.of(Resources.getResource("Big_Buck_Bunny_360_10s_2MB.cfr.mp4").toURI());
  }

  @AfterEach
  void tearDown() throws IOException {
    MoreFiles.deleteRecursively(testParentDirectory, RecursiveDeleteOption.ALLOW_INSECURE);
  }

  protected static boolean runApp(Path inputDirectory, Path outputDirectory, Path archiveDirectory)
      throws IOException {
    app =
        new App(
            new DirectoryScanner(inputDirectory, outputDirectory, archiveDirectory),
            new JobQueue(new VideoEncoder(HandBrake.newInstance()), new VideoArchiver()));
    return app.run();
  }

  @CanIgnoreReturnValue
  private static Path createDirectoryAt(Path path) throws IOException {
    // not returning result of Files.createDirectories - it's absolute rather than relative, which
    // breaks the tests
    Files.createDirectories(path);
    return path;
  }

  @CanIgnoreReturnValue
  protected static Path createVideoAt(Path path, Path videoToCopy) throws IOException {
    MoreFiles.createParentDirectories(path);
    Files.copy(videoToCopy, path);
    return path;
  }

  protected static IterableSubject.UsingCorrespondence<Path, PathAndContents>
      assertThatTestDirectory() throws IOException {
    try (Stream<Path> testFiles = Files.walk(testParentDirectory)) {
      return assertThat(testFiles.filter(Files::isRegularFile).toList())
          .comparingElementsUsing(PathAndContents.EQUIVALENCE);
    }
  }

  protected static PathAndContents pathAndContents(Path path, Path contents) {
    return new PathAndContents(path, contents);
  }

  /**
   * Represents path and path contents separately.
   *
   * <p>Useful so we can compare them separately. E.g. we can say "this file should exist with this
   * path and contents matching this other file".
   *
   * @param path path to compare
   * @param contents path whose contents to compare
   */
  protected record PathAndContents(Path path, Path contents) {
    private static final Correspondence<Path, PathAndContents> EQUIVALENCE =
        Correspondence.from(PathAndContents::recordsEquivalent, "is equivalent to")
            .formattingDiffsUsing(PathAndContents::formatRecordDiff);

    private static boolean recordsEquivalent(Path actual, PathAndContents expected) {
      return actual.equals(expected.path) && Files2.contentsSimilar(actual, expected.contents());
    }

    private static String formatRecordDiff(Path actual, PathAndContents expected) {
      if (!actual.equals(expected.path)) {
        return "paths not equal";
      }
      if (!Files2.contentsSimilar(actual, expected.contents())) {
        return "contents not similar";
      }
      throw new AssertionError("Unreachable");
    }
  }

  /** Encode and archive to the same directory (i.e. {@code inputDirectory}). */
  protected static class EncodeAndArchiveToSameDirectory implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws IOException {
      Path inputDirectory = createDirectoryAt(testParentDirectory.resolve("Gameplay"));
      return Stream.of(Arguments.of(inputDirectory, inputDirectory, inputDirectory));
    }
  }

  /** Encode to a different directory. */
  protected static class EncodeToDifferentDirectory implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws IOException {
      Path inputDirectory = createDirectoryAt(testParentDirectory.resolve("Gameplay"));
      Path outputDirectory = createDirectoryAt(testParentDirectory.resolve("Gameplay Encoded"));
      return Stream.of(Arguments.of(inputDirectory, outputDirectory, inputDirectory));
    }
  }

  /** Archive to a different directory. */
  protected static class ArchiveToDifferentDirectory implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws IOException {
      Path inputDirectory = createDirectoryAt(testParentDirectory.resolve("Gameplay"));
      Path archiveDirectory = createDirectoryAt(testParentDirectory.resolve("Gameplay Archive"));
      return Stream.of(Arguments.of(inputDirectory, inputDirectory, archiveDirectory));
    }
  }

  /** Encode and archive to a different directory. */
  protected static class EncodeAndArchiveToDifferentDirectory implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws IOException {
      Path inputDirectory = createDirectoryAt(testParentDirectory.resolve("Gameplay"));
      Path outputDirectory = createDirectoryAt(testParentDirectory.resolve("Gameplay Encoded"));
      Path archiveDirectory = createDirectoryAt(testParentDirectory.resolve("Gameplay Archive"));
      return Stream.of(Arguments.of(inputDirectory, outputDirectory, archiveDirectory));
    }
  }
}
