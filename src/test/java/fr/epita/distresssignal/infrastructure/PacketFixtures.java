package fr.epita.distresssignal.infrastructure;

import fr.epita.distresssignal.domain.IntegerPacket;
import fr.epita.distresssignal.domain.ListPacket;
import fr.epita.distresssignal.domain.Packet;
import java.util.Arrays;
import java.util.List;

public class PacketFixtures {
  private PacketFixtures() {}

  public static IntegerPacket integer(int value) {
    return new IntegerPacket(value);
  }

  public static ListPacket listOf(Packet... elements) {
    return new ListPacket(List.of(elements));
  }

  public static ListPacket integers(int... values) {
    List<Packet> elements = Arrays.stream(values).<Packet>mapToObj(IntegerPacket::new).toList();
    return new ListPacket(elements);
  }
}
