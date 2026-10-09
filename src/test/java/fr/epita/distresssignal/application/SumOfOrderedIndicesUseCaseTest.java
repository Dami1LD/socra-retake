package fr.epita.distresssignal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.epita.distresssignal.domain.IntegerPacket;
import fr.epita.distresssignal.domain.ListPacket;
import fr.epita.distresssignal.domain.Packet;
import fr.epita.distresssignal.domain.PacketPair;
import java.util.List;
import org.junit.jupiter.api.Test;

class SumOfOrderedIndicesUseCaseTest {
  private static PacketPair inOrderPair() {
    return pair(1, 2);
  }

  private static PacketPair outOfOrderPair() {
    return pair(2, 1);
  }

  private static PacketPair pair(int left, int right) {
    return new PacketPair(packetOf(left), packetOf(right));
  }

  private static Packet packetOf(int value) {
    return new ListPacket(List.of(new IntegerPacket(value)));
  }

  private static SumOfOrderedIndicesUseCase useCaseWith(PacketPair... pairs) {
    return new SumOfOrderedIndicesUseCase(() -> List.of(pairs));
  }

  @Test
  void execute_returnsZero_whenThereIsNoPair() {
    SumOfOrderedIndicesUseCase useCase = useCaseWith();

    int result = useCase.execute();

    assertEquals(0, result);
  }

  @Test
  void execute_returnsOne_whenOnlyFirstPairIsInOrder() {
    SumOfOrderedIndicesUseCase useCase = useCaseWith(inOrderPair());

    int result = useCase.execute();

    assertEquals(1, result);
  }

  @Test
  void execute_returnsZero_whenNoPairIsInOrder() {
    SumOfOrderedIndicesUseCase useCase = useCaseWith(outOfOrderPair(), outOfOrderPair());

    int result = useCase.execute();

    assertEquals(0, result);
  }

  @Test
  void execute_usesOneBasedIndices_whenOnlySecondPairIsInOrder() {
    SumOfOrderedIndicesUseCase useCase = useCaseWith(outOfOrderPair(), inOrderPair());

    int result = useCase.execute();

    assertEquals(2, result);
  }

  @Test
  void execute_sumsIndicesOfInOrderPairs_whenSeveralPairsAreInOrder() {
    SumOfOrderedIndicesUseCase useCase =
        useCaseWith(
            inOrderPair(), outOfOrderPair(), inOrderPair(), outOfOrderPair(), inOrderPair());

    int result = useCase.execute();

    assertEquals(1 + 3 + 5, result);
  }
}
