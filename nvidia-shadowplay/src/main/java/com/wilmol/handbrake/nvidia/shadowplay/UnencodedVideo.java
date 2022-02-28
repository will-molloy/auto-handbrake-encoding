package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import com.wilmol.handbrake.core.HandBrake;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents an unencoded, unarchived video (.mp4 file).
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class UnencodedVideo {

  private static final Logger log = LogManager.getLogger();

  private static final String MP4_SUFFIX = ".mp4";
  private static final String ENCODED_MP4_SUFFIX = " - CFR.mp4";
  private static final String TEMP_ENCODED_MP4_SUFFIX = " - CFR (incomplete).mp4";
  private static final String ARCHIVED_SUFFIX = " - Archived.mp4";

  public static boolean isMp4(Path path) {
    return path.toString().endsWith(MP4_SUFFIX);
  }

  public static boolean isEncodedMp4(Path path) {
    return path.toString().endsWith(ENCODED_MP4_SUFFIX);
  }

  public static boolean isTempEncodedMp4(Path path) {
    return path.toString().endsWith(TEMP_ENCODED_MP4_SUFFIX);
  }

  public static boolean isArchivedMp4(Path path) {
    return path.toString().endsWith(ARCHIVED_SUFFIX);
  }

  private final Path videoPath;
  private final Path inputDirectory;
  private final Path outputDirectory;
  private final Path archiveDirectory;

  UnencodedVideo(Path videoPath, Path inputDirectory, Path outputDirectory, Path archiveDirectory) {
    checkArgument(isMp4(videoPath), "videoPath (%s) does not represent an .mp4 file", videoPath);
    checkArgument(
        !isEncodedMp4(videoPath), "videoPath (%s) represents an encoded .mp4 file", videoPath);
    checkArgument(
        !isTempEncodedMp4(videoPath),
        "videoPath (%s) represents an incomplete encoded .mp4 file",
        videoPath);
    checkArgument(
        !isArchivedMp4(videoPath), "videoPath (%s) represents an archived .mp4 file", videoPath);

    checkArgument(
        Files.isDirectory(inputDirectory),
        "inputDirectory (%s) is not a directory",
        inputDirectory);
    checkArgument(
        Files.isDirectory(outputDirectory),
        "outputDirectory (%s) is not a directory",
        outputDirectory);
    checkArgument(
        Files.isDirectory(archiveDirectory),
        "archiveDirectory (%s) is not a directory",
        archiveDirectory);

    checkArgument(
        videoPath.startsWith(inputDirectory),
        "videoPath (%s) is not a child of inputDirectory (%s)",
        videoPath,
        inputDirectory);

    this.videoPath = videoPath;
    this.inputDirectory = inputDirectory;
    this.outputDirectory = outputDirectory;
    this.archiveDirectory = archiveDirectory;
  }

  public boolean hasBeenEncoded() {
    return Files.exists(encodedPath());
  }

  public Path originalPath() {
    return videoPath;
  }

  public Path encodedPath() {
    String encodedFileName = fileName().replace(MP4_SUFFIX, ENCODED_MP4_SUFFIX);
    return outputDirectory.resolve(relativePathFromInput()).resolveSibling(encodedFileName);
  }

  public Path tempEncodedPath() {
    String tempEncodedFileName = fileName().replace(MP4_SUFFIX, TEMP_ENCODED_MP4_SUFFIX);
    return outputDirectory.resolve(relativePathFromInput()).resolveSibling(tempEncodedFileName);
  }

  public Path archivedPath() {
    String archivedFileName = fileName().replace(MP4_SUFFIX, ARCHIVED_SUFFIX);
    return archiveDirectory.resolve(relativePathFromInput()).resolveSibling(archivedFileName);
  }

  private String fileName() {
    return checkNotNull(videoPath.getFileName()).toString();
  }

  private Path relativePathFromInput() {
    return inputDirectory.relativize(videoPath);
  }

  void archive() throws IOException {
    Files.createDirectories(checkNotNull(archivedPath().getParent()));
    Files.move(originalPath(), archivedPath());
    log.info("Archived: {} -> {}", originalPath(), archivedPath());
  }

  void encode(HandBrake handBrake) throws IOException {
    Stopwatch stopwatch = Stopwatch.createStarted();

    // to avoid leaving encoded files in an 'incomplete' state, encode to a temp file in case
    // something goes wrong
    boolean encodeSuccessful = handBrake.encode(originalPath(), tempEncodedPath());

    if (encodeSuccessful) {
      // only archive the original after renaming the temp file, then it'll never reach a state
      // where the encoding is incomplete and the original doesn't exist
      Files.move(tempEncodedPath(), encodedPath());
      log.info("Encoded: {} -> {}", originalPath(), encodedPath());

      archive();
    } else {
      log.error("Failed to encode: {}", originalPath());
    }

    log.info("Elapsed: {}", stopwatch.elapsed());
  }
}
