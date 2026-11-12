import { apiRequest } from "../api/client.js";
import { clearNotice, createDataRow, renderRows, setFormBusy, showNotice } from "../ui.js";

const recentAppointments = [];
let sessionVersion = 0;

function formatDateTime(dateTime) {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(dateTime));
}

function renderRecentAppointments() {
  renderRows(
    document.querySelector("#appointments-list"),
    recentAppointments,
    "No bookings have been made during this session.",
    (appointment) =>
      createDataRow(
        `${appointment.patientFirstName} ${appointment.patientLastName} with Dr. ${appointment.doctorFirstName} ${appointment.doctorLastName}`,
        formatDateTime(appointment.appointmentDateTime),
      ),
  );
}

export function initAppointments() {
  const form = document.querySelector("#appointment-form");
  const feedback = document.querySelector("#appointments-feedback");
  const dateTimeInput = form.elements.appointmentDateTime;
  // const setDateTimeMinimum = () => {
  //   const now = new Date();
  //   now.setSeconds(0, 0);
  //   now.setMinutes(now.getMinutes() + 1);
  //   const localNow = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  //   dateTimeInput.min = localNow.toISOString().slice(0, 16);
  // };

  // setDateTimeMinimum();
  // dateTimeInput.addEventListener("focus", setDateTimeMinimum);
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearNotice(feedback);
    // setDateTimeMinimum();
    const formData = new FormData(form);
    console.log("Form data submitted:", formData);
    const appointmentDateTime = String(formData.get("appointmentDateTime"));
    const patientId = Number(formData.get("patientId"));
    const doctorId = Number(formData.get("doctorId"));
    if (!Number.isInteger(patientId) || patientId <= 0) {
      showNotice(feedback, "Select a patient before booking an appointment.", "error");
      return;
    }
    setFormBusy(form, true);
    const payload = {
      patientId,
      doctorId,
      appointmentDateTime,
    };
    const bookingSessionVersion = sessionVersion;
    let appointment;

    try {
      appointment = await apiRequest("/api/appointments", {
        method: "POST",
        body: JSON.stringify(payload),
      });
    } catch (error) {
      if (bookingSessionVersion === sessionVersion) {
        showNotice(feedback, error.message, "error");
      }
      return;
    } finally {
      setFormBusy(form, false);
    }

    if (bookingSessionVersion !== sessionVersion) return;
    form.elements.appointmentDateTime.value = "";
    showNotice(feedback, "Appointment Booked Successfully !!", "success");
    if (appointment && typeof appointment === "object") {
      recentAppointments.unshift(appointment);
      try {
        renderRecentAppointments();
      } catch (error) {
        console.error("Could not render the recent appointment list.", error);
      }
    }
  });
  renderRecentAppointments();
}

export function clearAppointments() {
  sessionVersion += 1;
  recentAppointments.length = 0;
  renderRecentAppointments();
  clearNotice(document.querySelector("#appointments-feedback"));
}
