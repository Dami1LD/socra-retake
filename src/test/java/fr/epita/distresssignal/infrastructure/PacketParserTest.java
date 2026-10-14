package fr.epita.distresssignal.infrastructure;

import static fr.epita.distresssignal.infrastructure.PacketFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.epita.distresssignal.domain.Packet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class PacketParserTest {
  private final PacketParser parser = new PacketParser();

  @Nested
  class ValidPackets {

    @Test
    void parsesEmptyList() {
      String line = "[]";

      Packet packet = parser.parse(line);

      assertEquals(listOf(), packet);
    }

    @Test
    void parsesListOfSingleInteger() {
      String line = "[5]";

      Packet packet = parser.parse(line);

      assertEquals(integers(5), packet);
    }

    @Test
    void readsMultiDigitIntegersAsOneValue() {
      String line = "[10,2]";

      Packet packet = parser.parse(line);

      assertEquals(integers(10, 2), packet);
    }

    @Test
    void parsesNestedLists() {
      String line = "[1,[2,[3]]]";

      Packet packet = parser.parse(line);

      assertEquals(listOf(integer(1), listOf(integer(2), integers(3))), packet);
    }

    @Test
    void parsesNestedEmptyLists() {
      String line = "[[[]]]";

      Packet packet = parser.parse(line);

      assertEquals(listOf(listOf(listOf())), packet);
    }

    @Test
    void parsesDeepestPacketOfTheExample() {
      String line = "[1,[2,[3,[4,[5,6,7]]]],8,9]";
      Packet expected =
          listOf(
              integer(1),
              listOf(integer(2), listOf(integer(3), listOf(integer(4), integers(5, 6, 7)))),
              integer(8),
              integer(9));

      Packet packet = parser.parse(line);

      assertEquals(expected, packet);
    }

    @Test
    void ignoresWhitespaceAroundThePacket() {
      String line = "  [1]\t";

      Packet packet = parser.parse(line);

      assertEquals(integers(1), packet);
    }
  }

  @Nested
  class InvalidPackets {

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"   ", "\t"})
    void rejectsMissingContent(String line) {

      InvalidPacketException exception = parseExpectingFailure(line);

      assertEquals("Packet line cannot be empty", exception.getMessage());
    }

    @ParameterizedTest(name = "\"{0}\" is rejected because: {1}")
    @CsvSource(
        delimiter = '|',
        quoteCharacter = '"',
        value = {
          "[1,2         | unexpected end of packet",
          "5            | expected '[' but found '5'",
          "abc          | expected '[' but found 'a'",
          "[1,]         | expected a digit or '[' but found ']'",
          "[,1]         | expected a digit or '[' but found ','",
          "[1 2]        | expected ']' but found ' '",
          "[-1]         | expected a digit or '[' but found '-'",
          "[1]]         | unexpected characters after the end of the packet",
          "[99999999999]| Integer too large"
        })
    void rejectsMalformedPacketWithExplicitReason(String line, String expectedReason) {
      InvalidPacketException exception = parseExpectingFailure(line);

      assertTrue(
          exception.getMessage().contains(expectedReason),
          () -> "Unexpected message: " + exception.getMessage());
    }

    @Test
    void errorMessageLocatesTheProblem() {
      String line = "[1,2";

      InvalidPacketException exception = parseExpectingFailure(line);

      assertTrue(
          exception.getMessage().contains("at position 4 in \"[1,2\""),
          () -> "Unexpected message: " + exception.getMessage());
    }

    @Test
    void keepsTheOriginalCauseOfAnOverflow() {
      String line = "[99999999999]";

      InvalidPacketException exception = parseExpectingFailure(line);

      assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    private InvalidPacketException parseExpectingFailure(String line) {
      return assertThrows(InvalidPacketException.class, () -> parser.parse(line));
    }
  }
}
