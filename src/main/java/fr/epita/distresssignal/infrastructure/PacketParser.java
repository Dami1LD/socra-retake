package fr.epita.distresssignal.infrastructure;

import fr.epita.distresssignal.domain.Packet;

public final class PacketParser {
  public Packet parse(String line) {
    if (line == null || line.isBlank()) {
      throw new InvalidPacketException("Packet line cannot be null or empty");
    }

    PacketCursor cursor = new PacketCursor(line.strip());
    Packet packet = cursor.readList();
    cursor.expectEnd();
    return packet;
  }
}
