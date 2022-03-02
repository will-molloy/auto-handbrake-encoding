package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for archiving videos.
 *
 * @author <a href=https://wilmol.com>Will Molloy</a>
 */
public class VideoArchiver {

  private static final Logger log = LogManager.getLogger();

  /**
   * Archives the given video.
   *
   * @param video video to archive
   */
  public void archive(UnencodedVideo video) {
    try {
      log.info("Archiving: {} -> {}", video.originalPath(), video.archivedPath());

      Files.createDirectories(checkNotNull(video.archivedPath().getParent()));

      Files.move(video.originalPath(), video.archivedPath());

      log.info("Archived: {} -> {}", video.originalPath(), video.archivedPath());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
