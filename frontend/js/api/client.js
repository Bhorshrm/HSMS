import { API_BASE_URL } from "../config.js";

export async function apiRequest(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        Accept: "application/json",
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers,
      },
    });
  } catch (error) {
    console.error("Fetch failed:", error, "URL:", `${API_BASE_URL}${path}`);
    throw new Error("Can't reach the server. Please try again.");
  }

  if (response.status === 204) return null;

  const contentType = response.headers.get("content-type") ?? "";
  const responseText = await response.text();
  let body = responseText;
  if (contentType.includes("application/json") && responseText) {
    try {
      body = JSON.parse(responseText);
    } catch {
      body = responseText;
    }
  } else if (!responseText) {
    body = null;
  }

  if (!response.ok) {
    const messageCandidates =
      typeof body === "string"
        ? [body]
        : body && typeof body === "object"
          ? [body.message, body.detail, body.title]
          : [];
    const message = messageCandidates.find(
      (candidate) => typeof candidate === "string" && candidate.trim(),
    );
    throw new Error(message?.trim() || `Request failed (${response.status}). Please try again.`);
  }

  return body;
}
