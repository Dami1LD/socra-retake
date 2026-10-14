package fr.epita.distresssignal.domain;

import java.util.List;

public record ListPacket(List<Packet> elements) implements Packet {
  @Override
  public int compareTo(Packet other) {
    return switch (other) {
      case ListPacket otherList -> compareElements(otherList);
      case IntegerPacket otherInteger -> compareTo(otherInteger.toListPacket());
    };
  }

  private int compareElements(ListPacket other) {
    int sharedSize = Math.min(elements.size(), other.elements.size());
    for (int i = 0; i < sharedSize; i++) {
      int comparison = elements.get(i).compareTo(other.elements.get(i));
      if (comparison != 0) {
        return comparison;
      }
    }
    return Integer.compare(elements.size(), other.elements.size());
  }
}
