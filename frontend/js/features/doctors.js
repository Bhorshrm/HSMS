import { apiRequest } from "../api/client.js";
import {
  clearNotice,
  createDataRow,
  initPhoneValidation,
  renderRows,
  setFormBusy,
  showNotice,
} from "../ui.js";

let doctorLoadVersion = 0;

export async function loadDoctors() {
  const loadVersion = doctorLoadVersion;
  const container = document.querySelector("#doctors-list");
  container.innerHTML = '<p class="empty-state">Loading doctors...</p>';
  try {
    const doctors = await apiRequest("/api/doctors");
    console.log("Doctors loaded:", doctors);
    if (loadVersion !== doctorLoadVersion) return;

    // Populate appointment doctor dropdown
    populateDoctorDropdown(doctors);

    renderRows(
      container,
      doctors,
      "No doctors found. Add a doctor above.",
      (doctor) =>
        createDataRow(
          `Dr. ${doctor.firstName} ${doctor.lastName}`,
          `${doctor.specialty} · ${doctor.doctorType ?? "Type not set"}`,
        ),
    );
  } catch (error) {
    if (loadVersion !== doctorLoadVersion) return;
    renderRows(container, [], `Could not load doctors: ${error.message}`, () => null);
  }
}

export function clearDoctorsData() {
  doctorLoadVersion += 1;
  document.querySelector("#doctors-list").replaceChildren();
  clearNotice(document.querySelector("#doctors-feedback"));
}

export function initDoctors() {
  const form = document.querySelector("#doctor-form");
  const feedback = document.querySelector("#doctors-feedback");
  initPhoneValidation(form.elements.phone);
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearNotice(feedback);
    setFormBusy(form, true);
    const submitVersion = doctorLoadVersion;
    const formData = new FormData(form);
    const doctor = Object.fromEntries(formData);
    for (const key of ["email", "phone"]) {
      if (!doctor[key]) delete doctor[key];
    }

    try {
      await apiRequest("/api/doctors", {
        method: "POST",
        body: JSON.stringify(doctor),
      });
    } catch (error) {
      if (submitVersion === doctorLoadVersion) showNotice(feedback, error.message, "error");
      return;
    } finally {
      setFormBusy(form, false);
    }

    if (submitVersion !== doctorLoadVersion) return;
    form.reset();
    showNotice(feedback, "Doctor added.", "success");
    void loadDoctors();
  });
}

export function populateDoctorDropdown(doctors) {
    const doctorSelect = document.querySelector(
        'select[name="doctorId"]'
    );

    if (!doctorSelect) return;

    doctorSelect.innerHTML = `
        <option value="" disabled selected>Select doctor</option>
    `;

    doctors.forEach((doctor) => {
        const option = document.createElement("option");

        option.value = doctor.id;
        option.textContent =
            `Dr. ${doctor.firstName} ${doctor.lastName} (${doctor.specialty} · ${
              doctor.doctorType === "LIMITED_HOURS" ? "2:00-4:00 PM" : "9:00 AM-6:30 PM"
            })`;

        doctorSelect.appendChild(option);
    });
    console.log("Doctor dropdown populated with doctors:", doctors);
}