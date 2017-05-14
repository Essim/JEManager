package exceptions;

public class WrongRequestException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public WrongRequestException() {
    super();
  }

  public WrongRequestException(String message) {
    super(message);
  }

  public WrongRequestException(Exception exception) {
    super(exception);
  }
}
