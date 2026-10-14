package fr.epita.distresssignal.infrastructure;

import fr.epita.distresssignal.domain.IntegerPacket;
import fr.epita.distresssignal.domain.ListPacket;
import fr.epita.distresssignal.domain.Packet;
import java.util.ArrayList;
import java.util.List;

public final class PacketParser {
  private static final char LIST_START = '[';
  private static final char LIST_END = ']';
  private static final char SEPARATOR = ',';
  private static final int MAX_NESTING_DEPTH = 500;

  public Packet parse(String line) {
    if (line.isBlank()) {
      throw new InvalidPacketException("Packet line cannot be empty");
    }
    Reading reading = new Reading(line.strip());
    Packet packet = reading.readList();
    reading.expectEnd();
    return packet;
  }

  /** Mutable position over one line; one instance per parsed packet. */
  private static final class Reading {
    private final String text;
    private int position = 0;
    private int depth = 0;

    Reading(String text) {
      this.text = text;
    }

    ListPacket readList() {
      enterList();
      List<Packet> elements = new ArrayList<>();
      if (peek() == LIST_END) {
        position++;
      } else {
        elements.add(readValue());
        while (peek() == SEPARATOR) {
          position++;
          elements.add(readValue());
        }
        expect(LIST_END);
      }
      depth--;
      return new ListPacket(elements);
    }

    void expectEnd() {
      if (hasMore()) {
        throw error("unexpected characters after the end of the packet");
      }
    }

    private void enterList() {
      expect(LIST_START);
      depth++;
      if (depth > MAX_NESTING_DEPTH) {
        throw error("nesting deeper than " + MAX_NESTING_DEPTH + " levels");
      }
    }

    private Packet readValue() {
      char current = peek();
      if (current == LIST_START) {
        return readList();
      }
      if (isAsciiDigit(current)) {
        return readInteger();
      }
      throw error("expected a digit or '" + LIST_START + "' but found '" + current + "'");
    }

    private IntegerPacket readInteger() {
      int start = position;
      while (hasMore() && isAsciiDigit(text.charAt(position))) {
        position++;
      }
      String digits = text.substring(start, position);
      try {
        return new IntegerPacket(Integer.parseInt(digits));
      } catch (NumberFormatException overflow) {
        throw new InvalidPacketException("Integer too large: " + digits, overflow);
      }
    }

    private void expect(char expected) {
      char current = peek();
      if (current != expected) {
        throw error("expected '" + expected + "' but found '" + current + "'");
      }
      position++;
    }

    private char peek() {
      if (!hasMore()) {
        throw error("unexpected end of packet");
      }
      return text.charAt(position);
    }

    private boolean hasMore() {
      return position < text.length();
    }

    private static boolean isAsciiDigit(char character) {
      return character >= '0' && character <= '9';
    }

    private InvalidPacketException error(String reason) {
      return new InvalidPacketException(
          reason + " at position " + position + " in \"" + text + "\"");
    }
  }
}
