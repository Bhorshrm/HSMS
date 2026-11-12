import { apiRequest } from "../api/client.js";
import { createDataRow, initPhoneValidation, renderRows, setFormBusy, showToast } from "../ui.js";

let patientLoadVersion = 0;
let patientSize = 10;
let patientPage = 0;

function updatePatientPagination(data) {
  const previous = document.querySelector("#patients-prev");
  const next = document.querySelector("#patients-next");
  const pageInfo = document.querySelector("#patients-page-info");
  previous.disabled = data.first || data.totalPages === 0;
  next.disabled = data.last || data.totalPages === 0;
  pageInfo.textContent = data.totalElements === 0
    ? "No patients"
    : `Page ${data.number + 1} of ${data.totalPages} · ${data.totalElements} patients`;
}

export async function loadPatients(page = patientPage) {
  const loadVersion = patientLoadVersion;
  const container = document.querySelector("#patients-list");
  container.innerHTML = '<p class="empty-state">Loading patients...</p>';
  try {
    const params = new URLSearchParams({
      page: String(page),
      size: String(patientSize),
      sort: "id,desc",
    });
    const data = await apiRequest(`/api/patients?${params}`);
    if (loadVersion !== patientLoadVersion) return;
    patientPage = data.number;
    updatePatientPagination(data);
    renderRows(
      container,
      data.content,
      "No patients found. Add a patient above.",
      (patient) =>
        createDataRow(
          `${patient.firstName} ${patient.lastName}`,
          patient.email ?? `Patient #${patient.id}`,
        ),
    );
  } catch (error) {
    if (loadVersion !== patientLoadVersion) return;
    renderRows(container, [], `Could not load patients: ${error.message}`, () => null);
    document.querySelector("#patients-page-info").textContent = "Could not load pages";
    document.querySelector("#patients-prev").disabled = true;
    document.querySelector("#patients-next").disabled = true;
  }
}

export async function loadPatientOptions() {
  const select = document.querySelector('#appointment-form select[name="patientId"]');
  const hint = document.querySelector("#patient-options-hint");
  const loadVersion = patientLoadVersion;
  const setPlaceholder = (text, disableSelect, showHint = false) => {
    const option = new Option(text, "", true, true);
    option.disabled = true;
    select.replaceChildren(option);
    select.disabled = disableSelect;
    hint.hidden = !showHint;
  };
  setPlaceholder("Loading patients...", true);
  try {
    const patients = await apiRequest("/api/patients/options");
    if (loadVersion !== patientLoadVersion) return;
    if (patients.length === 0) {
      setPlaceholder("No patients available", true, true);
      return;
    }
    const placeholder = new Option("Select patient", "", true, true);
    select.replaceChildren(placeholder);
    select.disabled = false;
    hint.hidden = true;
    patients.forEach((patient) => {
      select.add(new Option(
        `${patient.firstName} ${patient.lastName} (#${patient.id})`,
        patient.id,
      ));
    });
  } catch (error) {
    if (loadVersion !== patientLoadVersion) return;
    setPlaceholder(`Could not load patients: ${error.message}`, true);
  }
}

export function clearPatientData() {
  patientLoadVersion += 1;
  document.querySelector("#patients-list").replaceChildren();
  const select = document.querySelector('#appointment-form select[name="patientId"]');
  const placeholder = new Option("Select patient", "", true, true);
  placeholder.disabled = true;
  select.replaceChildren(placeholder);
  select.disabled = false;
  document.querySelector("#patient-options-hint").hidden = true;
}

export function initPatients() {
  const form = document.querySelector("#patient-form");
  const toast = document.querySelector("#global-message");
  initPhoneValidation(form.elements.phone);
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    setFormBusy(form, true);
    const submitVersion = patientLoadVersion;
    const patient = Object.fromEntries(new FormData(form));
    for (const key of ["email", "phone", "dateOfBirth"]) {
      if (!patient[key]) delete patient[key];
    }

    try {
      await apiRequest("/api/patients", {
        method: "POST",
        body: JSON.stringify(patient),
      });
    } catch (error) {
      if (submitVersion === patientLoadVersion) showToast(toast, error.message, "error");
      return;
    } finally {
      setFormBusy(form, false);
    }

    if (submitVersion !== patientLoadVersion) return;
    form.reset();
    showToast(toast, "Patient Added", "success");
    void loadPatients(0);
    void loadPatientOptions();
  });

  document.querySelector("#patients-prev")
    .addEventListener("click", () => loadPatients(patientPage - 1));
  document.querySelector("#patients-next")
    .addEventListener("click", () => loadPatients(patientPage + 1));
  document.querySelector("#patients-size").addEventListener("change", (event) => {
    patientSize = Number(event.target.value);
    void loadPatients(0);
  });
}