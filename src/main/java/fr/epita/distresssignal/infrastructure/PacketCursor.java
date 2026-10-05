package fr.epita.distresssignal.infrastructure;

import fr.epita.distresssignal.domain.IntegerPacket;
import fr.epita.distresssignal.domain.ListPacket;
import fr.epita.distresssignal.domain.Packet;

import java.util.ArrayList;
import java.util.List;

final public class PacketCursor {
    private static final char LIST_START = '[';
    private static final char LIST_END = ']';
    private static final char SEPARATOR = ',';

    private final String text;
    private int position = 0;

    PacketCursor(String text) {
        this.text = text;
    }

    ListPacket readList() {
        expect(LIST_START);
        List<Packet> elements = new ArrayList<>();
        if (peek() == LIST_END) {
            position++;
            return new ListPacket(elements);
        }
        elements.add(readValue());
        while (peek() == SEPARATOR) {
            position++;
            elements.add(readValue());
        }
        expect(LIST_END);
        return new ListPacket(elements);
    }

    void expectEnd() {
        if (hasMore()) {
            throw error("unexpected characters after the end of the packet");
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

    // Character.isDigit would also accept non-ASCII digits such as Arabic-Indic numerals.
    private static boolean isAsciiDigit(char character) {
        return character >= '0' && character <= '9';
    }

    private InvalidPacketException error(String reason) {
        return new InvalidPacketException(reason + " at position " + position + " in \"" + text + "\"");
    }
}
