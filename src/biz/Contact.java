package biz;

import exceptions.InvalidInformationException;

/**
 * This is the Contact class's interface as seen from a UCC. Once a UCC has been given a DTO, he may
 * cast said object into this type in order to access all business-related methods in addition to
 * the DTO's accessors.
 * 
 * @author Antoine.Maniet
 */
public interface Contact extends ContactDto, OptimisticLock {

  /**
   * Checks that the id, company, firstName, lastName, email+phoneNumber are not null or 0/"".
   * Checks that the email matches "".+@.+\\.[a-z]+"" if he isn't empty. Checks that the
   * phoneNumber's length is under 15 digits.
   * 
   * @throws InvalidInformationException if the information aren't valid.
   */
  void checkNewContactInformation() throws InvalidInformationException;

  /**
   * Checks that the id, firstName, lastName, email+phoneNumber are not null or 0/"". Checks that
   * the email matches "".+@.+\\.[a-z]+"" if he isn't empty. Checks that the phoneNumber's length is
   * under 15 digits.
   * 
   * @throws InvalidInformationException if the information aren't valid.
   */
  void checkModifyContactInformation() throws InvalidInformationException;
}
