package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
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
  public CompletableFuture<Void> archiveAsync(UnencodedVideo video) {
    // run archiving async as it can be expensive (e.g. moving to another disk or NAS)
    // then while it archives it can encode the next video
    return CompletableFuture.runAsync(
        () -> {
          try {
            log.info("Archiving: {} -> {}", video.originalPath(), video.archivedPath());

            if (Files.exists(video.archivedPath())) {
              log.warn("Archive file ({}) already exists", video.archivedPath());
              // delete the archived file and try again
              // most likely it crashed and never fully moved the file
              Files.delete(video.archivedPath());
            }

            Files.createDirectories(checkNotNull(video.archivedPath().getParent()));

            Files.move(video.originalPath(), video.archivedPath());

            log.info("Archived: {} -> {}", video.originalPath(), video.archivedPath());
          } catch (IOException e) {
            log.error("Error archiving: %s".formatted(video), e);
          }
        });
  }
}
