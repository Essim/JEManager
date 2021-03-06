package persistence.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import biz.Dto;
import biz.User;
import biz.UserFactory;
import exceptions.FatalException;
import persistence.DalBackEndServices;
import util.Util;

public class UserDaoImpl implements UserDao {

  private DalBackEndServices dalBackendService;
  private UserFactory userFactory;

  public UserDaoImpl(DalBackEndServices dalServices, UserFactory userFactory) {
    this.dalBackendService = dalServices;
    this.userFactory = userFactory;
  }

  /**
   * {@inheritDoc}.
   */
  public User getUserByPseudo(String pseudo) { // can be an UserDto
    User toReturn = null;
    try (PreparedStatement prepare = dalBackendService.prepareStatement("query.authentifyUser")) {
      prepare.setString(1, pseudo);
      try (ResultSet result = prepare.executeQuery()) {
        while (result.next()) {
          toReturn = (User) userFactory.getUser();
          toReturn.setFirstName(result.getString(3));
          toReturn.setUserId(result.getInt(1));
          toReturn.setUsername(result.getString(2));
          toReturn.setRegistrationDate(Util.stringToLocalDateTime(result.getString(6)));
          toReturn.setEmail(result.getString(5));
          toReturn.setLastName(result.getString(4));
          toReturn.setPassword(result.getString(7));
          toReturn.setManager(result.getBoolean(8));
        }
      } catch (SQLException sqle) {
        throw new FatalException(sqle);
      }
    } catch (SQLException sqle2) {
      throw new FatalException(sqle2);
    }
    return toReturn;
  }

  @Override
  public User getUserById(int id) {
    User toReturn = null;
    try (PreparedStatement prepare = dalBackendService.prepareStatement("query.getUserById")) {
      prepare.setInt(1, id);
      try (ResultSet result = prepare.executeQuery()) {
        while (result.next()) {
          toReturn = (User) userFactory.getUser();
          toReturn.setUserId(result.getInt(1));
          toReturn.setUsername(result.getString(2));
          toReturn.setFirstName(result.getString(3));
          toReturn.setLastName(result.getString(4));
          toReturn.setEmail(result.getString(5));
          toReturn.setRegistrationDate(Util.stringToLocalDateTime(result.getString(6)));
          toReturn.setPassword(result.getString(7));
          toReturn.setManager(result.getBoolean(8));
        }
      } catch (SQLException sqle) {
        throw new FatalException(sqle);
      }
    } catch (SQLException sqle2) {
      throw new FatalException(sqle2);
    }
    return toReturn;
  }

  @Override
  public int update(Dto dto) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int insert(Dto dto) {
    User user = (User) dto;
    int id = -1;
    try (PreparedStatement prepare = dalBackendService.prepareStatement("query.registerUser")) {
      prepare.setString(1, user.getFirstName());
      prepare.setString(2, user.getLastName());
      prepare.setString(3, user.getUsername());
      prepare.setString(4, user.getPassword());
      prepare.setString(5, user.getEmail());
      prepare.setString(6, Util.localDateTimeToString(user.getRegistrationDate()));
      prepare.setBoolean(7, user.isManager());
      try (ResultSet result = prepare.executeQuery()) {
        while (result.next()) {
          id = result.getInt(1);
          return id;
        }
      } catch (SQLException sqle) {
        throw new FatalException(sqle);
      }
    } catch (SQLException sqle2) {
      throw new FatalException(sqle2);
    }
    return -1;
  }

  @Override
  public int delete(Dto dto) {
    throw new UnsupportedOperationException();
  }
}
