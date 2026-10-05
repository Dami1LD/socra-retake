package fr.epita.distresssignal.domain;

public record PacketPair(Packet left, Packet right) {
    public boolean isInOrder() {
        return left.compareTo(right) <= 0;
    }
}
