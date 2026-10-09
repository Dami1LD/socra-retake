package fr.epita.distresssignal.infrastructure;

public class PacketSourceException extends RuntimeException {
  public PacketSourceException(String message) {
    super(message);
  }

  public PacketSourceException(String message, Throwable cause) {
    super(message, cause);
  }
}
