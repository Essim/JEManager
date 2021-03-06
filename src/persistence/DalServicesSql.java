package persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.jdbc2.optional.PoolingDataSource;

import exceptions.FatalException;
import ihm.PluginProperties;

class DalServicesSql implements DalServices, DalBackEndServices {

  private static ThreadLocal<Connection> connections;
  private PoolingDataSource bds;
  private PluginProperties properties;

  public DalServicesSql(PluginProperties properties) {
    this.properties = properties;
    bds = new PoolingDataSource();
    try {
      bds.setUrl(properties.getProperty("url"));
    } catch (SQLException sqle) {
      throw new FatalException();
    }
    bds.setUser(properties.getProperty("user"));
    bds.setPassword(properties.getProperty("passwd"));
    bds.setMaxConnections(Integer.parseInt(properties.getProperty("poolSize")));
    bds.setInitialConnections(Integer.parseInt(properties.getProperty("poolSize")));
    connections = new ThreadLocal<Connection>();
  }

  /*
   * private static class DalServicesSqlHolder { private final static DalServicesSql INSTANCE = new
   * DalServicesSql(); }
   * 
   * public static DalServicesSql getInstance() { return DalServicesSqlHolder.INSTANCE; }
   */
  private final Connection connect() {
    Connection connection = null;
    try {
      connection = bds.getConnection();
    } catch (SQLException sqe) {
      throw new FatalException(sqe);
    }
    connections.set(connection);
    return connection;
  }

  private final void disconnect() {
    try {
      connections.get().close();
    } catch (SQLException sqe) {
      sqe.printStackTrace();
      throw new FatalException(sqe);
    }
  }

  /**
   * {@inheritDoc}.
   * 
   * @return the preparedStatement associated to the connection, or null if an error occured during
   *         the creation of the preparedStatement
   */
  public PreparedStatement prepareStatement(String sql) {
    String string = properties.getProperty(sql);
    try {
      return connections.get().prepareStatement(string);
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    }
    return null;
  }

  /**
   * {@inheritDoc}.
   */
  public void commit() {
    try {
      Connection connection = connections.get();
      connection.commit();
      connection.setAutoCommit(true);
      disconnect();
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}.
   */
  public void rollback() {
    try {
      Connection connection = connections.get();
      connection.rollback();
      connection.setAutoCommit(true);
      disconnect();
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}.
   */
  public void startTransaction() {
    try {
      Connection connection = connect();
      connection.setAutoCommit(false);
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }
}
