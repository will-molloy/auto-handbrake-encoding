package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents an unencoded, unarchived video (.mp4 file).
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class UnencodedVideo {

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

  private final Path originalPath;
  private final Path encodedPath;
  private final Path tempEncodedPath;
  private final Path archivedPath;

  UnencodedVideo(Path videoPath, Path outputPath) {
    checkArgument(isMp4(videoPath), "Video path does not represent an .mp4 file: %s", videoPath);
    checkArgument(
        !isEncodedMp4(videoPath), "Video path represents an encoded .mp4 file: %s", videoPath);
    checkArgument(
        !isTempEncodedMp4(videoPath),
        "Video path represents an incomplete encoded .mp4 file: %s",
        videoPath);
    checkArgument(
        !isArchivedMp4(videoPath), "Video path represents an archived .mp4 file: %s", videoPath);

    originalPath = videoPath;

    String fileName = checkNotNull(videoPath.getFileName()).toString();

    String encodedFileName = fileName.replace(MP4_SUFFIX, ENCODED_MP4_SUFFIX);
    encodedPath = outputPath.resolve(encodedFileName);

    String tempEncodedFileName = fileName.replace(MP4_SUFFIX, TEMP_ENCODED_MP4_SUFFIX);
    tempEncodedPath = outputPath.resolve(tempEncodedFileName);

    String archivedFileName = fileName.replace(MP4_SUFFIX, ARCHIVED_SUFFIX);
    archivedPath = videoPath.resolveSibling(archivedFileName);
  }

  public boolean hasBeenEncoded() {
    return Files.exists(encodedPath);
  }

  public Path originalPath() {
    return originalPath;
  }

  public Path encodedPath() {
    return encodedPath;
  }

  public Path tempEncodedPath() {
    return tempEncodedPath;
  }

  public Path archivedPath() {
    return archivedPath;
  }
}
