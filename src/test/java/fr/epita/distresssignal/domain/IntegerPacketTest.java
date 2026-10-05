package fr.epita.distresssignal.domain;

import static fr.epita.distresssignal.domain.PacketFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class IntegerPacketTest {
  @Test
  void lowerValueComesFirst() {
    assertEquals(-1, Integer.signum(integer(3).compareTo(integer(5))));
  }

  @Test
  void higherValueComesLast() {
    assertEquals(1, Integer.signum(integer(5).compareTo(integer(3))));
  }

  @Test
  void sameValuesAreUndecided() {
    assertEquals(0, integer(4).compareTo(integer(4)));
  }

  @Test
  void toListPacketWrapsTheValueInASingleElementList() {
    assertEquals(integers(7), integer(7).toListPacket());
  }

  @Test
  void integerAgainstListWithSameSingleValueIsUndecided() {
    assertEquals(0, integer(3).compareTo(integers(3)));
  }

  @Test
  void integerAgainstLongerListWithSamePrefixComesFirst() {
    assertEquals(-1, Integer.signum(integer(3).compareTo(integers(3, 4))));
  }

  @Test
  void integerAgainstEmptyListComesLast() {
    assertEquals(1, Integer.signum(integer(3).compareTo(listOf())));
  }
}
