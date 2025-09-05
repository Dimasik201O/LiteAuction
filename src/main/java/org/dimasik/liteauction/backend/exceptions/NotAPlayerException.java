package org.dimasik.liteauction.backend.exceptions;

public class NotAPlayerException extends RuntimeException {
  public NotAPlayerException() {
    super("Player must be sender to execute this action");
  }

  public NotAPlayerException(String message) {
    super(message);
  }
}
