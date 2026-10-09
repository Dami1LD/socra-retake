package fr.epita.distresssignal.application;

import fr.epita.distresssignal.domain.PacketPair;
import java.util.List;
import java.util.stream.IntStream;

public final class SumOfOrderedIndicesUseCase {
  private final PacketPairSourcePort source;

  public SumOfOrderedIndicesUseCase(PacketPairSourcePort source) {
    this.source = source;
  }

  public int execute() {
    List<PacketPair> pairs = source.loadPairs();
    return IntStream.rangeClosed(1, pairs.size())
        .filter(index -> pairs.get(index - 1).isInOrder())
        .sum();
  }
}
