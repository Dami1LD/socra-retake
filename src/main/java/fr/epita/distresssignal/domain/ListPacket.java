package fr.epita.distresssignal.domain;

import java.util.List;

public record ListPacket(List<Packet> elements) implements Packet {
  public int compareTo(Packet other) {
    if (other instanceof ListPacket otherList) {
      int minSize = Math.min(this.elements.size(), otherList.elements.size());
      for (int i = 0; i < minSize; i++) {
        boolean result = this.elements.get(i).compareTo(otherList.elements.get(i)) < 0;
        if (result) {
          return -1;
        } else if (this.elements.get(i).compareTo(otherList.elements.get(i)) > 0) {
          return 1;
        }
      }
      return Integer.compare(this.elements.size(), otherList.elements.size());
    } else if (other instanceof IntegerPacket otherInteger) {
      return this.compareTo(new ListPacket(List.of(otherInteger)));
    } else {
      throw new IllegalArgumentException("Unknown packet type: " + other.getClass());
    }
  }
}
