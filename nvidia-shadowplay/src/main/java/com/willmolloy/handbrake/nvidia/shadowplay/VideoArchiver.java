package com.willmolloy.handbrake.nvidia.shadowplay;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Stopwatch;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for archiving videos.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
public class VideoArchiver {

  private static final Logger log = LogManager.getLogger();

  /**
   * Archives the given video.
   *
   * @param video video to archive
   */
  public CompletableFuture<Void> archiveAsync(UnencodedVideo video) {
    return CompletableFuture.runAsync(
        () -> {
          Stopwatch stopwatch = Stopwatch.createStarted();
          try {
            log.info("Archiving: {} -> {}", video.originalPath(), video.archivedPath());

            if (Files.exists(video.archivedPath())) {
              if (Files.mismatch(video.originalPath(), video.archivedPath()) == -1) {
                log.warn(
                    "Archive file ({}) already exists, deleting original", video.archivedPath());
                Files.delete(video.originalPath());
              } else {
                log.warn(
                    "Archive file ({}) already exists but contents differ, keeping original",
                    video.archivedPath());
              }
              return;
            }

            Files.createDirectories(checkNotNull(video.archivedPath().getParent()));

            // archive to a temp file first in case something goes wrong
            // (e.g. app crash while it's uploading to NAS)
            Files.move(video.originalPath(), video.tempArchivedPath());
            Files.move(video.tempArchivedPath(), video.archivedPath());

            log.info("Archived: {} - elapsed: {}", video.archivedPath(), stopwatch.elapsed());
          } catch (Exception e) {
            log.error("Error archiving: %s".formatted(video), e);
          }
        });
  }
}
