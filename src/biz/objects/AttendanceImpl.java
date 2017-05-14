package biz.objects;

import biz.Contact;
import biz.Participation;
import util.Util;

public class AttendanceImpl implements biz.Attendance {
  private int attendanceId;
  private Contact contact;
  private int contactId;
  private Participation participation;
  private int participationId;
  private boolean confirmed = true;
  private int version = 0;

  public int getAttendanceId() {
    return attendanceId;
  }

  public Contact getContact() {
    return contact;
  }

  public Participation getParticipation() {
    return participation;
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public void setAttendanceId(int attendanceId) {
    Util.checkPositiveInteger(attendanceId);
    this.attendanceId = attendanceId;
  }

  public void setContact(Contact contact) {
    Util.checkNull(contact);
    this.contact = contact;
  }

  public void setParticipation(Participation participation) {
    Util.checkNull(participation);
    this.participation = participation;
  }

  public void setConfirmed(boolean confirmed) {
    this.confirmed = confirmed;
  }

  public int getContactId() {
    return contactId;
  }

  public void setContactId(int contactId) {
    Util.checkPositiveInteger(contactId);
    this.contactId = contactId;
  }

  public int getParticipationId() {
    return participationId;
  }

  public void setParticipationId(int participationId) {
    Util.checkPositiveInteger(participationId);
    this.participationId = participationId;
  }

  @Override
  public void setVersion(int version) {

    this.version = version;
  }

  @Override
  public int getVersion() {
    return this.version;
  }

  @Override
  public String getId() {
    return this.getClass().getName() + this.attendanceId;
  }
}
