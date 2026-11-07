# DuWatch Pro ⌚

[![Download APK](https://img.shields.io/badge/Download_Fixed_APK-DuWatch__Pro.apk-00E5FF?style=for-the-badge&logo=android&logoColor=black)](https://github.com/Aminelbouayachi/duwatch/releases/download/latest/DuWatch-Pro.apk)
[![Android Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/Aminelbouayachi/duwatch/actions)
[![Compatibility](https://img.shields.io/badge/Android-7.0_to_15_(API_24--35)-blue?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Aminelbouayachi/duwatch)

> **✅ Status: Bug Fixed & Verified (v1.0.1)**  
> The Samsung One UI / Android 14+ startup crash has been resolved. You can download and install the fixed APK directly using the button above or from the [Latest Releases](https://github.com/Aminelbouayachi/duwatch/releases/latest).

---
**DuWatch Pro** is a modern Android companion application built with **Jetpack Compose** and **Material Design 3** for smartwatches and fitness trackers. It provides complete customization of watch faces, alarms, vitals monitoring, remote camera control, and smartwatch hardware settings.

---

## ✨ Features

### 1. 🎨 Watch Face Customizer Studio
- **Live Interactive Watch Display Preview** in a sleek titanium smartwatch case with digital crown and strap.
- **Dual Display Modes**:
  - **Digital Modern**: High-contrast LCD/LED digits, custom time position (Top, Center, Bottom), and glowing neon time fonts.
  - **Analog Chronograph**: Real-time smooth sweeping seconds hand, custom minute/hour hands, 60-second outer bezel markings, and date complication subdial.
- **Customizable Wallpapers & Backgrounds**:
  - Sci-Fi Cyberpunk neon rings.
  - Cosmic Deep Space Nebula.
  - Deep AMOLED black & custom radial gradients.
- **Data Complications (Widgets)**:
  - Heart Rate (BPM)
  - Daily Step Counter
  - Watch Battery %
  - Weather & Temperature
  - Calories Burned
  - Calendar Date & Distance (km)
- **Bluetooth BLE Sync Simulation**: Realistic dial packaging, compression, binary upload, and CRC32 verification modal.

### 2. ⏰ Smart Alarms & Health Reminders
- Multiple customizable alarms with 24h/12h time selector.
- **Smart Gentle Wake-up**: Gentle micro-vibration 10 minutes prior to optimal sleep cycle.
- Selectable repeat days (Monday through Sunday).
- Vibration patterns: Gentle Wave, Standard Buzz, Double Pulse, Ultra Shock.
- **Sedentary Alert**: Notifies user when inactive for > 60 minutes.
- **Water Hydration Alert**: Reminds user to drink water every 2 hours.

### 3. 🛠️ Smart Watch Controls
- **Find My Watch**: Triggers watch buzzer and visual pulse radar.
- **Remote Camera Shutter**: Shake or tap watch to remotely snap photos on phone.
- **Raise to Wake**: Turns on screen automatically upon wrist gesture.
- **Continuous 24/7 Heart Rate**: Periodic cardiac telemetry monitoring.
- **Weather Push**: Live temperature and city forecast directly to the dial.
- **App Notification Forwarding**: Push alerts for WhatsApp, Calls, SMS, Instagram, Messenger, Gmail, and Telegram.
- **BLE Scanner & OTA Firmware**: Nearby smartwatch scanner and firmware updater.

### 4. 📊 Health & Activity Dashboard
- Real-time battery indicator with charging state.
- Daily activity rings for Steps, Calories, and Distance.
- Hourly activity bar chart.
- Vitals metrics: Heart Rate monitor (with "Measure Now"), SpO2 blood oxygen, and Sleep stage breakdown (Deep, Light, REM).

---

## 🚀 Tech Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Concurrency**: Kotlin Coroutines & StateFlow
- **Image Loading**: Coil Compose
- **Build System**: Gradle (Kotlin DSL - `.gradle.kts`) + Version Catalog (`libs.versions.toml`)

---

## 📱 Building & Running

1. Clone the repository:
   ```bash
   git clone https://github.com/Aminelbouayachi/duwatch.git
   ```
2. Open the project in **Android Studio Hedgehog** (or newer).
3. Let Gradle sync dependencies.
4. Run on an Android device or emulator running Android 7.0+ (API 24 to 36).
