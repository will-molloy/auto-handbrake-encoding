package com.willmolloy.handbrake.cfr.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * File utility methods. (Extension to {@link Files}.)
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public final class Files2 {

  /**
   * Tests if two files have similar contents.
   *
   * <p>Specifically if <1% of bytes mismatch.
   *
   * @param path1 first file
   * @param path2 second file
   * @return {@code true} if the files contents are similar
   * @see Files#mismatch
   */
  // HandBrake is not deterministic (encoding doesn't always produce the exact same output) so need
  // a method to test file contents are similar when comparing encoded files.
  public static boolean contentsSimilar(Path path1, Path path2) {
    return percentMismatch(path1, path2) < 0.01;
  }

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

  private Files2() {}
}
