# NexusHealth Website Test Cases (Function-Level)

This document lists functional test cases for the JavaScript functions and event handlers embedded in the frontend HTML pages. Test cases are grouped by page and then by function.

## Landing Page (frontend/index.html)

### Navbar scroll handler (window scroll listener)
- **TC-INDEX-SCROLL-01**: Scroll down past 100px. **Expected:** `.navbar--hidden` is added to the navbar.
- **TC-INDEX-SCROLL-02**: Scroll down past 100px, then scroll up. **Expected:** `.navbar--hidden` is removed.
- **TC-INDEX-SCROLL-03**: Scroll from top to 20px. **Expected:** navbar boxShadow is applied without errors.

## About Page (frontend/about.html)

### Navbar scroll handler (window scroll listener)
- **TC-ABOUT-SCROLL-01**: Scroll down past 100px. **Expected:** `.navbar--hidden` is added to the navbar.
- **TC-ABOUT-SCROLL-02**: Scroll down past 100px, then scroll up. **Expected:** `.navbar--hidden` is removed.
- **TC-ABOUT-SCROLL-03**: Scroll from top to 20px. **Expected:** navbar boxShadow is applied without errors.

## Contact Page (frontend/contact.html)

### Navbar scroll handler (window scroll listener)
- **TC-CONTACT-SCROLL-01**: Scroll down past 100px. **Expected:** `.navbar--hidden` is added to the navbar.
- **TC-CONTACT-SCROLL-02**: Scroll down past 100px, then scroll up. **Expected:** `.navbar--hidden` is removed.
- **TC-CONTACT-SCROLL-03**: Scroll from top to 20px. **Expected:** navbar boxShadow is applied without errors.

### Contact form submit handler
- **TC-CONTACT-FORM-01**: Fill required fields and submit. **Expected:** Default submit is prevented, alert is shown, form resets.
- **TC-CONTACT-FORM-02**: Submit with missing required fields. **Expected:** Browser validation blocks submit; handler does not run.

## Admin Dashboard (frontend/admin-dashboard.html)

### `switchTab(tabId, event)`
- **TC-ADMIN-SWITCHTAB-01**: Click a sidebar button. **Expected:** Clicked button gets `active`; other buttons lose `active`.
- **TC-ADMIN-SWITCHTAB-02**: Switch to a tab. **Expected:** `#tab-{tabId}` gets `active`; other `.tab-content` lose `active`.

### `showToast(message, isError = false)`
- **TC-ADMIN-TOAST-01**: Call with success message. **Expected:** Toast appears with success color and auto-hides after ~3s.
- **TC-ADMIN-TOAST-02**: Call with `isError=true`. **Expected:** Toast background is red and auto-hides after ~3s.

### `openModal(modalId)`
- **TC-ADMIN-OPENMODAL-01**: Open `createUserModal`. **Expected:** modal display becomes `flex`.

### `closeModal(modalId)`
- **TC-ADMIN-CLOSEMODAL-01**: Close `createUserModal`. **Expected:** modal display becomes `none`.

### `updateProfilePic(event)`
- **TC-ADMIN-PROFILEPIC-01**: Upload a valid image. **Expected:** `#optionsProfilePic` and `#sidebarProfilePic` update; success toast shown.
- **TC-ADMIN-PROFILEPIC-02**: Trigger change with no file. **Expected:** No image change and no errors.

## Receptionist Dashboard (frontend/receptionist-dashboard.html)

### `switchTab(tabId, event)`
- **TC-RECEP-SWITCHTAB-01**: Click a sidebar button. **Expected:** Clicked button gets `active`; other buttons lose `active`.
- **TC-RECEP-SWITCHTAB-02**: Call `switchTab` with `event=null`. **Expected:** Button matching the `onclick` selector becomes `active`.
- **TC-RECEP-SWITCHTAB-03**: Switch to a tab. **Expected:** `#tab-{tabId}` gets `active`; other `.tab-content` lose `active`.

### `showToast(message, isError = false)`
- **TC-RECEP-TOAST-01**: Call with success message. **Expected:** Toast appears with success color and auto-hides after ~3s.
- **TC-RECEP-TOAST-02**: Call with `isError=true`. **Expected:** Toast background is red and auto-hides after ~3s.

### `openModal(modalId, patientName = null, invoiceId = null, amount = null)`
- **TC-RECEP-OPENMODAL-01**: Open a generic modal (e.g., `registerPatientModal`). **Expected:** modal display becomes `flex`.
- **TC-RECEP-OPENMODAL-02**: Open `recordPaymentModal` with patient data. **Expected:** patient name, invoice ID, and amount are populated with `$` prefix.
- **TC-RECEP-OPENMODAL-03**: Open `recordPaymentModal` without patient data. **Expected:** existing payment fields remain unchanged.

### `closeModal(modalId)`
- **TC-RECEP-CLOSEMODAL-01**: Close any open modal. **Expected:** modal display becomes `none`.

### `updateProfilePic(event)`
- **TC-RECEP-PROFILEPIC-01**: Upload a valid image. **Expected:** `#optionsProfilePic` and `#sidebarProfilePic` update; success toast shown.
- **TC-RECEP-PROFILEPIC-02**: Trigger change with no file. **Expected:** No image change and no errors.

## Pharmacist Dashboard (frontend/pharmacist-dashboard.html)

### `switchTab(tabId, event)`
- **TC-PHARM-SWITCHTAB-01**: Click a sidebar button. **Expected:** Clicked button gets `active`; other buttons lose `active`.
- **TC-PHARM-SWITCHTAB-02**: Switch to a tab. **Expected:** `#tab-{tabId}` gets `active`; other `.tab-content` lose `active`.

### `showToast(message, isError = false)`
- **TC-PHARM-TOAST-01**: Call with success message. **Expected:** Toast appears with success color and auto-hides after ~3s.
- **TC-PHARM-TOAST-02**: Call with `isError=true`. **Expected:** Toast background is red and auto-hides after ~3s.

### `openModal(modalId, patientName = null, doctorName = null, meds = null)`
- **TC-PHARM-OPENMODAL-01**: Open a generic modal (e.g., `addStockModal`). **Expected:** modal display becomes `flex`.
- **TC-PHARM-OPENMODAL-02**: Open `viewRxModal` with patient, doctor, and meds. **Expected:** patient/doctor labels set; meds rendered as list items with trimmed names.
- **TC-PHARM-OPENMODAL-03**: Open `viewRxModal` without patient data. **Expected:** existing labels and med list remain unchanged.

### `closeModal(modalId)`
- **TC-PHARM-CLOSEMODAL-01**: Close any open modal. **Expected:** modal display becomes `none`.

### `updateProfilePic(event)`
- **TC-PHARM-PROFILEPIC-01**: Upload a valid image. **Expected:** `#optionsProfilePic` and `#sidebarProfilePic` update; success toast shown.
- **TC-PHARM-PROFILEPIC-02**: Trigger change with no file. **Expected:** No image change and no errors.

## Doctor Dashboard (frontend/doctor-dashboard.html)

### `switchTab(tabId, event)`
- **TC-DOCTOR-SWITCHTAB-01**: Click a sidebar button. **Expected:** Clicked button gets `active`; other buttons lose `active`.
- **TC-DOCTOR-SWITCHTAB-02**: Call `switchTab` with `event=null`. **Expected:** Button matching the `onclick` selector becomes `active`.
- **TC-DOCTOR-SWITCHTAB-03**: Switch to a tab. **Expected:** `#tab-{tabId}` gets `active`; other `.tab-content` lose `active`.

### `showToast(message, isError = false)`
- **TC-DOCTOR-TOAST-01**: Call with success message. **Expected:** Toast appears with success color and auto-hides after ~3s.
- **TC-DOCTOR-TOAST-02**: Call with `isError=true`. **Expected:** Toast background is red and auto-hides after ~3s.

### `toggleAvailability()`
- **TC-DOCTOR-AVAIL-01**: Enable leave toggle. **Expected:** status text shows “On Leave,” text color red, error toast shown.
- **TC-DOCTOR-AVAIL-02**: Disable leave toggle. **Expected:** status text shows “Available,” success color, success toast shown.

### `startConsultation(name, id, allergies)`
- **TC-DOCTOR-CONSULT-01**: Start with allergies list. **Expected:** consult sections visible, patient info populated, allergy box visible with list.
- **TC-DOCTOR-CONSULT-02**: Start with allergies set to `"None"`. **Expected:** allergy box hidden.
- **TC-DOCTOR-CONSULT-03**: Start consultation. **Expected:** `#rx-patient-name` prefilled and tab switches to `consultations`.

### `jumpToPharmacy()`
- **TC-DOCTOR-PHARM-01**: Trigger jump. **Expected:** active tab switches to `pharmacy`.

### `addMedication()`
- **TC-DOCTOR-ADDMED-01**: Submit with empty medication name. **Expected:** error toast shown; no row added.
- **TC-DOCTOR-ADDMED-02**: Add with name (dose/freq optional). **Expected:** medication added and rendered; inputs cleared.

### `removeMedication(index)`
- **TC-DOCTOR-REMOVEMED-01**: Remove an existing row. **Expected:** row removed and table re-rendered.

### `renderRxTable()`
- **TC-DOCTOR-RENDER-01**: No medications. **Expected:** placeholder row “No medications added yet.”
- **TC-DOCTOR-RENDER-02**: Multiple medications. **Expected:** rows rendered with name, dose, freq, and remove button.

### `sendToPharmacy()`
- **TC-DOCTOR-SENDRX-01**: No patient selected. **Expected:** error toast; no reset.
- **TC-DOCTOR-SENDRX-02**: Patient selected but no medications. **Expected:** error toast; no reset.
- **TC-DOCTOR-SENDRX-03**: Patient and medications present. **Expected:** success toast; after ~2.5s list cleared, patient cleared, tab returns to `schedule`.

### `updateProfilePic(event)`
- **TC-DOCTOR-PROFILEPIC-01**: Upload a valid image. **Expected:** options and sidebar images update; success toast shown.
- **TC-DOCTOR-PROFILEPIC-02**: Trigger change with no file. **Expected:** No image change and no errors.

## Patient Dashboard (frontend/patient-dashboard.html)

### `switchTab(tabId, event)`
- **TC-PATIENT-SWITCHTAB-01**: Click a desktop nav tab. **Expected:** matching desktop and mobile tabs both gain `active`.
- **TC-PATIENT-SWITCHTAB-02**: Switch to a tab. **Expected:** `#tab-{tabId}` gains `active`; other tabs lose `active`.
- **TC-PATIENT-SWITCHTAB-03**: Open mobile drawer and switch tab. **Expected:** drawer closes (removes `open`).

### `resetTab(tabId)`
- **TC-PATIENT-RESETTAB-01**: Reset to overview. **Expected:** only `#tab-overview` is active; all nav tabs deactivated.

### `toggleMobileMenu()`
- **TC-PATIENT-MOBILEMENU-01**: Toggle when closed. **Expected:** `#mobileDrawer` gains `open`.
- **TC-PATIENT-MOBILEMENU-02**: Toggle when open. **Expected:** `#mobileDrawer` loses `open`.

### `selectDoctor(el, name)`
- **TC-PATIENT-SELECTDOC-01**: Select a doctor card. **Expected:** only that card has `selected`; slots section becomes visible.
- **TC-PATIENT-SELECTDOC-02**: Select doctor with no time chosen. **Expected:** confirm button remains disabled.

### `selectSlot(el)`
- **TC-PATIENT-SELECTSLOT-01**: Click a disabled slot. **Expected:** no selection change and no booking enablement.
- **TC-PATIENT-SELECTSLOT-02**: Click an enabled slot after selecting doctor. **Expected:** slot becomes selected and confirm button enables.

### `validateBooking()`
- **TC-PATIENT-VALIDATE-01**: Only doctor selected. **Expected:** confirm button disabled.
- **TC-PATIENT-VALIDATE-02**: Only time selected. **Expected:** confirm button disabled.
- **TC-PATIENT-VALIDATE-03**: Doctor and time selected. **Expected:** confirm button enabled.

### `confirmBooking()`
- **TC-PATIENT-CONFIRM-01**: Confirm booking with selections. **Expected:** booking modal shows; selections reset; slots section hidden; confirm disabled.

### `viewReceipt(id)`
- **TC-PATIENT-RECEIPT-01**: Open receipt modal. **Expected:** receipt ID text shows `Receipt #<id>` and modal becomes active.

### `closeModal(id)`
- **TC-PATIENT-CLOSEMODAL-01**: Close receipt or booking modal. **Expected:** modal loses `active`.

### `updateProfilePic(event)`
- **TC-PATIENT-PROFILEPIC-01**: Upload a valid image. **Expected:** nav and options images update; toast displayed.
- **TC-PATIENT-PROFILEPIC-02**: Trigger change with no file. **Expected:** no image change and no errors.

### `showSavedToast()`
- **TC-PATIENT-TOAST-01**: Trigger save. **Expected:** toast becomes visible and auto-hides after ~3s.

### Modal backdrop click handler (`window.onclick`)
- **TC-PATIENT-BACKDROP-01**: Click modal overlay backdrop. **Expected:** modal closes via `closeModal`.
- **TC-PATIENT-BACKDROP-02**: Click inside modal content. **Expected:** modal remains open.

### `applyTheme(theme)`
- **TC-PATIENT-THEME-01**: Apply `light`. **Expected:** `data-theme="light"` set; icon shows `dark_mode`.
- **TC-PATIENT-THEME-02**: Apply `dark`. **Expected:** `data-theme` removed; icon shows `light_mode`.
- **TC-PATIENT-THEME-03**: Apply theme with missing icon element. **Expected:** no errors thrown.

### `toggleTheme()`
- **TC-PATIENT-TOGGLE-01**: Toggle from dark to light. **Expected:** localStorage set to `light`; theme applied; icon animates.
- **TC-PATIENT-TOGGLE-02**: Toggle from light to dark. **Expected:** localStorage set to `dark`; theme applied; icon animates.

### `initTheme()` (IIFE)
- **TC-PATIENT-INIT-01**: Stored preference exists. **Expected:** stored theme applied on load.
- **TC-PATIENT-INIT-02**: No stored preference. **Expected:** system preference determines initial theme.
