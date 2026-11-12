import { clearDoctorsData, initDoctors, loadDoctors } from "./features/doctors.js";
import { clearPatientData, initPatients, loadPatients, loadPatientOptions } from "./features/patients.js";
import { clearAppointments, initAppointments } from "./features/appointments.js";
import { clearNotice, setFormBusy, showNotice } from "./ui.js";

const loginView = document.querySelector("#login-view");
const appView = document.querySelector("#app-view");
const loginMessage = document.querySelector("#login-message");
const userBadge = document.querySelector("#user-badge");
const loginForm = document.querySelector("#login-form");

function showTab(tabName) {
  document.querySelectorAll(".view-panel").forEach((panel) => {
    panel.hidden = panel.id !== `${tabName}-panel`;
  });
  document.querySelectorAll(".tab-button").forEach((button) => {
    const active = button.dataset.tab === tabName;
    button.classList.toggle("is-active", active);
    button.setAttribute("aria-current", active ? "page" : "false");
  });
}

function showApp(email) {
  loginView.hidden = true;
  appView.hidden = false;
  userBadge.textContent = email.split("@")[0];
  showTab("doctors");
  void loadDoctors();
  void loadPatients();
  void loadPatientOptions();
}

loginForm.addEventListener("submit", (event) => {
  event.preventDefault();
  clearNotice(loginMessage);
  const email = String(new FormData(loginForm).get("email")).trim();
  const password = String(new FormData(loginForm).get("password")).trim();
  if (!email || !password) {
    showNotice(loginMessage, "Enter an email and password to continue.", "error");
    return;
  }
  showApp(email);
});

document.querySelector("#logout-button").addEventListener("click", () => {
  appView.hidden = true;
  loginView.hidden = false;
  loginForm.reset();
  const doctorForm = document.querySelector("#doctor-form");
  const patientForm = document.querySelector("#patient-form");
  const appointmentForm = document.querySelector("#appointment-form");
  doctorForm.reset();
  patientForm.reset();
  appointmentForm.reset();
  [doctorForm, patientForm, appointmentForm].forEach((form) => setFormBusy(form, false));
  clearDoctorsData();
  clearPatientData();
  clearAppointments();
  document.querySelectorAll(".notice").forEach(clearNotice);
});

document.querySelectorAll(".tab-button").forEach((button) => {
  button.addEventListener("click", () => {
    const leavingAppointments =
      !document.querySelector("#appointments-panel").hidden
      && button.dataset.tab !== "appointments";
    if (leavingAppointments) {
      document.querySelector("#appointment-form").reset();
      clearNotice(document.querySelector("#appointments-feedback"));
    }
    showTab(button.dataset.tab);
    if (button.dataset.tab === "appointments") void loadPatientOptions();
  });
});

document.querySelectorAll("[data-refresh]").forEach((button) => {
  button.addEventListener("click", () => {
    if (button.dataset.refresh === "doctors") void loadDoctors();
    if (button.dataset.refresh === "patients") {
      void loadPatients();
      void loadPatientOptions();
    }
  });
});

initDoctors();
initPatients();
initAppointments();
