package fr.epita.distresssignal.infrastructure;

import fr.epita.distresssignal.application.PacketPairSourcePort;
import fr.epita.distresssignal.domain.PacketPair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FilePacketPairSourceAdapter implements PacketPairSourcePort {
  private static final int LINES_PER_PAIR = 2;

  private final Path file;
  private final PacketParser parser;

  public FilePacketPairSourceAdapter(Path file, PacketParser parser) {
    this.file = file;
    this.parser = parser;
  }

  @Override
  public List<PacketPair> loadPairs() {
    List<PacketPair> pairs = new ArrayList<>();
    for (List<String> block : readBlocks()) {
      pairs.add(toPair(block, pairs.size() + 1));
    }
    if (pairs.isEmpty()) {
      throw new PacketSourceException("No packet pairs found in " + file);
    }
    return pairs;
  }

  private List<List<String>> readBlocks() {
    List<List<String>> blocks = new ArrayList<>();
    List<String> current = new ArrayList<>();
    for (String line : readLines()) {
      if (line.isBlank()) {
        flush(current, blocks);
      } else {
        current.add(line);
      }
    }
    flush(current, blocks);
    return blocks;
  }

  private static void flush(List<String> current, List<List<String>> blocks) {
    if (!current.isEmpty()) {
      blocks.add(List.copyOf(current));
      current.clear();
    }
  }

  private List<String> readLines() {
    try {
      return Files.readAllLines(file);
    } catch (IOException e) {
      throw new PacketSourceException("Cannot read packet file: " + file, e);
    }
  }

  private PacketPair toPair(List<String> block, int pairNumber) {
    if (block.size() != LINES_PER_PAIR) {
      throw new PacketSourceException(
          "Pair %d must have %d lines but has %d"
              .formatted(pairNumber, LINES_PER_PAIR, block.size()));
    }
    try {
      return new PacketPair(parser.parse(block.get(0)), parser.parse(block.get(1)));
    } catch (InvalidPacketException e) {
      throw new PacketSourceException(
          "Pair %d contains an invalid packet: %s".formatted(pairNumber, e.getMessage()), e);
    }
  }
}
