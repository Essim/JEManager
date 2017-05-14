package biz;

/**
 * The methods in this class return DTO instances implemented either by a Real or Mock class
 * depending on which factory (MockFactory or RealFactory) is used. The dependency injection (choice
 * of factory) should be done in the main method of the application using configuration files in the
 * "config" package.
 * 
 * @author Maniet.Alexandre
 */
public interface UserFactory {

  /**
   * @author Maniet.Alexandre
   * @return Returns a new instance of UserDTO.
   */
  public UserDto getUser();
}
