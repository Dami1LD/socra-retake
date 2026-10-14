package fr.epita.distresssignal.application;

import fr.epita.distresssignal.domain.IntegerPacket;
import fr.epita.distresssignal.domain.ListPacket;
import fr.epita.distresssignal.domain.Packet;
import fr.epita.distresssignal.domain.PacketPair;
import java.util.ArrayList;
import java.util.List;

public final class DecoderKeyUseCase {
  private static final Packet FIRST_DIVIDER = divider(2);
  private static final Packet SECOND_DIVIDER = divider(6);

  private final PacketPairSourcePort source;

  public DecoderKeyUseCase(PacketPairSourcePort source) {
    this.source = source;
  }

  public int execute() {
    List<Packet> packets = new ArrayList<>();
    for (PacketPair pair : source.loadPairs()) {
      packets.add(pair.left());
      packets.add(pair.right());
    }
    packets.add(FIRST_DIVIDER);
    packets.add(SECOND_DIVIDER);
    packets.sort(Packet::compareTo);
    return positionOf(packets, FIRST_DIVIDER) * positionOf(packets, SECOND_DIVIDER);
  }

  private static int positionOf(List<Packet> sorted, Packet divider) {
    for (int i = 0; i < sorted.size(); i++) {
      if (sorted.get(i) == divider) {
        return i + 1;
      }
    }
    throw new IllegalStateException("Divider missing from sorted packets");
  }

  private static Packet divider(int value) {
    return new ListPacket(List.of(new ListPacket(List.of(new IntegerPacket(value)))));
  }
}
