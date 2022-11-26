package com.willmolloy.handbrake.cfr;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for scanning the directories.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
class DirectoryScanner {

  private static final Logger log = LogManager.getLogger();

  private final Path inputDirectory;
  private final Path outputDirectory;
  private final Path archiveDirectory;
  private final UnencodedVideo.Factory factory;

  DirectoryScanner(Path inputDirectory, Path outputDirectory, Path archiveDirectory) {
    this.inputDirectory = checkNotNull(inputDirectory);
    this.outputDirectory = checkNotNull(outputDirectory);
    this.archiveDirectory = checkNotNull(archiveDirectory);
    factory = new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);
  }

  List<UnencodedVideo> scan() throws IOException {
    deleteIncompleteEncodingsAndArchives();
    return getUnencodedVideos();
  }

  private void deleteIncompleteEncodingsAndArchives() throws IOException {
    List<Path> tempFiles =
        Stream.of(inputDirectory, outputDirectory, archiveDirectory)
            .distinct()
            .flatMap(
                directory -> {
                  try {
                    return Files.walk(directory)
                        .filter(Files::isRegularFile)
                        .filter(
                            file ->
                                UnencodedVideo.isTempEncodedMp4(file)
                                    || UnencodedVideo.isTempArchivedMp4(file));
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                })
            .toList();

    if (!tempFiles.isEmpty()) {
      log.warn("Detected {} incomplete encoding(s)/archives(s)", tempFiles.size());
      for (int i : IntStream.range(0, tempFiles.size()).toArray()) {
        log.warn("Deleting ({}/{}): {}", i + 1, tempFiles.size(), tempFiles.get(i));
        Files.deleteIfExists(tempFiles.get(i));
      }
    }
  }

  private List<UnencodedVideo> getUnencodedVideos() throws IOException {
    List<UnencodedVideo> videos =
        Files.walk(inputDirectory)
            .filter(Files::isRegularFile)
            .filter(UnencodedVideo::isMp4)
            // don't include paths that represent already encoded videos
            .filter(path -> !UnencodedVideo.isEncodedMp4(path))
            .map(factory::newUnencodedVideo)
            .sorted(Comparator.comparing(video -> video.originalPath().toString()))
            .toList();

    log.info("Detected {} video(s) to encode", videos.size());
    for (int i : IntStream.range(0, videos.size()).toArray()) {
      log.info("Detected ({}/{}): {}", i + 1, videos.size(), videos.get(i));
    }

    return videos;
  }
}
