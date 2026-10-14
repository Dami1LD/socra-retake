package fr.epita.distresssignal.infrastructure;

import static fr.epita.distresssignal.infrastructure.PacketFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.epita.distresssignal.domain.PacketPair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilePacketPairSourceAdapterTest {
  @TempDir Path tempDir;

  private FilePacketPairSourceAdapter sourceOf(String content) throws IOException {
    Path file = Files.writeString(tempDir.resolve("input.txt"), content);
    return new FilePacketPairSourceAdapter(file, new PacketParser());
  }

  @Test
  void loadPairs_returnsOnePair_whenFileHasOneBlock() throws IOException {
    FilePacketPairSourceAdapter source = sourceOf("[1,2]\n[3]\n");

    List<PacketPair> pairs = source.loadPairs();

    assertEquals(List.of(new PacketPair(integers(1, 2), integers(3))), pairs);
  }

  @Test
  void loadPairs_returnsPairsInFileOrder_whenBlocksAreSeparatedByBlankLine() throws IOException {
    FilePacketPairSourceAdapter source = sourceOf("[1]\n[2]\n\n[[3]]\n[4]\n");

    List<PacketPair> pairs = source.loadPairs();

    assertEquals(
        List.of(
            new PacketPair(integers(1), integers(2)),
            new PacketPair(listOf(integers(3)), integers(4))),
        pairs);
  }

  @Test
  void loadPairs_ignoresExtraBlankLines_whenSeparatorsAreRepeatedOrTrailing() throws IOException {
    FilePacketPairSourceAdapter source = sourceOf("\n[1]\n[2]\n\n\n\n[3]\n[4]\n\n\n");

    List<PacketPair> pairs = source.loadPairs();

    assertEquals(2, pairs.size());
  }

  @Test
  void loadPairs_throws_whenFileIsEmpty() throws IOException {
    FilePacketPairSourceAdapter source = sourceOf("");

    PacketSourceException exception = assertThrows(PacketSourceException.class, source::loadPairs);

    assertTrue(exception.getMessage().startsWith("No packet pairs found"));
  }

  @Test
  void loadPairs_throws_whenFileDoesNotExist() {
    Path missing = tempDir.resolve("missing.txt");
    FilePacketPairSourceAdapter source =
        new FilePacketPairSourceAdapter(missing, new PacketParser());

    PacketSourceException exception = assertThrows(PacketSourceException.class, source::loadPairs);

    assertAll(
        () -> assertTrue(exception.getMessage().contains(missing.toString())),
        () -> assertInstanceOf(IOException.class, exception.getCause()));
  }

  @Test
  void loadPairs_throws_whenBlockHasASingleLine() throws IOException {
    FilePacketPairSourceAdapter source = sourceOf("[1]\n[2]\n\n[3]\n");

    PacketSourceException exception = assertThrows(PacketSourceException.class, source::loadPairs);

    assertEquals("Pair 2 must have 2 lines but has 1", exception.getMessage());
  }

  @Test
  void loadPairs_throws_whenBlockHasMoreThanTwoLines() throws IOException {
    FilePacketPairSourceAdapter source = sourceOf("[1]\n[2]\n[3]\n");

    PacketSourceException exception = assertThrows(PacketSourceException.class, source::loadPairs);

    assertEquals("Pair 1 must have 2 lines but has 3", exception.getMessage());
  }

  @Test
  void loadPairs_throwsWithPairNumberAndCause_whenPacketIsInvalid() throws IOException {
    FilePacketPairSourceAdapter source = sourceOf("[1]\n[2]\n\n[3\n[4]\n");

    PacketSourceException exception = assertThrows(PacketSourceException.class, source::loadPairs);

    assertAll(
        () -> assertTrue(exception.getMessage().startsWith("Pair 2 contains an invalid packet")),
        () -> assertInstanceOf(InvalidPacketException.class, exception.getCause()));
  }
}
