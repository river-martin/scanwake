# ScanWake ⏰📱

<img src="https://raw.githubusercontent.com/river-martin/scanwake/main/fastlane/metadata/android/en-US/images/icon.png" width="96" alt="ScanWake Logo">

**An open-source, privacy-respecting alarm clock that won't stop until you get out of bed and scan a
barcode.**

[![Release](https://img.shields.io/github/v/release/river-martin/scanwake?include_prereleases&style=flat-square)](https://github.com/river-martin/scanwake/releases)
[![License: AGPL-3.0](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)](https://github.com/river-martin/scanwake/blob/main/LICENSE.md)
[![F-Droid Reproducible Build](https://img.shields.io/badge/F--Droid-Reproducible%20Build%20Passing-success?style=flat-square)](https://gitlab.com/river-martin/fdroiddata/-/pipelines)

---

## 💡 Motivation & Backstory

I wrote ScanWake out of pure frustration with existing options:

- **Commercial Alarms (like Alarmy):** Riddled with aggressive ads, paywalls, and
  telemetry/tracking.
- **Other Open-Source Scanner Alarms:** I tried apps like QRAlarm. While it technically supported
  barcode scan dismissal, it would often take what felt like 30 seconds to a minute of struggling
  and changing the camera angle trying to get it to detect the barcode before finally dismissing the
  alarm.

I wanted something different: a **privacy-respecting, ad-free barcode scanner alarm with
lightning-fast barcode detection**, built for my own daily use while learning Android app
development. ScanWake bridges the gap by offering **fast barcode scanning for alarm dismissal**
without sacrificing your privacy or reliability.

---

## ✨ Features

- **Lightning-Fast Barcode Scanning:** Optimized on-device scanning so you can dismiss your alarm
  immediately without frustrating delays.
- **Flexible Barcode Modes:** Require *any* barcode, or assign a specific barcode (e.g., your
  toothpaste, cereal box, or a sticker placed in the bathroom) to each alarm.
- **Saved Barcodes:** Save and name barcodes to easily reuse them across multiple alarms.
- **Repeating Alarms:** Configure alarms with custom days and custom labels.
- **Nap Alarms:** Quick presets, custom durations, and an optional fall-asleep buffer.
- **Emergency Failsafe:** Ensures you are never permanently locked out of dismissing your alarm if
  you cannot reach your barcode.
- **Reliability Checklist:** Built-in guidance for exact alarms, notifications, camera permissions,
  and battery optimization settings.
- **Material 3 Design:** Modern, clean UI with adaptive layouts supporting phones, and foldables.

---

## 🔒 Privacy & Trust Verification

How can you be sure ScanWake respects your privacy?

- **No Internet Permission:** You can inspect
  the [AndroidManifest.xml](app/src/main/AndroidManifest.xml) and verify that the app does not
  request `android.permission.INTERNET`. It is physically incapable of sending data anywhere.
- **Zero Telemetry & Ads:** No analytics, crash reporting SDKs, ads, or third-party trackers.
- **Reproducible Builds & Binary Verification:** ScanWake's build is 100% reproducible and passes
  F-Droid's build pipeline verification. This means you can independently verify that the official
  GitHub release APK matches the APK compiled by F-Droid's independent build servers (verifiable
  via [fdroiddata pipeline logs](https://gitlab.com/river-martin/fdroiddata/-/pipelines)), proving
  the binaries were built straight from the public source code without modification.

---

## 📥 Installation

You can install ScanWake in two ways:

1. **GitHub Releases:** Download the latest APK directly from
   the [Releases Page](https://github.com/river-martin/scanwake/releases).
2. **Obtainium:** Add the GitHub repository URL
   to [Obtainium](https://github.com/ImranR98/Obtainium) to receive automatic updates.

### 🤖 F-Droid Status

The app is **not yet listed on F-Droid** because the project is still too new. F-Droid requires
projects to be actively developed for at least 3 months. Once the age requirement is met, it will be
submitted for official inclusion.

---

## 📱 Screenshots

<img src="https://raw.githubusercontent.com/river-martin/scanwake/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="180" alt="Screenshot 1"> <img src="https://raw.githubusercontent.com/river-martin/scanwake/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="180" alt="Screenshot 2"> <img src="https://raw.githubusercontent.com/river-martin/scanwake/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" width="180" alt="Screenshot 2"> <img src="https://raw.githubusercontent.com/river-martin/scanwake/main/fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" width="180" alt="Screenshot 2"> <img src="https://raw.githubusercontent.com/river-martin/scanwake/main/fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" width="180" alt="Screenshot 2">

---

## ⚠️ A Note on Alarm Reliability

Alarm reliability heavily depends on your phone's aggressive battery management. Some manufacturers
(like Samsung, Xiaomi, or Huawei) aggressively kill background tasks and alarms. Please make sure to
go through the **Reliability Checklist** inside the app and test your alarm before relying on it for
important mornings!

---

## 🛠️ Tech Stack & Development

- **Language:** Kotlin
- **UI:** Jetpack Compose & Material 3
- **Database:** Room
- **Scanner:** CameraX + zxing-cpp

---

## 📜 License

ScanWake is free and open-source software licensed under the **GNU Affero General Public License
v3.0 (AGPL-3.0)**. See [LICENSE.md](LICENSE.md) for details.
