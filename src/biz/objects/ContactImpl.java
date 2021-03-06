package biz.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.Company;
import exceptions.InvalidInformationException;
import util.Util;

public class ContactImpl implements biz.Contact {
  private int contactId;
  private Company company;
  private int companyId;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private boolean active;
  private int version;

  public int getContactId() {
    return contactId;
  }

  public Company getCompany() {
    return company;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public boolean isActive() {
    return active;
  }


  public int getCompanyId() {
    return companyId;
  }

  public void setCompanyId(int companyId) {
    Util.checkPositiveInteger(companyId);
    this.companyId = companyId;
  }

  public void setContactId(int contactId) {
    Util.checkPositiveInteger(contactId);
    this.contactId = contactId;
  }

  public void setCompany(Company company) {
    Util.checkNull(company);
    this.company = company;
  }

  public void setFirstName(String firstName) {
    Util.checkString(firstName);
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    Util.checkString(lastName);
    this.lastName = lastName;
  }

  @Override
  public int getVersion() {
    return this.version;
  }

  /**
   * {@inheritDoc}.
   */
  public void setEmail(String email) {
    if (email == null || email.isEmpty()) {
      this.email = "";
    } else {
      Util.checkString(email);
      this.email = email;
    }
  }

  /**
   * {@inheritDoc}.
   */
  public void setPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.isEmpty()) {
      this.phoneNumber = "";
    } else {
      Util.checkString(phoneNumber);
      this.phoneNumber = phoneNumber;
    }
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public void checkNewContactInformation() throws InvalidInformationException {
    if (companyId == 0 || company == null || firstName == null || lastName == null
        || (email == null && phoneNumber == null) || firstName.equals("") || lastName.equals("")
        || (email.equals("") && phoneNumber.equals(""))) {
      throw new InvalidInformationException("Informations incomplètes");
    }
    checkGlobal();
  }

  @Override
  public void checkModifyContactInformation() throws InvalidInformationException {
    if (firstName == null || lastName == null || (email == null && phoneNumber == null)
        || firstName.equals("") || lastName.equals("")
        || (email.equals("") && phoneNumber.equals(""))) {
      throw new InvalidInformationException("Informations incomplètes");
    }
    checkGlobal();
  }

  private void checkGlobal() {
    if (!email.isEmpty()) {
      Pattern p1 = Pattern.compile(".+@.+\\.[a-z]+");
      Matcher m1 = p1.matcher(email);
      boolean matchFound = m1.matches();
      if (!matchFound) {
        throw new InvalidInformationException("Email non valide");
      }
    }
    if (phoneNumber.length() > 15) {
      throw new InvalidInformationException(
          "Un numéro de téléphone valide doit contenir maximum 15 caractères");
    }
  }

  @Override
  public void setVersion(int version) {
    this.version = version;
  }

  @Override
  public String getId() {
    return this.getClass().getName() + this.contactId;
  }
}
