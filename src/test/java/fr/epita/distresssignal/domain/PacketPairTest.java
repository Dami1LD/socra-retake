package fr.epita.distresssignal.domain;

import static fr.epita.distresssignal.domain.PacketFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PacketPairTest {
  /** The eight pairs of the puzzle example, with their expected verdict. */
  static Stream<Arguments> examplePairs() {
    return Stream.of(
        Arguments.of(
            "pair 1", new PacketPair(integers(1, 1, 3, 1, 1), integers(1, 1, 5, 1, 1)), true),
        Arguments.of(
            "pair 2",
            new PacketPair(listOf(integers(1), integers(2, 3, 4)), listOf(integers(1), integer(4))),
            true),
        Arguments.of("pair 3", new PacketPair(integers(9), listOf(integers(8, 7, 6))), false),
        Arguments.of(
            "pair 4",
            new PacketPair(
                listOf(integers(4, 4), integer(4), integer(4)),
                listOf(integers(4, 4), integer(4), integer(4), integer(4))),
            true),
        Arguments.of("pair 5", new PacketPair(integers(7, 7, 7, 7), integers(7, 7, 7)), false),
        Arguments.of("pair 6", new PacketPair(listOf(), integers(3)), true),
        Arguments.of("pair 7", new PacketPair(listOf(listOf(listOf())), listOf(listOf())), false),
        Arguments.of("pair 8", new PacketPair(deeplyNested(7), deeplyNested(0)), false));
  }

  /** Builds [1,[2,[3,[4,[5,6,lastValue]]]],8,9]. */
  private static ListPacket deeplyNested(int lastValue) {
    return listOf(
        integer(1),
        listOf(integer(2), listOf(integer(3), listOf(integer(4), integers(5, 6, lastValue)))),
        integer(8),
        integer(9));
  }

  @ParameterizedTest(name = "{0} is in right order: {2}")
  @MethodSource("examplePairs")
  void detectsWhetherExamplePairIsInRightOrder(String label, PacketPair pair, boolean expected) {
    assertEquals(expected, pair.isInOrder());
  }

  // Collections.sort relies on this contract for part two.
  @ParameterizedTest(name = "{0}")
  @MethodSource("examplePairs")
  void swappingSidesInvertsTheComparison(String label, PacketPair pair, boolean ignored) {
    int leftFirst = Integer.signum(pair.left().compareTo(pair.right()));
    int rightFirst = Integer.signum(pair.right().compareTo(pair.left()));

    assertEquals(leftFirst, -rightFirst);
  }

  @Test
  void identicalPacketsAreInRightOrder() {
    assertTrue(new PacketPair(integers(1, 2), integers(1, 2)).isInOrder());
  }
}
