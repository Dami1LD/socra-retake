package fr.epita.distresssignal.domain;

import java.util.List;

public record IntegerPacket(int value) implements Packet {
  /** Puzzle rule: an integer compared with a list is first wrapped into a one-element list. */
  public ListPacket toListPacket() {
    return new ListPacket(List.of(this));
  }

  @Override
  public int compareTo(Packet other) {
    return switch (other) {
      case IntegerPacket otherInteger -> Integer.compare(value, otherInteger.value);
      case ListPacket otherList -> toListPacket().compareTo(otherList);
    };
  }
}
