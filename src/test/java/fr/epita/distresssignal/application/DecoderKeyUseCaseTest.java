package fr.epita.distresssignal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.epita.distresssignal.domain.IntegerPacket;
import fr.epita.distresssignal.domain.ListPacket;
import fr.epita.distresssignal.domain.Packet;
import fr.epita.distresssignal.domain.PacketPair;
import java.util.List;
import org.junit.jupiter.api.Test;

class DecoderKeyUseCaseTest {
  private static Packet packetOf(int value) {
    return new ListPacket(List.of(new IntegerPacket(value)));
  }

  private static DecoderKeyUseCase useCaseWith(PacketPair... pairs) {
    return new DecoderKeyUseCase(() -> List.of(pairs));
  }

  @Test
  void execute_returnsTwo_whenThereIsNoPair() {
    DecoderKeyUseCase useCase = useCaseWith();

    int result = useCase.execute();

    assertEquals(2, result);
  }

  @Test
  void execute_returnsProductOfDividerPositions_whenPacketsSurroundDividers() {
    DecoderKeyUseCase useCase =
        useCaseWith(
            new PacketPair(packetOf(7), packetOf(1)), new PacketPair(packetOf(3), packetOf(7)));

    int result = useCase.execute();

    assertEquals(2 * 4, result);
  }

  @Test
  void execute_placesDividersAfterEqualPackets_whenInputContainsDividerCopies() {
    Packet copy = new ListPacket(List.of(packetOf(2)));
    DecoderKeyUseCase useCase = useCaseWith(new PacketPair(copy, copy));

    int result = useCase.execute();

    assertEquals(3 * 4, result);
  }
}
