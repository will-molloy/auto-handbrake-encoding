package com.wilmol.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

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
    return CompletableFuture.runAsync(
        () -> {
          try {
            log.info("Archiving: {} -> {}", video.originalPath(), video.archivedPath());

            if (Files.exists(video.archivedPath())) {
              log.warn("Archive file ({}) already exists, deleting original", video.archivedPath());
              Files.delete(video.originalPath());
              return;
            }

            Files.createDirectories(checkNotNull(video.archivedPath().getParent()));

            // archive to a temp file first in case something goes wrong
            // (e.g. app crash while it's uploading to NAS)
            Files.move(video.originalPath(), video.tempArchivedPath());
            Files.move(video.tempArchivedPath(), video.archivedPath());

            log.info("Archived: {} -> {}", video.originalPath(), video.archivedPath());
          } catch (Exception e) {
            log.error("Error archiving: %s".formatted(video), e);
          }
        });
  }
}
