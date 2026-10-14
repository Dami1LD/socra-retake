package fr.epita.distresssignal.domain;

public sealed interface Packet extends Comparable<Packet> permits IntegerPacket, ListPacket {}
