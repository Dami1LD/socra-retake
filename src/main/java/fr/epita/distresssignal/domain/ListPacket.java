package fr.epita.distresssignal.domain;

import java.util.List;

public record ListPacket(List<Packet> elements) implements Packet {
  public int compareTo(Packet other) {
    if (other instanceof ListPacket otherList) {
      int minSize = Math.min(this.elements.size(), otherList.elements.size());
      for (int i = 0; i < minSize; i++) {
        int comparison = this.elements.get(i).compareTo(otherList.elements.get(i));
        if (comparison != 0) {
          return comparison;
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
