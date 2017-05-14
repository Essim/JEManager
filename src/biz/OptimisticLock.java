package biz;

/**
 * Interface for the needed method to implement the optimistic locking.
 * 
 * @author sam.ndagano
 */
public interface OptimisticLock {

  /**
   * Update the entity's version for optimistic lock implementation.
   * 
   * @param version updated entity's version
   */
  void setVersion(int version);

  /**
   * Provides the current version of the entity.
   * 
   * @return current version.
   */
  int getVersion();
}
