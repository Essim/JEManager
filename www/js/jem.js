/*-
 * Groupe PAE06
 */
var codeSecret = "";
var isConnected = false;
var isManager = false;
var companiesTable;
var contactsTable;
var companiesToInviteTable;
var participationsTable;
var participationsDashboardTable;
var eventSelected;
var eventDate;
var participationsToModify = {};
var loginObject = new Login();
var showObject = new Show();
var utilObject = new Util();
var AttendancesToCancel = [];
var contactSelected = {};
var addContact;
var contactsToModify = {};
/*-
 *
 * Index of the // zones after the "main" function
 * 1- Initiate onclick-functionsrelated to login and register
 * 2- Initiate navbar onclick-functions
 * 3- Initiate content onclick-functions
 * 4- Utility functions
 * 		4.1- HIDE AND SHOW table functions
 * 		4.2- EMPTY FORM functions
 *      4.3- GET INFOS functions
 * 5- Unsorted things
 * 6- Useless but fun functions
 */

/*-
 *  tips : formatting comment block comment into -> /*- */

/*-
 * tips : install plugin webClipse.
 */

//Do almost everything
$(function() {
	pageInit(); // 4.x
	loginAndRegisterFunctions(); // 1
	initiateNavbar(); // 2
	initiateContentFunction(); // 3
	redirection(); // 5

	/*-
	 * Ajax call to initialize the connection if the user is known of the server
	 */
	ajax("init", undefined, loginObject.init, undefined);

});

/*-
 *  ------------------------------ START : Initiate onclick-functions related to login and register ------------------------------ 1
 */

var login = function() {
	if ($("#login").val() === "" && $("#password").val() === "") {
		toastr.warning("Veuillez entrer vos informations de connexion");
	} else if ($("#login").val() === "") {
		toastr.warning("Veuillez entrer votre login");
	} else if ($("#password").val() === "") {
		toastr.warning("Veuillez entrer votre mot de passe");
	} else {
		var dict = {};
		dict["login"] = $("#login").val();
		dict["password"] = $("#password").val();
		ajax("login", JSON.stringify(dict), loginObject.loginSuccess,
				loginObject.loginError);
	}
}

/*
 * Object which contains the logins and register functions (Here to replace the
 * login and register functions.
 */
function Login() {
	/*-
	 * Function to log out
	 */
	function logout(response) {
		if (response != undefined) {
			isConnected = false;
			$("#show_success").empty();
			$("#nav").hide();
			$("#content").hide();
			closeAll();
			$("#accueil").show();
			toastr.success(response.info);
		} else {
			toastr.error("Déconnexion échouée");
		}
	}

	function init(response) {
		if (response != undefined) {
			isConnected = true;
			ifManager = false;
			if (response.manager) {
				isManager = true;
			}
			hideIfNotManager();
			$("#accueil").hide();
			$("#nav").show();
			$("#dashboard").show();
			$("#participationsDashboard").hide();
			$("#showDashboard").trigger('click');
			$("#content").show();
			toastr.success(response.info);
		}
	}

	function loginSuccess(response) {
		isConnected = true;
		isManager = false;
		if (response.manager == true) {
			isManager = true;
		}
		$("#accueil").hide();
		$("#nav").show();
		$("#dashboard").show();
		$("#participationsDashboard").hide();
		$("#showDashboard").trigger('click');
		$("#content").show();
		$("#showCompaniesToInvite").show();
		$("#getCsvButton").show();
		$("#getModifiedCsvButton").show();
		hideIfNotManager();
		toastr.success("Connexion réussie");
	}

	function loginError(response) {
		toastr.error("Connexion échouée");
	}

	function registerSuccess(response) {
		toastr.success("Inscription réussie !", "", {
			timeOut : 5000
		});
		emptyRegisterForm();
		$("#form_register").addClass("hidden");
		$("#login").val(response.username)
		$("#password").val(response.password)
		$("#signInDiv").show();
		$("#registerInfo").show();
		login();
	}

	function registerError(response) {
		toastr.error("Inscription échouée");
	}

	this.logout = logout;
	this.init = init;
	this.loginSuccess = loginSuccess;
	this.loginError = loginError;
	this.registerSuccess = registerSuccess;
	this.registerError = registerError;
}

var loginAndRegisterFunctions = function() {
	/*
	 * Function to allow the user to connect to the application
	 */
	$("#connectButton").on("click", function(event) {
		login();
	});

	$("#signInDiv").on("keyup", function(event) {
		if (event.which === 13) {
			login();
		}
	});

	/*
	 * When clicking on the logout button : disconnect the user.
	 */
	$("#logout").on("click", function(e) {
		ajax("logout", undefined, loginObject.logout, undefined);
	});

	/*-
	 * Shows the register form on click
	 */
	$("#form_register_show").click(function() {
		$("#form_register").removeClass("hidden");
		$("#signInDiv").hide();
		$("#registerInfo").hide();
		$("#loginInfo").show();
	});

	/*-
	 * Hide the register form on click
	 */
	$("#form_register_hide").click(function() {
		emptyRegisterForm();
		$("#form_register").addClass("hidden");
		$("#loginInfo").hide();
		$("#signInDiv").show();
		$("#registerInfo").show();
	});

	/*-
	 * Try to register the user on click
	 */
	$("#register").click(function() {
		// fields empty
		var manqueChamp = false;
		$("#form_register input").each(function(index, elt) {
			if ($(this).val() == null || $(this).val() == '') {
				toastr.warning("Veuillez compléter tous les champs");
				manqueChamp = true;
				return false;
			}
		});
		if (manqueChamp)
			return;
		// email doesn't match
		var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		if (!re.test($("#email").val())) {
			toastr.warning("Veuillez entrer une adresse email valide");
			return false;
		}
		if ($("#email").val() !== $("#confirm_email").val()) {
			toastr.warning("Les emails ne correspondent pas");
			return false;
		}
		// mot de passe doesn't match
		var matches = $("#password_register").val().match(/\d+/g);
		if ($("#password_register").val().length < 6 ||
				$("#password_register").val().toUpperCase() === $("#password_register").val() ||
				$("#password_register").val().toLowerCase() === $("#password_register").val()) {
			toastr.warning("Les mots de passe doivent faire plus de 6 caracteres, comporter au moins une majuscule /minuscule et au moins un chiffre !",
					"", { timeOut : 5000});
			return false;
		}
		if ($("#password_register").val() !== $("#confirm_password").val()) {
			toastr.warning("Les mots de passe ne correspondent pas");
			return false;
		}
		ajax("register", JSON.stringify(getRegisterInfo()), loginObject.registerSuccess, loginObject.registerError);
	});
}
/*-
 * ------------------------------ END : Initiate onclick-functions related to  login and register ------------------------------ 1
 */

/*-
 * ------------------------------ START : Initiate navbar onclick-functions ------------------------------ 2
 */


function Show() {

	function dashboardSuccess(json) {
		if(json[0] != undefined) {
			eventSelected = json[0].eventId;
			eventDate = json[0].date.dayOfMonth
			+ "-" + json[0].date.monthValue
			+ "-" + json[0].date.year;
		}
		var trs = "";
		$.each(json, function(val) {
			var tr = "<tr>";
			tr += "<td  id='"
				+ json[val].eventId
				+ "'><h5>"
				+ json[val].date.dayOfMonth
				+ "-"
				+ json[val].date.monthValue
				+ "-" + json[val].date.year
				+ "</h5></td>";
			tr += "</tr>";
			trs += tr;
		});
		$("#eventsTable tbody").html(trs);
		if(eventSelected != undefined)
			selectEvent();
	}

	function dashboardError(response) {
		toastr.error("Problème lors de l'affichage des journées entreprises");
	}

	function companiesSuccess(json) {
		companiesTable.clear().draw(false);
		$.each(json,function(val) {
			companiesTable.row.add(	["",
				json[val].companyId,
				json[val].name,
				json[val].street,
				json[val].number,
				json[val].municipality,
				json[val].postalCode ]).draw(false);
		});
	}

	function companiesError(response) {
		toastr.error("Problème lors de l'affichage des entreprises");
	}

	function contactsSuccess(json) {
		contactsTable.clear().draw(false);
		$.each(json, function(val) {
			contactsTable.row.add([json[val].company.name,
				json[val].firstName,
				json[val].lastName,
				json[val].email,
				json[val].phoneNumber,
				toggleContactDraw(json[val].active, json[val].contactId, json[val].version),
				buttonSetContact(), json[val].companyId]).draw(false);
		});
	}

	function contactsError(response) {
		toastr.error("Problème lors de l'affichage des contacts");
	}

	function invitationsSuccess(json) {
		companiesToInviteTable.clear().draw(false);
		$.each(json, function(val) {
			companiesToInviteTable.row.add([json[val].name,
				json[val].street,
				json[val].number,
				json[val].municipality,
				json[val].postalCode,
				"<input type=\"checkbox\"/>",
				json[val].companyId ]).draw(false);
		});
		$("#inviteButton").on("click", function() {
			$("#companiesToInviteTable tbody input:checked").each(function() {
				$(this).prop('checked',false);
				var data = companiesToInviteTable.row($(this).parents('tr')).data();
				$.ajax({
					url : "/Login",
					type : "POST",
					data : {
						type : "addNewParticipation",
						id : data[6]
					},
					success : function(response) {
						companiesToInviteTable.clear().draw(false);
						$("#showCompaniesToInvite").trigger("click");
						toastr.success("Entreprise invitée !");
					},
					error : function(response) {
						toastr.error("Problème lors de l'invitation de l'entreprise");
					}
				});
			});
		});
	}

	function invitationsError(response) {
		toastr.error("Problème lors de l'affichage des entreprises à inviter");
	}

	function selectEventSuccess(json) {
		$("#pieHover").html("");
		var content = "<p id='invitedParticipations' class='dashboardLink'>Nombre entreprises invitées : " + json.invited+ "</p>";
		content += "<p id='confirmedParticipations' class='dashboardLink'>Nombre d'entreprises ayant confirmé : "+ json.confirmed + "</p>";
		content += "<p id='refusedParticipations' class='dashboardLink'>Nombre d'entreprises ayant refusé : " + json.refused+ "</p>";
		content += "<p id='cancelledParticipations' class='dashboardLink'>Nombre d'entreprises ayant annulé : " + json.cancelled + "</p>";
		content += "<p id='invoicedParticipations' class='dashboardLink'>Nombre d'entreprises facturées : " + json.invoiced + "</p>";
		content += "<p id='paidParticipations' class='dashboardLink'>Nombre d'entreprises ayant payé : " + json.paid + "</p>";
		var data = [
			{ label: "invited",  data: json.invited, color: "#C4DEE6"},
			{ label: "confirmed",  data: json.confirmed, color: "#D9FFDA"},
			{ label: "refused",  data: json.refused, color: "#FFA0A0"},
			{ label: "cancelled",  data: json.cancelled, color: "#FF99EC"},
			{ label: "invoiced",  data: json.invoiced, color: "#FFFFAB"},
			{ label: "paid",  data: json.paid, color: "#FFD5A0"}
			];
		$.plot($("#chart"), data, {
			series : {
				pie : {
					show: true,
					radius: 1,
					label: {
						show: true,
						radius: 2/3,
						formatter: function(label, series) {
							return '<div style="font-size:11px ;text-align:center; padding:2px; color:black;">'+label+'<br/>'+Math.round(series.percent)+'%</div>';
						},
						threshold: 0.1
					}
				}
			},
			grid: {
				hoverable: true
			},
			legend : {
				labelBoxBorderColor : "none"
			}
		});
		$("#chart").bind("plothover", pieHover);
		$("#dashboardHeader").html("Détails de la journée entreprise du " + eventDate);
		$("#dashboardBody").html(content);
	}

	function pieHover(event, pos, obj) {
		if(!obj)
			return;
		percent = parseFloat(obj.series.percent).toFixed(2);
		$("#pieHover").html('<span>' + obj.series.label + ' (' + percent + '%)</span>')
	}

	function selectEventError(response) {
		toastr.error("Erreur lors de l'affichage du résumé");
	}

	function participationsSuccess(json) {
		participationsTable.clear().draw(false);
		$.each(json, function(val) {
			participationsTable.row.add([
				"",
				json[val].participationId,
				json[val].company.name,
				json[val].company.street,
				json[val].company.number,
				json[val].company.municipality,
				json[val].company.postalCode,
				setSelectStateParticipation(
						json[val].participationId,
						json[val].version,
						json[val].state) ]).draw(false);
		});
		participationsTable.on('draw', function() {
			$('tr').each(function() {
				$(this).addClass($(this).data()[5])
			})
		});
	}

	function participationsDashboardSuccess(json) {
		participationsDashboardTable.clear().draw(false);
		$.each(json, function(val) {
			var date = json[val].event.date.dayOfMonth
			+ "-" + json[val].event.date.monthValue
			+ "-" + json[val].event.date.year;
			participationsDashboardTable.row.add([
				"",
				json[val].participationId,
				json[val].company.name,
				json[val].company.street,
				json[val].company.number,
				json[val].company.municipality,
				json[val].company.postalCode,
				date,
				setSelectStateParticipation(
						json[val].participationId,
						json[val].version,
						json[val].state) ]).draw(false);
		});
		participationsDashboardTable.on('draw', function() {
			$('tr').each(function() {
				$(this).addClass($(this).data()[6])
			})
		});
	}

	function participationsError(response) {
		toastr.error("Erreur lors de l'affichage des participations");
	}

	function addContactSuccess(json) {
		$("#form_addContactCompany").html("");
		if (json.length == 0) {
			$("#form_addContactCompany").append($("<option>", {
				value: -1,
				text: "Pas d'entreprise connue"
			}));
		} else {
			$.each(json, function (i, company) {
				$("#form_addContactCompany").append($("<option>", {
					value: company.companyId,
					text : company.name
				}));
			});
		}
		if(addContact) {
			$("#panelHeadingFormContact").html("Formulaire de modification d'un contact");
			$("#form_addContactCompany").val(contactSelected["companyId"]);
			$("#form_addContactCompany").prop("disabled", true);
			$("#form_addContactFirstName").val(contactSelected["firstname"]);
			$("#form_addContactLastName").val(contactSelected["name"]);
			$("#form_addContactEmail").val(contactSelected["email"]);
			$("#form_addContactPhoneNumber").val(contactSelected["tel"]);
			$("#form_addContactSubmit").html("Modifier");
		} else {
			$("#panelHeadingFormContact").html("Formulaire d'ajout d'un contact");
			//document.getElementById("form_addContactCompany").value = "";
			$("#form_addContactCompany").prop("disabled", false);
			$("#form_addContactFirstName").val("");
			$("#form_addContactLastName").val("");
			$("#form_addContactEmail").val("");
			$("#form_addContactPhoneNumber").val("");
			$("#form_addContactSubmit").html("Ajouter");
		}
	}

	function addContactError(response) {
		toastr.error("Erreur lors de la récupération des entreprises");
	}

	this.addContactSuccess = addContactSuccess;
	this.addContactError = addContactError;
	this.dashboardSuccess = dashboardSuccess;
	this.dashboardError = dashboardError;
	this.companiesSuccess = companiesSuccess;
	this.companiesError = companiesError;
	this.contactsSuccess = contactsSuccess;
	this.contactsError = contactsError;
	this.invitationsSuccess = invitationsSuccess;
	this.invitationsError = invitationsError;
	this.selectEventSuccess = selectEventSuccess;
	this.selectEventError = selectEventError;
	this.participationsSuccess = participationsSuccess;
	this.participationsDashboardSuccess = participationsDashboardSuccess;
	this.participationsError = participationsError;
}



var initiateNavbar = function() {
	$("#JEManager").on("click", function(e) {
		closeAll();
		showParticipations();
	});
	$("#showFormCompany").on("click", function(e) {
		closeAll();
		$("#newCompany").show();
	});

	$("#showFormContact").on("click", function(e) {
		addContact = false;
		contactSelected = {};
		closeAll();
		ajax("getCompaniesForContacts", null, showObject.addContactSuccess, showObject.addContactError);
		$("#newContact").show();
	});

	$("#eventsTable").on("click", "td", function(event) {
		eventDate = $(this).text();
		eventSelected = $(this).attr("id");
		selectEvent();
	});

	/*-
	 * Used to show the dashboard div and download the several business day.
	 */
	$("#showDashboard").on("click",function(e) {
		closeAll();
		ajax("showEvents", undefined, showObject.dashboardSuccess, showObject.dashboardError);
		$("#dashboard").show();
		$("#participationsDashboard").hide();
	});

	/*-
	 * Used to show the several companies encoded in the DataBase.
	 */
	$("#showCompanies").on("click", function(e) {
		closeAll();
		ajax("showCompanies", undefined, showObject.companiesSuccess, showObject.companiesError);
		$("#companies").show();
	});

	// Add event listener for opening and closing details
	$('#allCompaniesTable tbody').on('click', 'td.details-control', function() {
		var tr = $(this).closest('tr');
		var row = companiesTable.row(tr);
		if (row.child.isShown()) {
			row.child.hide();
			tr.removeClass('shown');
		} else {
			row.child(format(row.data())).show();
			$('.toggleContact').bootstrapToggle({
				on: 'Travail Actuel',
				off: 'A demissionné'
			});
			tr.addClass('shown');
		}
	});

	// Add event listener for opening and closing details showing attendance by
	// participation
	$('#participationsTable tbody').on('click', 'td.participations-control', function() {
		var tr = $(this).closest('tr');
		var row = participationsTable.row(tr);
		if (row.child.isShown()) {
			row.child.hide();
			tr.removeClass('shown');
		} else {
			row.child(formatAttendance(row.data())).show();
			tr.addClass('shown');
		}
	});


	$('#participationsDashboardTable tbody').on('click', 'td.participations-control', function() {
		var tr = $(this).closest('tr');
		var row = participationsDashboardTable.row(tr);
		if (row.child.isShown()) {
			row.child.hide();
			tr.removeClass('shown');
		} else {
			row.child(formatAttendance(row.data())).show();
			tr.addClass('shown');
		}
	});




	/*-
	 * Used to show the several contacts encoded in the DataBase.
	 */
	$("#showContacts").on("click", function(e) {
		showContacts();
	});

	$("#showParticipations").on("click", function(e) {
		showParticipations();
	});

	$("#showCompaniesToInvite").on("click", function(e) {
		closeAll();
		var showCompaniesToInviteAjax = ajax("showCompaniesToInvite", undefined, showObject.invitationsSuccess, showObject.invitationsError);
		$("#companiesToInvite").show();
	});
}


//TODO
var formatAttendance = function(d) {
	var dataTable;
	$.ajax({
		url : "/Login",
		type : "POST",
		async : false,
		data : {
			type : "showAttendanceParticipations",
			participation : d[1]
		},
		success : function(response) {
			var json = JSON.parse(response);
			dataTable = '<div class="panel panel-default">'
				+ '<div class="panel-heading"> Contacts présent pour ' + d[2]
			+ '</div>' + '<div class="panel-body">';
			if (json.length == 0) {
				dataTable += '<p>Aucune presence pour cette participation </p>';
			} else {
				dataTable += '<table class="table table-striped table-bordered table-hover"><thead><tr><th>Prénom</th><th>Nom</th><th>Email</th><th>Téléphone</th><th>Annuler présence</th></tr></thead><tbody>';
				$.each(json, function(val) {
					dataTable += '<tr name= '+ json[val].attendanceId  +'><td>' + json[val].contact.firstName
					+ '</td><td>' + json[val].contact.lastName
					+ '</td><td>' + json[val].contact.email
					+ '</td><td>' + json[val].contact.phoneNumber
					+ '</td><td><input class="attendanceCheckbox" type="checkbox"></td></tr>';
				});
				dataTable += '</tbody></table>'
//					dataTable += '<button type="button" class="btn btn-primary btn-smd
//					cancelAttandanceBtn">Supprimer présence(s) séléctionnée(s)</button>'
			}
			dataTable += "</div></div>";



		},
		error : function(response) {
			toastr.error("Erreur lors de l'affichage des contacts présent: " + d[2]);
		}
	});
	return dataTable;
}




var format = function(d) {
	var dataTable;
	// `d` is the original data object for the row
	$.ajax({
		url : "/Login",
		type : "POST",
		async : false,
		data : {
			type : "showContactsCompany",
			company : d[1]
		},
		success : function(response) {
			var json = JSON.parse(response);
			dataTable = '<div class="panel panel-default">'
				+ '<div class="panel-heading"> Contacts de ' + d[2]
			+ '</div>' + '<div class="panel-body">';
			if (json.length == 0) {
				dataTable += '<p>Aucun contact actif enregistré pour cette entreprise</p>';
			} else {
				dataTable += '<table class="table table-striped table-bordered table-hover"><thead><tr><th>Prénom</th><th>Nom</th><th>Email</th><th>Téléphone</th><th></th></tr></thead><tbody>';
				$.each(json, function(val) {
					dataTable += '<tr><td>' + json[val].firstName
					+ '</td><td>' + json[val].lastName
					+ '</td><td>' + json[val].email
					+ '</td><td>' + json[val].phoneNumber
					+ '</td><td>' + toggleContactDraw(json[val].active, json[val].contactId, json[val].version)
					+ '</td></tr>';
				});
				dataTable += '</tbody></table>'
			}
			dataTable += "</div></div>";
		},
		error : function(response) {
			toastr.error("Erreur lors de l'affichage des contacts de l'entreprise : " + d[2]);
		}
	});
	return dataTable;
}

var selectEvent = function() {
	ajax("showResume", eventSelected, showObject.selectEventSuccess, showObject.selectEventError);
}

/**
 * Used to show the several participations encoded in the database
 */
var showParticipations = function() {
	closeAll();
	ajax("showParticipations", undefined, showObject.participationsSuccess, showObject.participationsError);
	$("#participations").show();
}
var showContacts = function() {
	closeAll();
	ajax("showContacts", undefined, showObject.contactsSuccess, showObject.contactsError);
	$("#contacts").show();
}
/*-
 * ------------------------------ END initiate navbar onclick-functions ------------------------------ 2
 */

/*-
 *  ------------------------------ START initiate content onclick-functions  ------------------------------ 3
 */

function Util() {

	function csvSuccess(response) {
		exportToCsv("JE_full.csv", response);
		toastr.success("Fichier CSV généré");
	}

	function csvError(response) {
		toastr.error(response.responseText);
	}

	function modifiedCsvSuccess(response) {
		exportToCsv("JE_modified.csv", response);
		toastr.success("Fichier CSV généré");
	}

	function modifiedCsvError(response) {
		toastr.error(response.responseText);
	}

	function addContactSuccess(response) {
		toastr.success("Contact ajouté !", "", {timeOut : 5000});
		emptyAddContactForm();
	}

	function addContactError(response) {
		toastr.warning(response.responseText);
	}

	function modifyContactSuccess(response) {
		toastr.success("Contact Modifié !", "", {timeOut : 5000});
		emptyAddContactForm();
	}

	function modifyContactError(response) {
		toastr.warning(response.responseText);
	}

	function addEventSuccess(response) {
		toastr.success("Ajout réussi !", "", {timeOut : 5000});
		$("#eventModalForm").modal('hide');
		$("#showDashboard").trigger('click');
		showParticipations();
		$("participations").hide();
	}

	function addEventError(response) {
		toastr.warning(response.responseText);
	}

	function saveStateSuccess(response) {
		showObject.participationsSuccess(response);
		toastr.success("Modifications enregistrées", "", {
			timeOut : 5000
		});
		participationsToModify = {};
		showParticipations();
	}

	function saveStateError(response) {
		toastr.error(response.responseText);
	}

	function setContactActivitySuccess(response) {
		toastr.success("Modifications enregistrées !");
		showContacts();
	}
	function setContactActivityError(response) {
		toastr.error(response.responseText);
	}

	function setContactActivityCompanySuccess(response) {
		toastr.success("L'activité du contact a bien été modifiée !");
		closeAll();
		ajax("showCompanies", undefined, showObject.companiesSuccess, showObject.companiesError);
		$("#companies").show();
	}

	function setContactActivityCompanyError(response) {
		toastr.error(response.responseText);
	}

	function saveAttendanceCancellingSuccess(response){
		toastr.success("Les présences ont bien été annulées ");
		AttendancesToCancel = [];
	}

	function saveAttendanceCancellingError(response){
		toastr.error(response.responseText);

	}

	function addCompanySuccess(response) {
		emptyAddCompanyForm();
		toastr.success("Ajout réussi !");
	}

	function addCompanyError(response) {
		toastr.warning(response.responsetext);
	}

	this.csvSuccess = csvSuccess;
	this.csvError = csvError;
	this.modifiedCsvSuccess = modifiedCsvSuccess;
	this.modifiedCsvError = modifiedCsvError;
	this.addCompanySuccess = addCompanySuccess;
	this.addCompanyError = addCompanyError;
	this.addContactSuccess = addContactSuccess;
	this.addContactError = addContactError;
	this.modifyContactSuccess = modifyContactSuccess;
	this.modifyContactError = modifyContactError;
	this.addEventSuccess = addEventSuccess;
	this.addEventError = addEventError;
	this.saveStateSuccess = saveStateSuccess;
	this.saveStateError = saveStateError;
	this.setContactActivityError = setContactActivityError;
	this.setContactActivitySuccess = setContactActivitySuccess;
	this.setContactActivityCompanyError = setContactActivityCompanyError;
	this.setContactActivityCompanySuccess = setContactActivityCompanySuccess;
	this.saveAttendanceCancellingSuccess = saveAttendanceCancellingSuccess;
	this.saveAttendanceCancellingError = saveAttendanceCancellingError;
}

/*-
 * Ajax call to prepare and send the csv file
 */
var initiateContentFunction = function() {
	$("#getCsvButton").click(function() {
		ajax("generateCsv", undefined, utilObject.csvSuccess, utilObject.csvError);
	});
	$("#getModifiedCsvButton").click(function() {
		ajax("generateModifiedCsv", undefined, utilObject.modifiedCsvSuccess, utilObject.modifiedCsvError);
	});
	$("#showParticipationsDashboard").click(function() {
		if ($("#participationsDashboard").is(":visible")){
			$("#participationsDashboard").hide();
		} else {
			ajax("showDashboardParticipations", undefined, showObject.participationsDashboardSuccess, showObject.participationsError);
			$("#participationsDashboard").show();
		}
	});
	/*-
	 * The button submit of the addContact form
	 */
	$("#form_addContactSubmit").click(function(e) {
		e.preventDefault();
		var manqueChamp = false;
		$("#form_addContact input").each(function(index, elt) {
			if ($(this).val() == null || $(this).val() == '') {
				if ($(this).attr('name') == "Email" && $("#form_addContactPhoneNumber").val() != null) {

				} else if ($(this).attr('name') == "PhoneNumber" && $("#form_addContactEmail").val() != null) {
					toastr.warning("Veuillez compléter tous les champs");
					manqueChamp = true;
					return false;
				}
			}
		});
		if (manqueChamp)
			return;
		var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		if (!re.test($("#form_addContactEmail").val()) && $("#form_addContactPhoneNumber").val().length < 1) {
			toastr.warning("Veuillez entrer une adresse email valide ou un numéro de téléphone");
			return false;
		}
		if ($("#form_addContactPhoneNumber").val().length > 15) {
			toastr.warning("Un numéro de téléphone valide ne comporte pas plus de 15 chiffres !","", {timeOut : 5000});
			return false;
		}
		if($("#form_addContactSubmit").html() === "Ajouter"){
			ajax("addNewContact", JSON.stringify(getNewContactInfo()), utilObject.addContactSuccess, utilObject.addContactError);
			var type = "addContact"
		} else {
			let json = getNewContactInfo();
			json.id = contactSelected["id"];
			json.version = contactSelected["version"];
			json.active = contactSelected["active"];
			ajax("modifyContact", JSON.stringify(json), utilObject.modifyContactSuccess, utilObject.modifyContactError);
		}
	});

	/*-
	 * New company click listener
	 *
	 * Does an Ajax call to send the new company form data after field
	 * verification
	 */
	$("#createCompanyFormSubmit").click(function(e) {
		e.preventDefault();
		// Checking for empty fields
		var missingField = false;
		$("#createCompanyForm input").each(function(index) {
			if ($(this).attr('id') != "createCompanyFormBox" && ($(this).val() == "" || $(this).val() == null)) {
				toastr.warning("Veuillez compléter le champ "+ $(this).attr('name'));
				missingField = true;
			}
		});
		if (missingField)
			return false;
		// Retrieving form data
		if(new Date($("#createCompanyFormDateFirstContact").val()) > new Date()) {
			toastr.warning("La date se situe dans le futur");
			return;
		}
		var newCompanyData = getNewCompanyInfo();
		// creating the request's data string
		ajax("addNewCompany", JSON.stringify(newCompanyData), utilObject.addCompanySuccess, utilObject.addCompanyError);
	});

	/*-
	 * Event creation action button
	 *
	 */
	$("#event_creation_btn").on("click", function() {
		let ajaxData;
		let date;
		$("#creation_event_form").find('input[type=date]').each(function(i, el) {
			ajaxData = $(el).val();
			date = new Date(ajaxData);
		});
		$("#creation_event_form")[0].reset();
		if (date !== undefined) {
			var today = new Date();
			if (date < today) {
				toastr.warning("Problème d'ajout : la date doit se situer dans le futur !");
			} else {
				ajax("addNewEvent", ajaxData, utilObject.addEventSuccess, utilObject.addEventError);
			}
		} else {
			toastr.warning("Le date est invalide");
		}
	});
	/*
	 * this fill up the list of participations which are waiting for a state
	 * changing
	 */
	$("#participationsTable").on('change', 'tr select', function(event) {
		if ($(this).find('option:selected').attr("name") != "unchanged") {
			var info={};
			info.state=$(this).find('option:selected').attr("name");
			info.version=$(this).attr("version");
			info.event = eventSelected.toString();
			participationsToModify[$(this).attr("id")] = info;
			toastr.info("N'oubliez pas de sauver");
		}
	});
	/*
	 * This validate the list of participation which are waiting for a state
	 * changing to the server
	 */
	// TODO
	$("#saveStates").click(function() {
		if( AttendancesToCancel.length !== 0 ){
			ajax("editConfirmationAttendance",  JSON.stringify(AttendancesToCancel),  utilObject.saveAttendanceCancellingSuccess, utilObject.saveAttendanceCancellingError);

		}

		ajax("editStateParticipation", JSON.stringify(participationsToModify), utilObject.saveStateSuccess, utilObject.saveStateError)
	});

	$("#saveContacts").click(function() {
		ajax("setContactActivity", JSON.stringify(contactsToModify), utilObject.setContactActivitySuccess,utilObject.setContactActivityError);
		contactsToModify = {};
	});

	$("#saveContactsCompany").click(function() {
		ajax("setContactActivity", JSON.stringify(contactsToModify), utilObject.setContactActivitySuccess,utilObject.setContactActivityError);
		contactsToModify = {};
	});

	/*
	 * This marks a contact as active or not active
	 *
	 */
	$("#allContactsTable").on('click', 'tr', function(event) {
		contactSelected = {};
		contactSelected["id"] = $('td .toggle input', $(this)).eq(0).attr("id");
		contactSelected["version"] = $('td .toggle input', $(this)).eq(0).attr("version");
		contactSelected["company"] = $('td', $(this)).eq(0).html();
		contactSelected["companyId"] = contactsTable.row( this ).data()[7];
		contactSelected["firstname"]  = $('td', $(this)).eq(1).html();
		contactSelected["name"] =  $('td', $(this)).eq(2).html();
		contactSelected["email"] =  $('td', $(this)).eq(3).html();
		contactSelected["tel"] = $('td', $(this)).eq(4).html();
		contactSelected["active"] =  $('td .toggle input', $(this)).eq(0).is(":checked");
		if (event.target.outerHTML == '<label class="btn btn-default active toggle-off">A demissionné</label>'
			|| event.target.outerHTML == '<label class="btn btn-primary toggle-on">Travail Actuel</label>'
				|| event.target.outerHTML == '<span class="toggle-handle btn btn-default"></span>') {
			contactsToModify[contactSelected["id"]] = contactSelected;
			toastr.info("N'oubliez pas de sauver");
		} else if(event.target.outerHTML == '<button name="setContactInfo" type="button" class="btn btn-outline btn-info">Modifier</button>') {
			addContact = true;
			closeAll();
			ajax("getCompaniesForContacts", null, showObject.addContactSuccess, showObject.addContactError);
			$("#newContact").show();
		}
	});

	$("#allCompaniesTable tbody").on("click", 'tr td tr', function(event) {
		if(event.target.outerHTML != '<label class="btn btn-primary toggle-on">Travail Actuel</label>'
			&& event.target.outerHTML != '<label class="btn btn-default active toggle-off">A demissionné</label>') {
			return;
		}
		contactSelected = {};
		contactSelected["id"] = $('td input', $(this)).eq(0).attr("id");
		contactSelected["version"] = $('td input', $(this)).eq(0).attr("version");
		//contactSelected["company"] = $('td', $(this)).eq(0).html();
		contactSelected["firstname"]  = $('td', $(this)).eq(0).html();
		contactSelected["name"] =  $('td', $(this)).eq(1).html();
		contactSelected["email"] =  $('td', $(this)).eq(2).html();
		contactSelected["tel"] = $('td', $(this)).eq(3).html();
		contactSelected["active"] =  $('td input', $(this)).eq(0).is(':checked');
		contactsToModify[contactSelected["id"]] = contactSelected;
		toastr.info("N'oubliez pas de sauver");
	});

	$("#participationsTable").on("click", function(event) {
		if(event.target.className == "attendanceCheckbox") {
			confirmationAttendance(event.target);
		}
	})

	$("#selectAllInvitations").click(function(){
		$("#companiesToInviteTable :checkbox").each(function(index) {
			$(this).prop("checked", true);
		}); 
	});

}

/*-
 * ------------------------------ END initiate content onclick-functions  ------------------------------ 3
 */

/*
 * ------------------------------ START redirection functions
 * ------------------------------------------5
 */

var redirection = function() {

	$("#eventShowAllButton").on("click", function() {
		closeAll();
		ajax("showAllEventParticipations", eventSelected, showObject.participationsSuccess, showObject.participationsError);
		$("#participations").show();
	});

	$("#dashboardBody").on("click", "p", function(){
		closeAll();
		let type;
		switch($(this).attr('id')) {
		case "invitedParticipations":
			type = "showInvitedEventParticipations";
			break;
		case "refusedParticipations":
			type = "showRefusedEventParticipations";
			break;
		case "confirmedParticipations":
			type = "showConfirmedEventParticipations";
			break;
		case "invoicedParticipations":
			type = "showInvoicedEventParticipations";
			break;
		case "cancelledParticipations":
			type = "showCancelledEventParticipations";
			break;
		case "paidParticipations":
			type = "showPaidEventParticipations";
			break;
		}
		ajax(type, eventSelected, showObject.participationsSuccess, showObject.participationsError);
		$("#participations").show();
	});
};

/*
 * ------------------------------ END redirection functions
 * ---------------------------------------------- 5
 */

/*-
 *  ------------------------------ START utility functions ------------------------------- 4
 */

/*-
 * Used to automatize the ajax call.
 */
var ajax = function(type, json, successFunction, errorFunction) {
	$.ajax({
		url : "/Login",
		type : "POST",
		dataType : "json",
		data : {
			type : type,
			json : json
		},
		success : function(response) {
			if (successFunction != undefined) {
				successFunction(response);
			}
		},
		error : function(response) {
			switch (response.status) {
			case 400:
			case 401:
			case 403:
			case 409:
				if(type != "init" && type != "login") {
					toastr.warning(response.responseText);
				} else {
					if (errorFunction != undefined) {
						errorFunction(response);
					}
				}
				break;
			case 500:
				let html;
				$("body").css("backgroundColor", "#fafafa")
				// html = "<p>Serveur non disponible pour le moment, veuilliez
				// réitérer votre demande dans quelques instants </p>";
				html = "<img class='error500' src='pictures/error_500.png'/>"
					$("body").html(html)
					break;
			default :
				if (errorFunction != undefined) {
					errorFunction(response);
				}
			}
		}
	});
}

/*-
 * Used to initialize the scripts and set up the application.
 */
var pageInit = function() {
	/* Initialisation of the datatables */
	$(document).ready(function() {
		companiesTable = $('#allCompaniesTable').DataTable({
			"createdRow" : function(row, data, index) {
				$('td', row).eq(0).addClass('details-control');
				$('td', row).eq(1).addClass('hidden');
			}
		});
		companiesToInviteTable = $('#companiesToInviteTable').DataTable({
			"columnDefs" : [ {
				"targets" : [ 6 ],
				"visible" : false,
				"searchable" : false
			} ]
		});
		contactsTable = $('#allContactsTable').DataTable({
			"createdRow" : function(row, data, index) {
				var column = contactsTable.column(7);
				column.visible(false);
				$('th', column).addClass("hidden");
				$('th', row).eq(7).addClass("hidden");
				$('td', row).eq(7).addClass("hidden");
				if(!isManager) {
					let column = contactsTable.column(6);
					column.visible(false);
					$('th', row).eq(6).addClass("hidden");
					$('td', row).eq(6).addClass('hidden');
				}
			},
			"columns": [
				{"name": "Nom de l'entreprise", "orderable": true},
				{"name": "Prénom", "orderable": true},
				{"name": "Nom", "orderable": true},
				{"name": "Email", "orderable": true},
				{"name": "Téléphone", "orderable": true},
				{"name": "Actif", "orderable": false},
				{"name": "", "orderable": false},
				{"name": "idCompany", "orderable" : false}
				],
				"fnDrawCallback": function() {
					$('.toggleContact').bootstrapToggle({
						on: 'Travail Actuel',
						off: 'A demissionné'
					});
				},
				"responsive": true
		});
		participationsTable = $("#participationsTable").DataTable({
			"createdRow" : function(row, data, index) {
				$('td', row).eq(0).addClass('participations-control');
				$('td', row).eq(1).addClass('hidden');
				$('td', row).addClass(data[5]);
			}
		});
		participationsDashboardTable = $("#participationsDashboardTable").DataTable({
			"createdRow" : function(row, data, index) {
				$('td', row).eq(0).addClass('participations-control');
				$('td', row).eq(1).addClass('hidden');
				$('td', row).addClass(data[6]);
			},
		"order": [[ 2, 'asc' ]]
		});
	});

	/* Initialisation of the info box */
	toastr.options = {
			"closeButton" : false,
			"debug" : false,
			"newestOnTop" : false,
			"progressBar" : false,
			"positionClass" : "toast-top-right",
			"preventDuplicates" : false,
			"onclick" : null,
			"showDuration" : "300",
			"hideDuration" : "1000",
			"timeOut" : "2000",
			"extendedTimeOut" : "1000",
			"showEasing" : "swing",
			"hideEasing" : "linear",
			"showMethod" : "fadeIn",
			"hideMethod" : "fadeOut"
	}
	$("#nav").hide();
	$("#content").hide();
	closeAll();
	$("#accueil").show();
	$("#loginInfo").hide();
}

/*-
 * Used to close the several divs related to the application.
 */
var closeAll = function() {
	$("#dashboard").hide();
	$("#companies").hide();
	$("#contacts").hide();
	$("#newCompany").hide();
	$("#newContact").hide();
	$("#companiesToInvite").hide();
	$("#participations").hide();
};

/*
 * Hide things you can't access if you're not manager
 *
 * TODO : Gérer les cas où on accède aux pages de manière dérivée. (Redirection
 * ou autre)
 */
var hideIfNotManager = function() {
	if (!isManager) {
		$("#showCompaniesToInvite").hide();
		$("#getCsvButton").hide();
		$("#getModifiedCsvButton").hide();
	}
}

/*-
 * Function to export csv file. The first parameter is the name of the file to
 * create, the second is the json map to print.
 */
function exportToCsv(filename, myDatas) {
	var csvFile = 'Nom Entreprise;Prénom Contact;Nom Contact;Email Contact;Téléphone Contact;\n';
	$.each(myDatas, function(index, value) {
		csvFile += value + "\n";
	});

	var blob = new Blob([ csvFile ], {
		type : 'csv;charset=utf-8;'
	});
	if (navigator.msSaveBlob) { // IE 10+
		navigator.msSaveBlob(blob, filename);
	} else {
		var link = document.createElement("a");
		if (link.download !== undefined) { // feature detection
			// Browsers that support HTML5 download attribute
			var url = URL.createObjectURL(blob);
			link.setAttribute("href", url);
			link.setAttribute("download", filename);
			link.style.visibility = 'hidden';
			document.body.appendChild(link);
			link.click();
			document.body.removeChild(link);
		}
	}
}

/*-
 * --------------- START : HIDE AND SHOW table functions --------------- 4.1
 */
function hideCompaniesTable() {
	$('#companiesTable').hide();
}
function showCompaniesTable() {
	$('#companiesTable').show();
}
function hidecompaniesToInviteTable() {
	$('#companiesToInviteTable').hide();
}
function showcompaniesToInviteTable() {
	$('#companiesToInviteTable').show();
}
function hideContactsTable() {
	$('#contactsTable').hide();
}
function showContactsTable() {
	$('#contactsTable').show();
}

/*-
 *  --------------- END : HIDE AND SHOW table functions --------------- 4.1
 */

/*-
 * --------------- START : EMPTY FORM functions --------------- 4.2
 */

var emptyRegisterForm = function() {
	$("#form_register input").each(function(index, elt) {
		$(this).val("");
	});
}
var emptyAddCompanyForm = function() {
	$("#createCompanyForm input").each(function() {
		$(this).val("");
	});
}
var emptyAddContactForm = function() {
	$("#form_addContact input").each(function() {
		$(this).val("");
	});
}

/*-
 * --------------- END : EMPTY FORM functions --------------- 4.2
 */

/*-
 * --------------- START : GET INFOS functions --------------- 4.3
 */

function getNewContactInfo() {
	var form_addContact = {};
	form_addContact["idCompany"] = $("#form_addContactCompany").val();
	form_addContact["firstname"] = $("#form_addContactFirstName").val();
	form_addContact["lastname"] = $("#form_addContactLastName").val();
	form_addContact["email"] = $("#form_addContactEmail").val();
	form_addContact["phonenumber"] = $("#form_addContactPhoneNumber").val();
	return form_addContact;
}

function getRegisterInfo() {
	var dataRegister = {};
	dataRegister.email = $("#email").val();
	dataRegister["username"] = $("#pseudo_register").val();
	dataRegister["firstname"] = $("#firstname_register").val();
	dataRegister["lastname"] = $("#lastname_register").val();
	dataRegister["password"] = $("#password_register").val();
	return dataRegister;
}

/*-
 * Used to translate form data into an AJAX object when creating a new company
 */
function getNewCompanyInfo() {
	var newCompanyData = {};
	newCompanyData["name"] = $("#createCompanyFormName").val();
	newCompanyData["dateFirstContact"] = $("#createCompanyFormDateFirstContact")
	.val();
	newCompanyData["street"] = $("#createCompanyFormStreet").val();
	newCompanyData["number"] = $("#createCompanyFormNumber").val();
	newCompanyData["box"] = $("#createCompanyFormBox").val();
	newCompanyData["postalCode"] = $("#createCompanyFormPostalCode").val();
	newCompanyData["municipality"] = $("#createCompanyFormMunicipality").val();
	return newCompanyData;
}

function setSelectStateParticipation(idPart,version, currentState) {
	html = "<div class=\"form-group\"><select name='stateSelector' class=\"form-control "
		+ currentState + "\" id=" + idPart + " version="+version+"> ";
	if (currentState == "invited")
		html += "<option name='unchanged' selected>Invitée</option> "
			+ "<option name='confirmed' >Confirmée</option> "
			+ "<option name='refused'>Refusée</option>";
	else if (currentState == "paid")
		html += "<option name='unchanged' selected>Payée</option> "
			+ "<option name='cancelled' >Annulée</option>"
			else if (currentState == "invoiced")
				html += "<option name='unchanged' selected>Facturée</option> "
					+ "<option name='cancelled'>Annulée</option>"
					+ "<option name='paid'>Payée</option> ";
			else if (currentState == "confirmed")
				html += "<option name='unchanged' selected>Confirmée</option> "
					+ "<option name='cancelled' >Annulée</option>"
					+ "<option name='invoiced' >Facturée</option> "
					else if (currentState == "refused")
						html += "<option name='unchanged' selected>Refusée</option> "
							else if (currentState == "cancelled")
								html += "<option name='unchanged' selected>Annulée</option> "
									html += "</select></div>"
										return html;

}
/**
 * This function is used to draw the button to show wheter a contact is active
 * and allow the user to mark him a not active
 *
 */
function toggleContactDraw(isActive, idContact, version){
	if(isActive)
		return '<input class="toggleContact" data-toggle="toggle" checked version='+version+' id='+idContact+' type="checkbox">';
	else
		return '<input class="toggleContact" data-toggle="toggle" version='+version+' id='+idContact+' type="checkbox">';
};

function buttonSetContact() {
	if(isManager) {
		return "<button name='setContactInfo' type='button' class='btn btn-outline btn-info'>Modifier</button>"
	} else {
		return "";
	}
}

/*-
 *  --------------- END : GET INFOS functions --------------- 4.3
 */

/*-
 * ------------------------------ END utility functions  ------------------------------- 4
 */

/*-
 * ------------------------------ START : UNSORTED THINGS ------------------------------ 5
 */
//Use this if you're too lazy, instead of putting it anywhere !
/*-
 * ------------------------------ END : UNSORTED THINGS  ------------------------------ 5
 */

function  confirmationAttendance(event) {
	// this will contain a reference to the checkbox
	var row = $(event).closest('tr');
	var attendance = parseInt($(row).attr("name"));
	var indiceAttandance =  AttendancesToCancel.indexOf(attendance);
	if (event.checked) {
		if(indiceAttandance === -1){
			AttendancesToCancel.push(attendance);
			row.css("backgroundColor", "#F5A9A9");
		}
	} else {
		if(indiceAttandance !== -1){
			delete AttendancesToCancel[indiceAttandance];
			row.css("backgroundColor", "white");
		}
	}
	toastr.info("N'oubliez pas de sauver");
}


/*-
 *  ------------------------------ START : Useless but fun functions ------------------------------ 6
 */

var easterEgg1 = function() {
	/*
	 * JEM Easter Egg (To keep or to throw away)
	 */
	$('html').on("keyup", function(event) {
		if (event.which === 222) /* 222 === ² */
			codeSecret = "²";
		if (event.which === 74) /* 74 === 'j' */
			codeSecret += "j";
		if (event.which === 69) /* 69 === 'e' */
			codeSecret += "e";
		if (event.which === 77) /* 77 === 'm' */
			codeSecret += "m";
		if (codeSecret === "²jem")
			window.open("https://www.youtube.com/watch?v=m6G_o1MYECg");
	});
}

/*-
 * ------------------------------ END : Useless but fun functions ------------------------------ 6
 */
