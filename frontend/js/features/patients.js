import { apiRequest } from "../api/client.js";
import { clearNotice, createDataRow, renderRows, setFormBusy, showNotice } from "../ui.js";

let patientLoadVersion = 0;

export async function loadPatients() {
  const loadVersion = patientLoadVersion;
  const container = document.querySelector("#patients-list");
  container.innerHTML = '<p class="empty-state">Loading patients...</p>';
  try {
    const patients = await apiRequest("/api/patients");
    if (loadVersion !== patientLoadVersion) return;
    renderRows(
      container,
      patients,
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
    const patients = await apiRequest("/api/patients");
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
  clearNotice(document.querySelector("#patients-feedback"));
  const select = document.querySelector('#appointment-form select[name="patientId"]');
  const placeholder = new Option("Select patient", "", true, true);
  placeholder.disabled = true;
  select.replaceChildren(placeholder);
  select.disabled = false;
  document.querySelector("#patient-options-hint").hidden = true;
}

export function initPatients() {
  const form = document.querySelector("#patient-form");
  const feedback = document.querySelector("#patients-feedback");
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearNotice(feedback);
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
      if (submitVersion === patientLoadVersion) showNotice(feedback, error.message, "error");
      return;
    } finally {
      setFormBusy(form, false);
    }

    if (submitVersion !== patientLoadVersion) return;
    form.reset();
    showNotice(feedback, "Patient added.", "success");
    void loadPatients();
    void loadPatientOptions();
  });
}
