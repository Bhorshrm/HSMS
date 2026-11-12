const noticeTimeouts = new WeakMap();
const noticeFadeTimeouts = new WeakMap();

export function showNotice(element, message, kind = "error") {
  const readableMessage =
    typeof message === "string" && message.trim()
      ? message
      : "Something went wrong. Please try again.";
  element.textContent = readableMessage;
  element.className = `notice notice-${kind}`;
  element.hidden = false;
}

export function clearNotice(element) {
  window.clearTimeout(noticeTimeouts.get(element));
  window.clearTimeout(noticeFadeTimeouts.get(element));
  noticeTimeouts.delete(element);
  noticeFadeTimeouts.delete(element);
  element.textContent = "";
  element.className = "notice";
  element.hidden = true;
}

export function initPhoneValidation(input) {
  const error = document.getElementById(input.getAttribute("aria-describedby"));
  const updateValidation = () => {
    const invalid = input.validity.patternMismatch;
    input.setCustomValidity(invalid ? input.title : "");
    input.setAttribute("aria-invalid", invalid ? "true" : "false");
    error.hidden = !invalid;
  };

  input.addEventListener("input", updateValidation);
  input.addEventListener("blur", updateValidation);
  updateValidation();
}

export function showToast(element, message, kind = "success") {
  clearNotice(element);
  showNotice(element, message, kind);
  element.classList.add("toast");
  noticeFadeTimeouts.set(
    element,
    window.setTimeout(() => element.classList.add("toast-fading"), 3000),
  );
  noticeTimeouts.set(element, window.setTimeout(() => clearNotice(element), 4000));
}

export function setFormBusy(form, busy) {
  const button = form.querySelector('button[type="submit"]');
  if (!button) return;
  button.disabled = busy;
  button.dataset.originalText ??= button.textContent;
  button.textContent = busy ? "Please wait..." : button.dataset.originalText;
}

export function renderRows(container, rows, emptyMessage, renderRow) {
  container.replaceChildren();
  if (rows.length === 0) {
    const empty = document.createElement("p");
    empty.className = "empty-state";
    empty.textContent = emptyMessage;
    container.append(empty);
    return;
  }
  rows.forEach((row) => container.append(renderRow(row)));
}

export function createDataRow(primaryText, secondaryText) {
  const row = document.createElement("div");
  row.className = "data-row";
  const primary = document.createElement("span");
  primary.textContent = primaryText;
  const secondary = document.createElement("small");
  secondary.textContent = secondaryText;
  row.append(primary, secondary);
  return row;
}
