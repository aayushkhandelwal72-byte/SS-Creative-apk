# SS Creative - Android Application (APK)

**Repository:** [aayushkhandelwal72-byte/SS-Creative-apk](https://github.com/aayushkhandelwal72-byte/SS-Creative-apk)  
**Package:** `com.aistudio.sscreative.agency`  
**Platform:** Android (Jetpack Compose, Kotlin, Material 3)

---

## 🎨 Overview

**SS Creative** is an all-in-one creative agency client portal, portfolio showcase, editable paint studio, and GST billing management software designed for SS Creative Studio.

### 🌟 Key Features

1. **💼 GST Billing & Invoicing Software**
   - Professional GST tax invoice generator with SAC code tracking (`998311` etc.).
   - Itemized service breakdown with dynamic CGST + SGST (18%) tax calculations.
   - Client GSTIN, invoice numbering (`INV-2026-XXXX`), due dates, and payment status tracking (Paid, Pending, Overdue).
   - Light blue column text accents and printable/exportable tax invoice sheet.
   - Quick action to record and generate new GST invoices directly from the app.

2. **🖌️ Editable Paint Brush Studio**
   - Full-featured digital canvas with customizable brush sizes, opacities, and vibrant color palettes.
   - Eraser tool, undo history, clear canvas, and local export/save capability.
   - Integrated AI Design Review / Critique feedback badge for creative iterations.

3. **📱 Creative Portfolio & Client Portal**
   - High-fidelity showcase of studio branding, UI/UX, 3D motion graphics, and print designs.
   - Filterable categories with visual grid cards.
   - Booking consultation & real-time chat with agency lead (Nidhi Agarwal).

4. **🔒 Admin Dashboard**
   - KPI metrics overview: Total revenue, active clients, outstanding GST dues, and invoice summaries.
   - Quick client status switcher and agency settings management.

5. **🎨 Navy Blue & Light Blue Theme**
   - Premium Midnight Navy Blue background palette (`#0F172A`, `#1E293B`).
   - High-contrast Light Blue column text fills (`#7DD3FC`, `#93C5FD`, `#38BDF8`) for optimal readability.

---

## 🚀 Building & Downloading APK

### Option A: From AI Studio Platform
1. In the top navigation or project settings in Google AI Studio, click on **Export / Download APK**.
2. Download `app-debug.apk` directly to your phone or computer.
3. Install on any Android device running Android 8.0+ (API 26+).

### Option B: Push to GitHub (`aayushkhandelwal72-byte/SS-Creative-apk`)
1. Click **Export to GitHub** in Google AI Studio.
2. Select your repository: **`aayushkhandelwal72-byte/SS-Creative-apk`**.
3. Push all commits. The included GitHub Actions workflow will automatically build the APK on every push!

### Option C: Build Locally with Gradle
```bash
# Clone the repository
git clone https://github.com/aayushkhandelwal72-byte/SS-Creative-apk.git
cd SS-Creative-apk

# Assemble the debug APK
gradle :app:assembleDebug

# The APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0+
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with Kotlin Coroutines & StateFlow
- **Design System:** Custom Dark Theme (Midnight Navy Blue with Light Blue Typography)
- **Minimum SDK:** Android 8.0 (API Level 26)
- **Target SDK:** Android 15 (API Level 35)

---

## 📄 License & Credits
Developed for **SS Creative Studio** by [aayushkhandelwal72-byte](https://github.com/aayushkhandelwal72-byte).
