package mockObjects;

import java.util.Collection;

import biz.Company;
import biz.Contact;
import exceptions.InvalidInformationException;
import util.SetValidator;

/**
 * This is the MockContact class. See the MockUser class documentation for more info on how Mock
 * Objects are used.
 * 
 * @author Maniet.Alexandre
 */
public class MockContact extends SetValidator implements Contact {

  private Company company = new MockCompany();

  /**
   * Empty constructor.
   * 
   * @author alexandre.maniet
   */
  public MockContact() {
    super();
  }

  /**
   * @param expectedMethodsCalls expected methods.
   * @author alexandre.maniet
   */
  public MockContact(Collection<String> expectedMethodsCalls) {
    super(expectedMethodsCalls);
  }

  @Override
  public void checkNewContactInformation() throws InvalidInformationException {
    addCurrentMethodToSet();
  }

  @Override
  public Company getCompany() {
    addCurrentMethodToSet();
    return company;
  }

  @Override
  public int getCompanyId() {
    addCurrentMethodToSet();
    return company.getCompanyId();
  }

  @Override
  public int getContactId() {
    addCurrentMethodToSet();
    return 1;
  }

  @Override
  public String getEmail() {
    addCurrentMethodToSet();
    return "sampleEmail@mymail.com";
  }

  @Override
  public String getFirstName() {
    addCurrentMethodToSet();
    return "sampleFirstName";
  }

  @Override
  public String getId() {
    addCurrentMethodToSet();
    return null;
  }

  @Override
  public String getLastName() {
    addCurrentMethodToSet();
    return "sampleLastName";
  }

  @Override
  public String getPhoneNumber() {
    addCurrentMethodToSet();
    return "0000000000";
  }

  @Override
  public int getVersion() {
    addCurrentMethodToSet();
    return 1;
  }

  @Override
  public boolean isActive() {
    addCurrentMethodToSet();
    return true;
  }

  @Override
  public void setActive(boolean active) {
    addCurrentMethodToSet();
  }

  @Override
  public void setCompany(Company company) {
    addCurrentMethodToSet();
  }

  @Override
  public void setCompanyId(int companyId) {
    addCurrentMethodToSet();
  }

  @Override
  public void setContactId(int contactId) {
    addCurrentMethodToSet();
  }

  @Override
  public void setEmail(String email) {
    addCurrentMethodToSet();
  }

  @Override
  public void setFirstName(String firstName) {
    addCurrentMethodToSet();
  }

  @Override
  public void setLastName(String lastName) {
    addCurrentMethodToSet();
  }

  @Override
  public void setPhoneNumber(String phoneNumber) {
    addCurrentMethodToSet();
  }

  @Override
  public void setVersion(int version) {
    addCurrentMethodToSet();
  }

  @Override
  public void checkModifyContactInformation() throws InvalidInformationException {
    addCurrentMethodToSet();
  }

}
