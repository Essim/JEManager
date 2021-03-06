package tests;
import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import biz.Event;
import biz.objects.EventImpl;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class EventImplTest {

  private Event event1;
  private Event event2;
  private LocalDateTime now = LocalDateTime.now();
  private LocalDateTime oneHourFuture = LocalDateTime.now().plusHours(1);

  /**
   * @throws Exception if one is thrown during initialisation.
   */
  @Before
  public void setUp() throws Exception {
    event1 = new EventImpl();

    event2 = new EventImpl();
    event2.setClosed(false);
    event2.setDate(now);
    event2.setEventId(1);
  }

  /**
   * Testing all getters on a sample event.
   */
  @Test
  public void testGetters() {
    assertEquals("Event date should be " + now, now, event2.getDate());
    assertEquals("Event id should be 1", 1, event2.getEventId());
    assertEquals("Event should not be close", false, event2.isClosed());
  }

  /**
   * SetDate with correct value.
   */
  @Test
  public void testSetDate1() {
    event1.setDate(now);
    assertEquals("Date should be " + now, now, event1.getDate());
  }

  /**
   * SetDate with incorrect value (null).
   */
  @Test(expected = IllegalArgumentException.class)
  @SuppressFBWarnings("")
  public void testSetDate2() {
    event1.setDate(null);
  }

  /**
   * SetClosed with correct value.
   */
  @Test
  public void testSetClosed() {
    event1.setClosed(true);
    assertEquals("Event should be closed", true, event1.isClosed());
  }


  /**
   * SetEventId with correct value.
   */
  @Test
  public void testSetEventId1() {
    event1.setEventId(1);
    assertEquals("Event id should be 1", 1, event1.getEventId());
  }

  /**
   * SetEventId with incorrect value (negative integer).
   */
  @Test(expected = IllegalArgumentException.class)
  public void testSetEventId2() {
    event1.setEventId(-1);
  }

  /**
   * IfAfter with correct value (event 1 is one hour after event 2)
   */
  @Test
  public void testIsAfter() {
    event1.setDate(oneHourFuture);
    assertEquals("event1 should be after event2", true, event1.isAfter(event2));
  }
}
