package fr.epita.distresssignal.application;

import fr.epita.distresssignal.domain.PacketPair;
import java.util.List;

public interface PacketPairSource {
  List<PacketPair> loadPairs();
}
