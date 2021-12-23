package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents an unencoded video (.mp4 file).
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
class UnencodedVideo {

  private static final String MP4_SUFFIX = ".mp4";
  private static final String ENCODED_MP4_SUFFIX = " - CFR 60 FPS.mp4";
  private static final String TEMP_ENCODED_MP4_SUFFIX = " - CFR 60 FPS (incomplete).mp4";

  public static boolean isMp4(Path path) {
    return path.toString().endsWith(MP4_SUFFIX);
  }

  public static boolean isEncodedMp4(Path path) {
    return path.toString().endsWith(ENCODED_MP4_SUFFIX);
  }

  public static boolean isTempEncodedMp4(Path path) {
    return path.toString().endsWith(TEMP_ENCODED_MP4_SUFFIX);
  }

  private final Path originalPath;
  private final Path encodedPath;
  private final Path tempEncodedPath;

  UnencodedVideo(Path path) {
    checkArgument(isMp4(path), "Path does not represent an .mp4 file: %s", path);
    checkArgument(!isEncodedMp4(path), "Path represents an encoded .mp4 file: %s", path);
    checkArgument(
        !isTempEncodedMp4(path), "Path represents an incomplete encoded .mp4 file: %s", path);

    originalPath = path;

    String fileName = checkNotNull(path.getFileName()).toString();

    String encodedFileName = fileName.replace(MP4_SUFFIX, ENCODED_MP4_SUFFIX);
    encodedPath = path.resolveSibling(encodedFileName);

    String tempEncodedFileName = fileName.replace(MP4_SUFFIX, TEMP_ENCODED_MP4_SUFFIX);
    tempEncodedPath = path.resolveSibling(tempEncodedFileName);
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
}
