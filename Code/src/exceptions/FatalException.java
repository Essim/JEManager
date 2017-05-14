package exceptions;

/**
 * Do not try to cast me Except maybe in AppServlet.
 */
public class FatalException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public FatalException() {
    super();
  }

  public FatalException(String message) {
    super(message);
  }

  public FatalException(Exception exc) {
    super(exc);
  }
}
