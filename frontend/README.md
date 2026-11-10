# HSMS frontend

Plain HTML, CSS, and JavaScript frontend for the Spring Boot API. No build step or npm dependencies are required.

## Run locally

1. Start Spring Boot on `http://localhost:8080` and make sure its configured database is available.
2. Serve the frontend from the repository root. In VS Code, open `frontend/index.html` and choose **Open with Live Server** (configure its port as `5500`), or use Python if installed:

   ```powershell
   python -m http.server 5500 --directory frontend
   ```

3. Open `http://localhost:5500` in Chrome. Use any non-empty email and password for the **demo-only** sign-in.
4. Open `http://localhost:5500/?view=staff` to see the staff layout without admin tabs.

The login is only a visual demo; it does not authenticate users or provide authorization. Do not use it as production authentication.

## API mapping

The API base URL is configured in `js/config.js` (default `http://localhost:8080`).

| UI action | API |
| --- | --- |
| Load/add doctors | `GET` / `POST /api/doctors` |
| Load/add patients | `GET` / `POST /api/patients` |
| Book appointment | `POST /api/appointments` |

Appointment booking sends `patientId` and `appointmentDateTime`; the API assigns the doctor. A patient must be selected and past date-times cannot be booked. The backend currently has no appointment-list endpoint, so the recent-bookings list is limited to the current browser session and is cleared on logout.

## Local development CORS

When the UI is served on port 5500 separately from Spring Boot, allow the local frontend origin in Spring MVC CORS configuration. The backend includes `config/FrontendCorsConfig.java` for `http://localhost:5500` and `http://127.0.0.1:5500`.
