package fr.epita.distresssignal.infrastructure;

import fr.epita.distresssignal.domain.Packet;

public final class PacketParser {
  public Packet parse(String line) {
    if (line == null || line.isEmpty()) {
      throw new InvalidPacketException("Packet line cannot be null or empty");
    }


  }
}
