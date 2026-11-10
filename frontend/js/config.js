// export const API_BASE_URL =
//   window.HSMS_CONFIG?.apiBaseUrl ?? "http://localhost:8081/api";
  export const API_BASE_URL =
  (window.HSMS_CONFIG?.apiBaseUrl ?? "http://localhost:8081").replace(/\/+$/, "");
