package com.willmolloy.handbrake.cfr;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

/**
 * JobQueueTest.
 *
 * @author <a href=https://willmolloy.com>Will Molloy</a>
 */
@ExtendWith(MockitoExtension.class)
class JobQueueTest {

  @Mock private VideoEncoder mockVideoEncoder;
  @Mock private VideoArchiver mockVideoArchiver;
  @InjectMocks private JobQueue jobQueue;

  private FileSystem fileSystem;
  private Path inputDirectory;
  private UnencodedVideo.Factory factory;

  @BeforeEach
  void setUp() throws Exception {
    fileSystem = Jimfs.newFileSystem(Configuration.unix());

    inputDirectory = fileSystem.getPath("input");
    Path outputDirectory = fileSystem.getPath("output");
    Path archiveDirectory = fileSystem.getPath("archive");

    Files.createDirectories(inputDirectory);
    Files.createDirectories(outputDirectory);
    Files.createDirectories(archiveDirectory);

    factory = new UnencodedVideo.Factory(inputDirectory, outputDirectory, archiveDirectory);
  }

  @AfterEach
  void tearDown() throws IOException {
    fileSystem.close();
  }

  @Test
  void encodesVideoFilesAndArchivesOriginals() throws Exception {
    // Given
    whenVideoEncoderReturns(true);
    when(mockVideoArchiver.archive(any())).thenReturn(true);

    Files.createDirectories(inputDirectory.resolve("NestedFolder"));

    List<UnencodedVideo> videos =
        List.of(
            factory.newUnencodedVideo(inputDirectory.resolve("NestedFolder/video1.mp4")),
            factory.newUnencodedVideo(inputDirectory.resolve("video2.mp4")),
            factory.newUnencodedVideo(inputDirectory.resolve("video3.mp4")));

    // When
    boolean result = jobQueue.process(videos);

    // Then
    assertThat(result).isTrue();
    for (UnencodedVideo video : videos) {
      verify(mockVideoEncoder).encode(same(video));
      verify(mockVideoArchiver).archive(same(video));
    }
  }

  static Stream<Arguments> anyEncodeOrArchiveFailed() {
    return Stream.of(
        Arguments.of(true, true, false),
        Arguments.of(true, false, true),
        Arguments.of(false, true, true));
  }

  @ParameterizedTest
  @MethodSource("anyEncodeOrArchiveFailed")
  void
      whenEncodingFails_skipsArchiving_andStillEncodesAndArchivesOtherVideos_andReturnsFalseOverall(
          boolean result1, boolean result2, boolean result3) throws Exception {
    // Given
    whenVideoEncoderReturns(result1, result2, result3);
    when(mockVideoArchiver.archive(any())).thenReturn(true);

    Files.createDirectories(inputDirectory.resolve("NestedFolder"));

    List<UnencodedVideo> videos =
        List.of(
            factory.newUnencodedVideo(inputDirectory.resolve("NestedFolder/video1.mp4")),
            factory.newUnencodedVideo(inputDirectory.resolve("video2.mp4")),
            factory.newUnencodedVideo(inputDirectory.resolve("video3.mp4")));

    // When
    boolean result = jobQueue.process(videos);

    // Then
    assertThat(result).isFalse();
    for (UnencodedVideo video : videos) {
      verify(mockVideoEncoder).encode(same(video));
    }
    verify(mockVideoArchiver, times(result1 ? 1 : 0)).archive(same(videos.get(0)));
    verify(mockVideoArchiver, times(result2 ? 1 : 0)).archive(same(videos.get(1)));
    verify(mockVideoArchiver, times(result3 ? 1 : 0)).archive(same(videos.get(2)));
  }

  @ParameterizedTest
  @MethodSource("anyEncodeOrArchiveFailed")
  void whenArchivingFails_stillEncodesAndArchivesOtherVideos_andReturnsFalseOverall(
      boolean result1, boolean result2, boolean result3) throws Exception {
    // Given
    whenVideoEncoderReturns(true);
    when(mockVideoArchiver.archive(any())).thenReturn(result1, result2, result3);

    Files.createDirectories(inputDirectory.resolve("NestedFolder"));

    List<UnencodedVideo> videos =
        List.of(
            factory.newUnencodedVideo(inputDirectory.resolve("NestedFolder/video1.mp4")),
            factory.newUnencodedVideo(inputDirectory.resolve("video2.mp4")),
            factory.newUnencodedVideo(inputDirectory.resolve("video3.mp4")));

    // When
    boolean result = jobQueue.process(videos);

    // Then
    assertThat(result).isFalse();
    for (UnencodedVideo video : videos) {
      verify(mockVideoEncoder).encode(same(video));
      verify(mockVideoArchiver).archive(same(video));
    }
  }

  @Test
  void encodesInOrder() {
    // Given
    whenVideoEncoderReturns(true);
    when(mockVideoArchiver.archive(any())).thenReturn(true);

    List<UnencodedVideo> videos =
        IntStream.rangeClosed(1, 1000)
            .mapToObj(
                i ->
                    factory.newUnencodedVideo(inputDirectory.resolve("video%03d.mp4".formatted(i))))
            .toList();

    // When
    boolean result = jobQueue.process(videos);

    // Then
    assertThat(result).isTrue();

    InOrder inOrder = inOrder(mockVideoEncoder);
    for (UnencodedVideo video : videos) {
      inOrder.verify(mockVideoEncoder).encode(same(video));
    }
  }

  private void whenVideoEncoderReturns(boolean... results) {
    // simulate the locking behaviour of VideoEncoder
    // TODO kinda ugly (leaky abstraction), but best tradeoff?
    Lock lock = new ReentrantLock();
    doAnswer(
            invocation -> {
              lock.lock();
              return null;
            })
        .when(mockVideoEncoder)
        .acquire();

    when(mockVideoEncoder.encode(any()))
        .then(
            new Answer<Boolean>() {
              final AtomicInteger i = new AtomicInteger();

              @Override
              public Boolean answer(InvocationOnMock invocation) {
                lock.unlock();
                return results[i.getAndIncrement() % results.length];
              }
            });
  }
}
