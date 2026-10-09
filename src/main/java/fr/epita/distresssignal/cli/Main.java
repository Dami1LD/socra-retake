package fr.epita.distresssignal.cli;

import fr.epita.distresssignal.application.SumOfOrderedIndicesUseCase;
import fr.epita.distresssignal.infrastructure.FilePacketPairSourceAdapter;
import fr.epita.distresssignal.infrastructure.PacketParser;
import fr.epita.distresssignal.infrastructure.PacketSourceException;
import java.io.PrintStream;
import java.nio.file.Path;

public final class Main {
  static final int EXIT_OK = 0;
  static final int EXIT_ERROR = 1;

  private Main() {}

  public static void main(String[] args) {
    System.exit(run(args, System.out, System.err));
  }

  static int run(String[] args, PrintStream out, PrintStream err) {
    if (args.length != 1) {
      err.println("Usage: java -jar distress-signal.jar <input-file>");
      return EXIT_ERROR;
    }
    try {
      FilePacketPairSourceAdapter source =
          new FilePacketPairSourceAdapter(Path.of(args[0]), new PacketParser());
      out.println(new SumOfOrderedIndicesUseCase(source).execute());
      return EXIT_OK;
    } catch (PacketSourceException e) {
      err.println("Error: " + e.getMessage());
      return EXIT_ERROR;
    }
  }
}
