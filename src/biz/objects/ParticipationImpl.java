package biz.objects;

import biz.Company;
import biz.Event;
import exceptions.WrongStateException;
import util.Util;

public class ParticipationImpl implements biz.Participation {
  private int participationId;
  private int companyId;
  private Company company;
  private int eventId;
  private Event event;
  private String state;
  private int version;

  public int getParticipationId() {
    return participationId;
  }

  public Company getCompany() {
    return company;
  }

  public Event getEvent() {

    return event;
  }

  public String getState() {
    return state;
  }

  public void setParticipationId(int participationId) {
    Util.checkPositiveInteger(participationId);
    this.participationId = participationId;
  }

  public void setCompany(Company company) {
    Util.checkNull(company);
    this.company = company;
  }

  public void setEvent(Event event) {
    Util.checkNull(event);
    this.event = event;
  }

  public void setState(String state) {
    Util.checkString(state);
    this.state = state;
  }

  public int getCompanyId() {
    return companyId;
  }

  public void setCompanyId(int companyId) {
    Util.checkPositiveInteger(companyId);
    this.companyId = companyId;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId(int eventId) {
    Util.checkPositiveInteger(eventId);
    this.eventId = eventId;
  }

  @Override
  public void setVersion(int version) {

    this.version = version;
  }

  @Override
  public int getVersion() {

    return this.version;
  }

  /**
   * Checks that the state of this participation is a valid one.
   * 
   * @throws WrongStateException if it isn't the case.
   */
  public void checkState() throws WrongStateException {
    if (!this.state.equals("confirmed") && !this.state.equals("invited")
        && !this.state.equals("paid") && !this.state.equals("invoiced")
        && !this.state.equals("refused") && !this.state.equals("cancelled")) {
      throw new WrongStateException(
          "L'état :" + this.state + "ne fait pas parti des états possibles !");
    }
  }

  @Override
  public String getId() {
    return this.getClass().getName() + this.participationId;
  }


}
