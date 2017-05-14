package ucc;

import java.util.List;

import biz.Attendance;
import biz.AttendanceDto;
import biz.ParticipationDto;
import persistence.DalServices;
import persistence.UnitOfWork;
import persistence.dao.AttendanceDao;

public class AttendanceUccImpl implements AttendanceUcc {

  private AttendanceDao attendanceDao;
  private DalServices dal;

  /**
   * @param attendanceDao The ucc's associated dao.
   * @param unit an instance of unitOfWork needed to do insert, update and delete.
   * @param dal An instance of dalServices needed for transactions.
   */
  public AttendanceUccImpl(AttendanceDao attendanceDao, UnitOfWork unit, DalServices dal) {
    this.attendanceDao = attendanceDao;
    this.dal = dal;
  }

  @Override
  public AttendanceDto cancelAttendance(AttendanceDto attendanceDto) {
    Attendance attendance = (Attendance) attendanceDto;
    attendance = attendanceDao.setAttendanceConfirmation(attendance);
    if (!attendance.isConfirmed()) {
      return attendance;
    }
    return null;
  }

  @Override
  public List<Integer> addAllForCompany(AttendanceDto attendanceDto, int idCompany) {
    dal.startTransaction();
    Attendance attendance = (Attendance) attendanceDto;
    List<Integer> toReturn = attendanceDao.addAllForCompany(attendance, idCompany);
    dal.commit();
    return toReturn;
  }

  @Override
  public void editConfirmation(List<AttendanceDto> attendanceDtos) {
    dal.startTransaction();
    for (AttendanceDto attendanceDto : attendanceDtos) {
      Attendance attendance = (Attendance) attendanceDto;
      attendance = attendanceDao.setAttendanceConfirmation(attendance);
    }
    dal.commit();
  }

  @Override
  public List<AttendanceDto> getAllAttendance(ParticipationDto participation) {
    dal.startTransaction();
    List<AttendanceDto> toReturn =
        attendanceDao.findAttendanceContactByParticipation(participation);
    dal.commit();
    return toReturn;
  }
}
