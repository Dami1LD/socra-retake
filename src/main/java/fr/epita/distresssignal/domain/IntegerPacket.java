package fr.epita.distresssignal.domain;

import java.util.List;

public record IntegerPacket(int value) implements Packet {
  public ListPacket toListPacket() {
    return new ListPacket(List.of(this));
  }

  public int compareTo(Packet other) {
    if (other instanceof IntegerPacket otherInteger) {
      return Integer.compare(this.value, otherInteger.value);
    } else if (other instanceof ListPacket otherList) {
      return this.toListPacket().compareTo(otherList);
    } else {
      throw new IllegalArgumentException("Unknown packet type: " + other.getClass());
    }
  }
}
