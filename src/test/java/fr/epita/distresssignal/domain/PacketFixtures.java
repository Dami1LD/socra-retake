package fr.epita.distresssignal.domain;

import java.util.Arrays;
import java.util.List;

public class PacketFixtures {
    private PacketFixtures() {
    }

    static IntegerPacket integer(int value) {
        return new IntegerPacket(value);
    }

    static ListPacket listOf(Packet... elements) {
        return new ListPacket(List.of(elements));
    }

    static ListPacket integers(int... values) {
        List<Packet> elements = Arrays.stream(values)
                .<Packet>mapToObj(IntegerPacket::new)
                .toList();
        return new ListPacket(elements);
    }
}
