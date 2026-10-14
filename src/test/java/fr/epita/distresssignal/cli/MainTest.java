package fr.epita.distresssignal.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
  @TempDir Path tempDir;

  private ByteArrayOutputStream outBuffer;
  private ByteArrayOutputStream errBuffer;
  private PrintStream out;
  private PrintStream err;

  @BeforeEach
  void setUp() {
    outBuffer = new ByteArrayOutputStream();
    errBuffer = new ByteArrayOutputStream();
    out = new PrintStream(outBuffer, true);
    err = new PrintStream(errBuffer, true);
  }

  private Path inputFile(String content) throws IOException {
    return Files.writeString(tempDir.resolve("input.txt"), content);
  }

  @Test
  void run_printsBothPartsAndReturnsZero_whenFileIsValid() throws IOException {
    Path file = inputFile("[1]\n[2]\n\n[3]\n[2]\n\n[1]\n[1]\n");

    int exitCode = Main.run(new String[] {file.toString()}, out, err);

    assertEquals(Main.EXIT_OK, exitCode);
    // part 1: pairs 1 and 3 in order; part 2: [[2]] at 6, [[6]] at 8 after sorting
    assertEquals(
        "4" + System.lineSeparator() + "48" + System.lineSeparator(), outBuffer.toString());
    assertEquals("", errBuffer.toString());
  }

  @Test
  void run_printsErrorAndReturnsNonZero_whenFileDoesNotExist() {
    String missing = tempDir.resolve("missing.txt").toString();

    int exitCode = Main.run(new String[] {missing}, out, err);

    assertEquals(Main.EXIT_ERROR, exitCode);
    assertEquals("", outBuffer.toString());
    assertTrue(errBuffer.toString().startsWith("Error: "));
  }

  @Test
  void run_printsErrorAndReturnsNonZero_whenPacketIsInvalid() throws IOException {
    Path file = inputFile("[1\n[2]\n");

    int exitCode = Main.run(new String[] {file.toString()}, out, err);

    assertEquals(Main.EXIT_ERROR, exitCode);
    assertTrue(errBuffer.toString().contains("Pair 1 contains an invalid packet"));
  }

  @Test
  void run_printsUsageAndReturnsNonZero_whenArgumentIsMissing() {
    int exitCode = Main.run(new String[] {}, out, err);

    assertEquals(Main.EXIT_ERROR, exitCode);
    assertTrue(errBuffer.toString().startsWith("Usage:"));
  }
}
