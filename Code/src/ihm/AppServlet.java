package ihm;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

import biz.AttendanceDto;
import biz.AttendanceFactory;
import biz.Company;
import biz.CompanyDto;
import biz.CompanyFactory;
import biz.ContactDto;
import biz.ContactFactory;
import biz.Event;
import biz.EventDto;
import biz.EventFactory;
import biz.ParticipationDto;
import biz.ParticipationFactory;
import biz.User;
import biz.UserDto;
import biz.UserFactory;
import exceptions.FatalException;
import exceptions.IncompleteRequestException;
import exceptions.InvalidInformationException;
import exceptions.OptimisticException;
import exceptions.UserNameAlreadyPickedException;
import exceptions.WrongRequestException;
import exceptions.WrongStateException;
import ucc.AttendanceUcc;
import ucc.CompanyUcc;
import ucc.ContactUcc;
import ucc.EventUcc;
import ucc.ParticipationUcc;
import ucc.UserUcc;
import util.Util;

public class AppServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final int CONFLICT_ERROR = 409;
  /*
   * Used with the request.
   */
  private static final String JSON_PARAMETER = "json";
  private transient Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true)
      .useDateFormat(new SimpleDateFormat("yyyy-MM-dd")).useIndentation(true)
      .useConstructorWithArguments(true).useMethods(true).create();

  /*
   * Error Status.
   */
  private static final int SUCCESS = 200;
  private static final int USER_PERMISSION_DENIED = 401;
  private static final int FORBIDDEN = 403;
  private static final int CLIENT_ERROR = 400;
  private static final int SERVER_ERROR = 500;
  /*
   * Ucc.
   */
  private transient UserUcc userUcc;
  private transient EventUcc eventUcc;
  private transient CompanyUcc companyUcc;
  private transient ContactUcc contactUcc;
  private transient ParticipationUcc participationUcc;
  private transient AttendanceUcc attendanceUcc;
  /*
   * Factory.
   */
  private transient UserFactory userFactory;
  private transient EventFactory eventFactory;
  private transient CompanyFactory companyFactory;
  private transient ContactFactory contactFactory;
  private transient ParticipationFactory participationFactory;
  private transient AttendanceFactory attendanceFactory;

  private String secret;
  private static final String FILE_MODIFIED_CSV = "./csvDatas/modifiedCsv.txt";

  public void setSecret(String secret) {
    this.secret = secret;
  }

  /**
   * Initialize the ucc and factories needed.
   */
  public AppServlet(UserUcc userUcc, UserFactory userFactory, EventUcc eventUcc,
      EventFactory eventFactory, CompanyUcc companyUcc, CompanyFactory companyFactory,
      ContactUcc contactUcc, ContactFactory contactFactory, ParticipationUcc participationUcc,
      ParticipationFactory participationFactory, AttendanceUcc attendanceUcc,
      AttendanceFactory attendanceFactory) {
    this.userUcc = userUcc;
    this.userFactory = userFactory;
    this.eventUcc = eventUcc;
    this.eventFactory = eventFactory;
    this.companyUcc = companyUcc;
    this.companyFactory = companyFactory;
    this.contactUcc = contactUcc;
    this.contactFactory = contactFactory;
    this.participationUcc = participationUcc;
    this.participationFactory = participationFactory;
    this.attendanceUcc = attendanceUcc;
    this.attendanceFactory = attendanceFactory;
  }

  /**
   * The doPost, who redirect to the right method according to the "type" parameter. The order in
   * the switch case is kept in the whole class.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException, RuntimeException {
    try {
      resp.setCharacterEncoding("UTF-8");
      String type = req.getParameter("type");
      switch (type) {
        case "login":
          login(req, resp);
          return;
        case "register":
          register(req, resp);
          return;
        case "init":
          init(req, resp);
          return;
        default:
      }
      if (check(req) == null) {
        resp.setStatus(USER_PERMISSION_DENIED);
        resp.getWriter().print("Vous devez être connecté");
        return;
      }
      switch (type) {
        case "logout":
          logout(req, resp);
          return;
        case "addNewCompany":
          sendMessage(resp, addNewCompany(req));
          return;
        case "addNewContact":
          sendMessage(resp, addNewContact(req));
          return;
        case "getCompaniesForContacts":
          sendMessage(resp, getCompaniesForContacts(req));
          return;
        case "addNewEvent":
          sendMessage(resp, addNewEvent(req));
          return;
        case "addNewParticipation":
          sendMessage(resp, addNewParticipation(req));
          return;
        case "showCompanies":
          sendMessage(resp, showCompanies());
          return;
        case "showContacts":
          sendMessage(resp, showContacts());
          return;
        case "showContactsCompany":
          sendMessage(resp, showContactsCompany(req));
          return;
        case "showEvents":
          sendMessage(resp, showEvents());
          return;
        case "showParticipations":
          sendMessage(resp, showParticipations());
          return;
        case "showDashboardParticipations":
          sendMessage(resp, showDashboardParticipations());
          return;
        case "showResume":
          sendMessage(resp, showResume(req));
          return;
        case "editStateParticipation":
          sendMessage(resp, editStateParticipation(req));
          return;
        case "editConfirmationAttendance":
          sendMessage(resp, editConfirmationAttendance(req));
          return;


        case "showAllEventParticipations":
          sendMessage(resp, showAllEventParticipation(req));
          return;
        case "showInvitedEventParticipations":
          sendMessage(resp, showInvitedParticipations(req));
          return;
        case "showConfirmedEventParticipations":
          sendMessage(resp, showConfirmedParticipations(req));
          return;
        case "showRefusedEventParticipations":
          sendMessage(resp, showRefusedParticipations(req));
          return;
        case "showCancelledEventParticipations":
          sendMessage(resp, showCancelledParticipations(req));
          return;
        case "showInvoicedEventParticipations":
          sendMessage(resp, showInvoicedParticipations(req));
          return;
        case "showPaidEventParticipations":
          sendMessage(resp, showPaidParticipations(req));
          return;

        case "showAttendanceParticipations":
          sendMessage(resp, showAttendanceParticipations(req));
          return;
        case "setContactActivity":
          sendMessage(resp, setContactActivity(req));
          return;
        default:
      }
      if (!checkManager(req)) {
        resp.setStatus(FORBIDDEN);
        resp.getWriter().println("Accès Refusé");
        return;
      }
      switch (type) {
        case "showCompaniesToInvite":
          sendMessage(resp, showCompaniesToInvite());
          return;
        case "generateCsv":
          sendMessage(resp, generateCsv());
          return;
        case "generateModifiedCsv":
          sendMessage(resp, generateModifiedCsv());
          return;
        case "modifyContact":
          sendMessage(resp, modifyContact(req));
          return;
        default:
      }
    } catch (FatalException fae) {
      resp.setStatus(SERVER_ERROR);
      fae.printStackTrace();
      resp.getWriter().println("Une erreur serveur inattendue a eu lieu.");
    } catch (WrongRequestException wre) {
      resp.setStatus(FORBIDDEN);
      resp.getWriter().println("Requête non autorisée");
    } catch (OptimisticException ole) {
      resp.setStatus(CONFLICT_ERROR);
      resp.getWriter().println("Modification concurrente");
    } catch (Exception excep) {
      excep.printStackTrace();
      resp.setStatus(CLIENT_ERROR);
      resp.getWriter().println("Requête incorrecte");
    }
  }



  /**
   * Make a json of the obj and sent it back to the client.
   * 
   * @param resp The HttpResponse to sent to the client.
   * @param obj The obj to sent to the client.
   */
  private void sendMessage(HttpServletResponse resp, Object obj) {
    try {
      resp.setStatus(SUCCESS);
      resp.getWriter().println(genson.serialize(obj));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  // ----------------------------- START : doPost cases methods -----------------------------

  /**
   * LOGIN Check the user credit and connect the user to the application if they're true.
   * 
   * @author Sacre.Christopher
   * @param req The HttpRequest emit by the client.
   * @param resp The HttpResponse to sent to the client.
   */
  private void login(HttpServletRequest req, HttpServletResponse resp) {
    String json = req.getParameter(JSON_PARAMETER);
    Map<String, Object> map = new HashMap<String, Object>();
    if (json != null) {
      @SuppressWarnings("unchecked")
      Map<String, String> infos = genson.deserialize(json, Map.class);
      if (userUcc != null) {
        UserDto user = userFactory.getUser();
        user.setUsername(infos.get("login"));
        user.setPassword(infos.get("password"));
        if (userUcc.login(user) != null) {
          map.put("login", infos.get("login"));
          req.getSession().setAttribute("login", infos.get("login"));
          map.put("ip", req.getRemoteAddr());
          req.getSession().setAttribute("ip", req.getRemoteAddr());
          String jwt = new JWTSigner(secret).sign(map);
          Cookie cookie = new Cookie("user", jwt);
          cookie.setPath("/cookies");
          cookie.setMaxAge(60 * 60 * 24);
          resp.setStatus(SUCCESS);

          // Send the informations isManager to the front-end
          if (checkManager(req)) {
            map.put("manager", true);
          }
          resp.addCookie(cookie);
        } else {
          map.put("info", "Connexion refussée");
          resp.setStatus(USER_PERMISSION_DENIED);
        }
      } else {
        map.put("info", "Connexion refussée");
        resp.setStatus(USER_PERMISSION_DENIED);
      }
    } else {
      map.put("info", "Connexion refussée");
      resp.setStatus(USER_PERMISSION_DENIED);
    }
    try {
      resp.getOutputStream().write(genson.serialize(map).getBytes("UTF-8"));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }



  /**
   * LOGOUT Disconnect the user from the application by deleting his session and his cookie.
   * 
   * @author Sacre.Christopher
   * @param req The HttpRequest emitted by the client.
   * @param resp The HttpResponse to send to the client.
   */
  private Map<String, Object> logout(HttpServletRequest req, HttpServletResponse resp) {
    Object idUser = req.getSession().getAttribute("login");
    if (idUser != null) {
      req.getSession().removeAttribute("login");
      req.getSession().removeAttribute("ip");
    }
    Cookie cookie = getCookie(req);
    if (cookie != null) {
      cookie.setMaxAge(0);
      resp.addCookie(cookie);
    }
    resp.setStatus(SUCCESS);
    Map<String, Object> map = new HashMap<String, Object>();
    try {
      map.put("info", "Déconnexion réussie");
      resp.getOutputStream().write(genson.serialize(map).getBytes("UTF-8"));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return map;
  }



  /**
   * REGISTER Used to register a user and send back to him the JSON of the user created or an error
   * if something went wrong with his information.
   * 
   * @param req The HttpRequest emitted by the client.
   * @param resp The HttpResponse to send to the client.
   */
  private void register(HttpServletRequest req, HttpServletResponse resp) {
    String json = req.getParameter(JSON_PARAMETER);
    @SuppressWarnings("unchecked")
    Map<String, Object> map = genson.deserialize(json, Map.class);
    UserDto newUser = userFactory.getUser();
    newUser.setEmail((String) map.get("email"));
    newUser.setFirstName((String) map.get("firstname"));
    newUser.setLastName((String) map.get("lastname"));
    newUser.setUsername((String) map.get("username"));
    newUser.setPassword((String) map.get("password"));
    newUser.setRegistrationDate(LocalDateTime.now());
    try {
      userUcc.register(newUser);
    } catch (UserNameAlreadyPickedException unape) {
      unape.printStackTrace();
      try {
        resp.setStatus(USER_PERMISSION_DENIED);
        resp.getWriter().println(unape.getMessage());
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      return;
    } catch (InvalidInformationException uvi) {
      uvi.printStackTrace();
      try {
        resp.setStatus(USER_PERMISSION_DENIED);
        resp.getWriter().println(uvi.getMessage());
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      return;
    }
    try {
      resp.setStatus(201);
      resp.getOutputStream().write(genson.serialize(map).getBytes("UTF-8"));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }



  /**
   * Receives a new company's data as a serialised JSON object in the request, passes it on to
   * companyUCC for registration then sends the complete, registered company as a serialised JSON
   * object on the resp.
   * 
   * @author Maniet.Alexandre
   * @param req The client's request. Contains the new company data.
   * @throws IncompleteRequestException Thrown if a mandatory field isn't included in the request
   */
  private Object addNewCompany(HttpServletRequest req) throws IncompleteRequestException {
    // Tansferring request data to a HashMap
    @SuppressWarnings("unchecked")
    Map<String, String> ham = genson.deserialize(req.getParameter(JSON_PARAMETER), Map.class);
    // Checking mandatory fields
    List<String> mandatoryFields =
        Arrays.asList("name", "dateFirstContact", "street", "number", "postalCode", "municipality");
    checkMandatoryFields(ham, mandatoryFields);
    // Requesting a new Dto to fill
    CompanyDto newCompany = companyFactory.getCompany();
    // Creating a new formatter to properly set the company's firstContact
    // date
    String username = (String) req.getSession().getAttribute("login");
    // Getting a full user Dto from the username
    User userDto = (User) userUcc.getUserByUsername(username);
    // Setting the Dto's fields
    newCompany.setCreator(userDto);
    newCompany.setName(ham.get("name"));
    // LocalDateTime
    newCompany.setDateFirstContact(Util.stringToLocalDateTime(ham.get("dateFirstContact")));
    newCompany.setStreet(ham.get("street"));
    newCompany.setNumber(Integer.parseInt(ham.get("number")));
    if (ham.containsKey("box")) {
      newCompany.setBox(ham.get("box"));
    }
    newCompany.setPostalCode(Integer.parseInt(ham.get("postalCode")));
    newCompany.setMunicipality(ham.get("municipality"));
    newCompany.setCreator(userDto);
    // Registering the new company
    newCompany = companyUcc.addCompany(newCompany);
    // Setting the response flags
    return newCompany;
  }



  /**
   * Add a new contact in the DB, with the datas contained in the new contact form Check the
   * validity of the selected company.
   * 
   * @author Antoine.Maniet
   * @param req The HttpRequest emitted by the client.
   */
  private Object addNewContact(HttpServletRequest req) {
    @SuppressWarnings("unchecked")
    Map<String, Object> map = genson.deserialize(req.getParameter(JSON_PARAMETER), Map.class);
    ContactDto newContact = contactFactory.getContact();
    newContact.setCompanyId(Integer.parseInt((String) map.get("idCompany")));
    newContact.setFirstName((String) map.get("firstname"));
    newContact.setLastName((String) map.get("lastname"));
    newContact.setEmail((String) map.get("email"));
    newContact.setPhoneNumber((String) map.get("phonenumber"));
    newContact.setActive(true);
    newContact.setCompany((Company) companyUcc.getCompanyById(newContact.getCompanyId()));
    ContactDto contact = contactUcc.addContact(newContact);
    return contact;
  }


  /**
   * Retrieves all the companies for the addNewContact form.
   * 
   * @author Antoine.Maniet
   * @param req The HttpRequest emitted by the client.
   */
  private Object getCompaniesForContacts(HttpServletRequest req) {
    return companyUcc.getAllCompanies();
  }



  /**
   * Event creation service. Create a new event day in the DB, with a date after the latest event
   * day if there is one, after the current otherwise.
   *
   * @author sam.ndagano
   * @param req The HttpRequest emit by the client.
   */
  private Object addNewEvent(HttpServletRequest req) {
    LocalDateTime date = Util.stringToLocalDateTime(req.getParameter(JSON_PARAMETER));
    EventDto eventDto = eventFactory.getEvent();
    eventDto.setDate(date);
    EventDto result = eventUcc.addNewEvent(eventDto);
    result = eventUcc.getCurrentEvent();
    if (result == null) {
      throw new IllegalArgumentException();
    } else {
      return result;
    }
  }

  /**
   * Mark a contact as inactive.
   * 
   * @author Damien.Meur
   * @param req the HttpRequest emit by the client.
   * 
   */
  private Object setContactActivity(HttpServletRequest req) {
    @SuppressWarnings("unchecked")
    Map<Integer, Map<String, Object>> map =
        genson.deserialize(req.getParameter(JSON_PARAMETER), Map.class);
    for (Entry<Integer, Map<String, Object>> entry : map.entrySet()) {
      ContactDto contact = contactFactory.getContact();
      contact.setActive(!(boolean) entry.getValue().get("active"));
      contact.setContactId(Integer.parseInt((String) entry.getValue().get("id")));
      contact.setVersion(Integer.parseInt((String) entry.getValue().get("version")));
      contactUcc.setContactActivity(contact);
    }
    return contactUcc.getAllContacts();
  }

  /**
   * Modify the data of a contact.
   * 
   * @author Sacre.christopher
   * @param req the HttpRequest emit by the client.
   * 
   */
  private Object modifyContact(HttpServletRequest req) {
    @SuppressWarnings("unchecked")
    Map<String, Object> map = genson.deserialize(req.getParameter(JSON_PARAMETER), Map.class);
    ContactDto contact = contactFactory.getContact();
    contact.setActive((boolean) map.get("active"));
    contact.setContactId(Integer.parseInt((String) map.get("id")));
    contact.setVersion(Integer.parseInt((String) map.get("version")));
    contact.setFirstName((String) map.get("firstname"));
    contact.setLastName((String) map.get("lastname"));
    contact.setPhoneNumber((String) map.get("phonenumber"));
    contact.setEmail((String) map.get("email"));
    return contactUcc.updateContact(contact);
  }



  /**
   * Add a new participation in the DB, for the company invited through the button who called this
   * method. Check if the company doesn't already have a participation running for this Event. It
   * also adds a line in a file for the future modifiedCsv.
   * 
   * 
   * @author Antoine.Maniet
   * @param req The HttpRequest emitted by the client.
   * 
   */
  private Object addNewParticipation(HttpServletRequest req) {
    ParticipationDto newParticipation = participationFactory.getParticipation();
    newParticipation.setCompanyId(Integer.parseInt(req.getParameter("id")));
    newParticipation.setState("invited");
    newParticipation
        .setCompany((Company) companyUcc.getCompanyById(newParticipation.getCompanyId()));
    newParticipation.setEvent((Event) eventUcc.getCurrentEvent());
    newParticipation = participationUcc.addParticipation(newParticipation);
    AttendanceDto myFirstAttendance = attendanceFactory.getAttendance();
    myFirstAttendance.setParticipationId(newParticipation.getParticipationId());
    attendanceUcc.addAllForCompany(myFirstAttendance, Integer.parseInt(req.getParameter("id")));

    // Fill the modifiedCsv file
    Map<String, Object> csvRows = eventUcc.generateCsvRows(newParticipation.getParticipationId());
    for (Entry<String, Object> entryRow : csvRows.entrySet()) {
      byte[] data = {};
      try {
        data = ((String) entryRow.getValue() + "\n").getBytes("UTF-8");
      } catch (UnsupportedEncodingException uee) {
        uee.printStackTrace();
      }
      Path path = Paths.get(FILE_MODIFIED_CSV);
      try (OutputStream out =
          new BufferedOutputStream(Files.newOutputStream(path, CREATE, APPEND))) {
        out.write(data, 0, data.length);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
    // End of filling
    return "Participation ajoutée";
  }

  /**
   * Edit the state of the participations of several companies to a particular event.
   * 
   * @author Damien.Meur
   * @param req The HttpRequest emitted by the client.
   */
  private Object editStateParticipation(HttpServletRequest req) throws WrongStateException {
    @SuppressWarnings("unchecked")
    Map<String, HashMap<String, String>> map =
        genson.deserialize(req.getParameter(JSON_PARAMETER), Map.class);
    List<ParticipationDto> participationsToModify = new ArrayList<ParticipationDto>();
    EventDto currentEvent = eventUcc.getCurrentEvent();
    for (Entry<String, HashMap<String, String>> entry : map.entrySet()) {
      ParticipationDto participationToModify = participationFactory.getParticipation();
      participationToModify.setState(entry.getValue().get("state").toString());
      participationToModify
          .setVersion(Integer.parseInt(entry.getValue().get("version").toString()));
      if (currentEvent.getEventId() != Integer.parseInt(entry.getValue().get("event").toString())) {
        throw new WrongRequestException();
      }
      participationToModify.setParticipationId(Integer.parseInt(entry.getKey()));
      participationsToModify.add(participationToModify);

    }
    participationUcc.editStates(participationsToModify);
    return participationUcc.getAllPartcipationFor(eventUcc.getCurrentEvent());

  }

  /**
   * @author Sacre.Christopher
   * @param req The HttpRequest emitted by the client.
   */
  private Object editConfirmationAttendance(HttpServletRequest req) {
    int[] idAttendance = genson.deserialize(req.getParameter(JSON_PARAMETER), int[].class);
    List<AttendanceDto> attendanceDtos = new ArrayList<>();
    for (int attendanceId : idAttendance) {
      AttendanceDto att = attendanceFactory.getAttendance();
      att.setAttendanceId(attendanceId);
      attendanceDtos.add(att);
    }
    attendanceUcc.editConfirmation(attendanceDtos);
    Map<String, String> infos = new HashMap<String, String>();
    infos.put("info", "Modification sauvegardées");
    return infos;
  }

  /**
   * Used to gather all the companies and send it back to the front end.
   * 
   * @author Sacre.Christopher
   */
  private Object showCompanies() {
    return companyUcc.getAllCompanies();
  }



  /**
   * Gather all the companies who could still be invited.
   * 
   * @author Antoine.Maniet
   */
  private Object showCompaniesToInvite() {
    return companyUcc.getCompaniesToInvite();
  }



  /**
   * Gather all the contacts and send them back to the front end.
   * 
   * @author Antoine.Maniet
   */
  private Object showContacts() {
    List<ContactDto> contacts = contactUcc.getAllContacts();
    for (ContactDto contact : contacts) {
      contact.setCompany((Company) companyUcc.getCompanyById(contact.getCompanyId()));
    }
    return contacts;
  }



  /**
   * This sends all the contacts related to a specific company.
   * 
   * @author Sacre.Christopher
   * @param req The HttpRequest emitted by the client.
   */
  private Object showContactsCompany(HttpServletRequest req) {
    CompanyDto company = companyFactory.getCompany();
    String companyId = req.getParameter("company");
    if (companyId != null) {
      company.setCompanyId(Integer.parseInt(companyId));
      List<ContactDto> contacts = companyUcc.getMyContacts(company);
      return contacts;
    } else {
      throw new IllegalArgumentException();
    }
  }


  /**
   * This finds all the confirmed attendance ( describes as contact information ) related to a
   * specific participation.
   * 
   * @author sam.ndagano
   * @param req he HttpRequest emitted by the client.
   */
  private Object showAttendanceParticipations(HttpServletRequest req) {
    ParticipationDto participation = participationFactory.getParticipation();
    String participationId = req.getParameter("participation");
    if (participationId != null) {
      participation.setParticipationId((Integer.parseInt(participationId)));
      List<AttendanceDto> attendanceDtos = attendanceUcc.getAllAttendance(participation);
      return attendanceDtos;
    } else {
      throw new IllegalArgumentException();
    }
  }



  /**
   * Used to gather all the events and sent it back to the front end.
   * 
   * @author Sacre.Christopher
   */
  private Object showEvents() {
    return eventUcc.getAllEvents();
  }



  /**
   * Used to gather the participations of the current business day.
   * 
   * @author Sacre.Christopher
   */
  private Object showParticipations() {
    return participationUcc.getCurrentParticipations();
  }

  /**
   * Used to gather the participations of every business day.
   * 
   * @author Antoine.Maniet
   */
  private Object showDashboardParticipations() {
    return participationUcc.getAllParticipations();
  }


  /**
   * Give a resume about the participations of a business day.
   * 
   * @author Sacre.Christopher
   * @param req The client's request. Contains the new company data.
   */
  private Object showResume(HttpServletRequest req) {
    String eventString = req.getParameter(JSON_PARAMETER);
    if (eventString == null) {
      throw new IllegalArgumentException();
    }
    EventDto event = eventFactory.getEvent();
    event.setEventId(Integer.parseInt(eventString));
    Map<String, Integer> resume = new HashMap<String, Integer>();
    resume.put(ParticipationUcc.INVITED, participationUcc.countInvitedParticipation(event));
    resume.put(ParticipationUcc.CONFIRMED, participationUcc.countConfirmedParticipation(event));
    resume.put(ParticipationUcc.REFUSED, participationUcc.countRefusedParticipation(event));
    resume.put(ParticipationUcc.CANCELLED, participationUcc.countCancelledParticipation(event));
    resume.put(ParticipationUcc.INVOICED, participationUcc.countInvoicedParticipation(event));
    resume.put(ParticipationUcc.PAID, participationUcc.countPaidParticipation(event));
    return resume;
  }

  /**
   * Gather every data in a map and send them to the javascript.
   * 
   * @author Antoine.Maniet
   */
  private Object generateCsv() {
    EventDto event = eventFactory.getEvent();
    event = eventUcc.getCurrentEvent();
    return eventUcc.generateFullCsv(event);
  }

  /**
   * Put the recently modified datas (contained in a file) in a map and send them to the javascript.
   * 
   * @author Antoine.Maniet
   */
  private Object generateModifiedCsv() {
    // fill the map with the content of the file
    Map<String, Object> myModifiedDatas = new HashMap<>();
    String line = "";
    int order = 0;
    try (InputStream fis = new FileInputStream(FILE_MODIFIED_CSV);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);) {
      while ((line = br.readLine()) != null) {
        myModifiedDatas.put(order + "", line);
        order++;
      }
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    try {
      new OutputStreamWriter(new FileOutputStream(FILE_MODIFIED_CSV), "UTF-8").close();
    } catch (UnsupportedEncodingException uee) {
      uee.printStackTrace();
    } catch (FileNotFoundException fnfe) {
      fnfe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return myModifiedDatas;
  }

  /**
   * Used to check if the user is already known of our system and sent the answer back to the
   * front-end.
   * 
   * @author Sacre.Christopher
   * @param req The HttpRequest emitted by the client.
   * @param resp The HttpResponse to send to the client.
   */
  private void init(HttpServletRequest req, HttpServletResponse resp) {
    if (check(req) != null) {
      resp.setStatus(SUCCESS);
      Cookie cookie = getCookie(req);
      if (cookie != null) {
        resp.addCookie(cookie);
      }

      // Send the informations isManager to the front-end
      Map<String, Object> json = new HashMap<String, Object>();
      if (checkManager(req)) {
        json.put("manager", true);
      }
      json.put("info", "Connexion réussie");
      try {
        resp.getOutputStream().write(genson.serialize(json).getBytes("UTF-8"));
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    } else {
      resp.setStatus(USER_PERMISSION_DENIED);
    }
  }
  // ----------------------------- END : doPost cases methods -----------------------------


  // ----------------------------- START : redirection methods ---------------------------

  private List<ParticipationDto> showAllEventParticipation(HttpServletRequest req) {
    EventDto event = eventUcc.getEventById(Integer.parseInt(req.getParameter(JSON_PARAMETER)));
    return participationUcc.getAllPartcipationFor(event);
  }

  private List<ParticipationDto> showInvitedParticipations(HttpServletRequest req) {
    EventDto event = eventUcc.getEventById(Integer.parseInt(req.getParameter(JSON_PARAMETER)));
    return participationUcc.getInvitedParticipationsFor(event);
  }

  private List<ParticipationDto> showConfirmedParticipations(HttpServletRequest req) {
    EventDto event = eventUcc.getEventById(Integer.parseInt(req.getParameter(JSON_PARAMETER)));
    return participationUcc.getConfirmedParticipationsFor(event);
  }

  private List<ParticipationDto> showRefusedParticipations(HttpServletRequest req) {
    EventDto event = eventUcc.getEventById(Integer.parseInt(req.getParameter(JSON_PARAMETER)));
    return participationUcc.getRefusedParticipationsFor(event);
  }

  private List<ParticipationDto> showCancelledParticipations(HttpServletRequest req) {
    EventDto event = eventUcc.getEventById(Integer.parseInt(req.getParameter(JSON_PARAMETER)));
    return participationUcc.getCancelledParticipationsFor(event);
  }

  private List<ParticipationDto> showInvoicedParticipations(HttpServletRequest req) {
    EventDto event = eventUcc.getEventById(Integer.parseInt(req.getParameter(JSON_PARAMETER)));
    return participationUcc.getInvoicedParticipationsFor(event);
  }

  private List<ParticipationDto> showPaidParticipations(HttpServletRequest req) {
    EventDto event = eventUcc.getEventById(Integer.parseInt(req.getParameter(JSON_PARAMETER)));
    return participationUcc.getPaidParticipationsFor(event);
  }

  // ----------------------------- END : redirection methods -----------------------------


  // ----------------------------- START : utility methods -----------------------------

  /**
   * @author Maniet.Alexandre
   * @param ham HashMap containing deserialized data from a form
   * @param requiredFields list of fields which must be present in the map
   * @throws IncompleteRequestException thrown if a mandatory field isn't present in the map
   */
  private void checkMandatoryFields(Map<String, String> ham, List<String> requiredFields)
      throws IncompleteRequestException {
    for (String s : requiredFields) {
      if (!ham.containsKey(s)) {
        throw new IncompleteRequestException(
            "The mandatory field " + s + " wasn't found in the request's form data parameter");
      }
    }
  }


  /**
   * Test the HttpRequest to see if the client have a cookie or a session defined.
   * 
   * @author Sacre.Christopher
   * @param req The HttpRequest emit by the client.
   * @param resp The HttpResponse to sent to the client.
   * @param genson The genson dictionary used to transmit information to the client.
   */
  private UserDto check(HttpServletRequest req) {
    String idUser = (String) req.getSession().getAttribute("login");
    String login = null;
    if (idUser == null) {
      Cookie cookie = getCookie(req);
      if (cookie != null) {
        String token = cookie.getValue();
        try {
          Map<String, Object> decodedPayload = new JWTVerifier(secret).verify(token);
          login = (String) decodedPayload.get("login");
          if (!req.getRemoteAddr().equals(decodedPayload.get("ip"))) {
            idUser = null;
          }
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException
            | SignatureException | IOException | JWTVerifyException exceptions) {
          exceptions.printStackTrace();
        }
        // Connection Fail (check the credits)
        if (login != null) {
          UserDto user = userUcc.getUserByUsername(login);
          if (user != null) {
            req.getSession().setAttribute("login", user.getUsername());
            return user;
          } else {
            return null;
          }
        } else {
          return null;
        }
      } else {
        return null;
      }
    } else {
      UserDto user = userUcc.getUserByUsername(idUser);
      if (user != null) {
        return user;
      } else {
        return null;
      }
      // Connection Succes (Session)
    }
  }


  /**
   * Test the HttpRequest and get the cookie if exist.
   * 
   * @author Sacre.Christopher
   * @param req : The HttpRequest of the user's connection attempt.
   * @return the cookie if exist or null.
   */
  private Cookie getCookie(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("user".equals(cookie.getName()) && cookie.getSecure()) {
          return cookie;
        } /*
           * else if ("user".equals(cookie.getName()) && token == null) { return cookie; }
           */
      }
    }
    return null;
  }


  /**
   * Test the HttpRequest to see if the client is logged as a manager.
   * 
   * @author Maniet.Antoine
   * @param req The HttpRequest emit by the client.
   * @param resp The HttpResponse to sent to the client.
   * @param genson The genson dictionary used to transmit information to the client.
   */
  private boolean checkManager(HttpServletRequest req) {
    String loginUser = (String) req.getSession().getAttribute("login");
    if (loginUser == null) {
      return false;
    }
    return userUcc.getUserByUsername(loginUser).isManager();
  }
}
// ----------------------------- END : utility methods -----------------------------

