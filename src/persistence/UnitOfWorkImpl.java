package persistence;

import java.util.HashMap;
import java.util.Map;

import biz.Dto;
import exceptions.OptimisticException;
import ihm.PluginProperties;
import persistence.dao.Dao;

public class UnitOfWorkImpl implements UnitOfWork {

  ThreadLocal<UnitOfWorkBag> bags;
  ThreadLocal<Integer> idsReturned;
  DalServices dal;
  PluginProperties props;
  int semaphore;

  /**
   * Instanciate UnitOfWork objects.
   * 
   * @author Sacre.Christopher
   * @param dal an instance of DalServices.
   * @param props an instance of PluginProperties.
   */
  public UnitOfWorkImpl(DalServices dal, PluginProperties props) {
    bags = new ThreadLocal<>();
    idsReturned = new ThreadLocal<>();
    this.dal = dal;
    this.props = props;
    this.semaphore = 0;
  }

  /**
   * (non-Javadoc).
   * 
   * @see persistence.UnitOfWork#startTransaction()
   */
  public void startTransaction() {
    bags.set(new UnitOfWorkBag());
    semaphore++;
  }

  /**
   * (non-Javadoc).
   * 
   * @see persistence.UnitOfWork#rollBack()
   */
  public void rollBack() {
    semaphore--;
  }

  /**
   * (non-Javadoc).
   * 
   * @see persistence.UnitOfWork#addUpdate(biz.Dto)
   */
  public void addUpdate(Dto dto) {
    bags.get().mapUpdate.put(dto.getId(), dto);
  }

  /**
   * (non-Javadoc).
   * 
   * @see persistence.UnitOfWork#addInsert(biz.Dto)
   */
  public void addInsert(Dto dto) {
    bags.get().mapInsert.put(dto.getId(), dto);
  }

  /**
   * (non-Javadoc).
   * 
   * @see persistence.UnitOfWork#addDelete(biz.Dto)
   */
  public void addDelete(Dto dto) {
    bags.get().mapDelete.put(dto.getId(), dto);
  }


  /**
   * (non-Javadoc).
   * 
   * @see persistence.UnitOfWork#commit()
   */
  public void commit() {
    try {
      --semaphore;
      if (semaphore != 0) {
        return;
      }
      dal.startTransaction();
      for (String s : this.bags.get().mapInsert.keySet()) {
        Dto dto = this.bags.get().mapInsert.get(s);
        Dao dao = getDao(dto);
        idsReturned.set(dao.insert(dto));
      }
      for (String s : this.bags.get().mapUpdate.keySet()) {
        Dto dto = this.bags.get().mapUpdate.get(s);
        Dao dao = getDao(dto);
        idsReturned.set(dao.update(dto));
      }
      for (String s : this.bags.get().mapDelete.keySet()) {
        Dto dto = this.bags.get().mapDelete.get(s);
        Dao dao = getDao(dto);
        idsReturned.set(dao.delete(dto));
      }
    } catch (OptimisticException excep) {
      dal.rollback();
      throw new OptimisticException();
    }
    dal.commit();
  }

  /**
   * (non-Javadoc).
   * 
   * @see persistence.UnitOfWork#getResult()
   */
  public int getResult() {
    if (idsReturned.get() != null) {
      return idsReturned.get();
    }
    return -1;
  }

  /**
   * @param dto a dto.
   * @return a dao.
   */
  private Dao getDao(Dto dto) {
    try {
      return (Dao) props
          .getPluginFor(Class.forName(props.getProperty(dto.getClass().getSimpleName())));
    } catch (ClassNotFoundException cnfe) {
      cnfe.printStackTrace();
      return null;
    }
  }

  private static class UnitOfWorkBag {

    private Map<String, Dto> mapUpdate;
    private Map<String, Dto> mapInsert;
    private Map<String, Dto> mapDelete;

    public UnitOfWorkBag() {
      mapUpdate = new HashMap<String, Dto>();
      mapInsert = new HashMap<String, Dto>();
      mapDelete = new HashMap<String, Dto>();
    }

  }

}
