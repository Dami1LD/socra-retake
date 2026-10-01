package fr.epita.distresssignal.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fr.epita.distresssignal.domain.PacketFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListPacketTest {
    @Test
    void twoEmptyListsAreUndecided() {
        assertEquals(0, listOf().compareTo(listOf()));
    }

    @Test
    void emptyListComesBeforeNonEmptyList() {
        assertEquals(-1, Integer.signum(listOf().compareTo(integers(3))));
    }

    @Test
    void identicalListsAreUndecided() {
        assertEquals(0, integers(1, 2, 3).compareTo(integers(1, 2, 3)));
    }

    @Test
    void firstDifferentElementDecidesEvenIfLaterElementsDisagree() {
        assertEquals(-1, Integer.signum(integers(1, 2, 9).compareTo(integers(1, 3, 0))));
    }

    @Test
    void leftRunningOutFirstComesFirst() {
        assertEquals(-1, Integer.signum(integers(7, 7, 7).compareTo(integers(7, 7, 7, 7))));
    }

    @Test
    void rightRunningOutFirstComesLast() {
        assertEquals(1, Integer.signum(integers(7, 7, 7, 7).compareTo(integers(7, 7, 7))));
    }

    @Test
    void deeperEmptyNestingComesLast() {
        ListPacket threeLevels = listOf(listOf(listOf()));
        ListPacket twoLevels = listOf(listOf());

        assertEquals(1, Integer.signum(threeLevels.compareTo(twoLevels)));
    }

    @Test
    void listAgainstIntegerWrapsTheInteger() {
        assertEquals(1, Integer.signum(integers(9).compareTo(integer(8))));
    }
}
