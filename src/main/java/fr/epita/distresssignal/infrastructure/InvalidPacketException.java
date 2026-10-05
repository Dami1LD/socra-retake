package fr.epita.distresssignal.infrastructure;

public class InvalidPacketException extends RuntimeException {
  public InvalidPacketException(String message) {
    super(message);
  }

  public InvalidPacketException(String message, Throwable cause) {
    super(message, cause);
  }
}
