# UPISplitter 🚀 — Offline Merchant Payment Splitter

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-purple.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-orange.svg)](LICENSE)

**UPISplitter** is a modern, offline-first Android application designed to help users split large merchant UPI payments into valid, scannable QR code chunks (such as ₹2,000 or ₹10,000 limits) directly on their device without requiring an active internet connection.

---

## 📲 Direct APK Download

Try the app on your Android phone right now:

👉 **[Download UPISplitter-v1.0.apk](https://github.com/harsh-vsh/upi-splitter/raw/main/apk/UPISplitter-v1.0.apk)**

---

## ✨ Key Features

- 📸 **Camera QR Scanner**: Real-time barcode scanning powered by **Google ML Kit Barcode Scanning** & **CameraX**. Supports scanning GPay, PhonePe, Paytm, CRED & BHIM merchant QR codes with haptic feedback, flashlight toggle (`Flash`), and image selection from gallery (`Gallery`).
- ⚡ **Custom Single QR Code**: 3-step wizard to pick a saved merchant, enter a total amount, and generate an ultra-sharp high-resolution pure QR code matrix complete with payee name, UPI ID, amount badge, and a `COMPLETE PAYMENT` action button.
- 💳 **Split by UPI ID**: Multi-step wizard to pick saved merchants from a vertical list with radio selection buttons and break payments into valid chunks.
- 🏪 **Saved Merchants Manager**: Built-in local **Room Database** to add, edit, or delete merchant UPI IDs. Scanning a merchant QR in Add/Edit mode auto-populates merchant name and UPI ID in real-time.
- 📜 **Payment History**: Filter transactions by search query, select calendar date filters, mark payments as completed, single item deletion, and multi-select bulk delete.
- 🔒 **100% Offline & Private**: Zero cloud dependencies or internet access required. Your payment history and merchant data remain strictly local on your device.

---

## 🛠 Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material 3 Fintech Design System](https://m3.material.io)
- **Database & Architecture**: [Room DB](https://developer.android.com/training/data-storage/room) + Kotlin Coroutines + `StateFlow` + ViewModel
- **Camera & Vision**: [CameraX](https://developer.android.com/training/camerax) + [Google ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- **QR Code Generation**: [ZXing Core](https://github.com/zxing/zxing) Matrix Rendering
- **Navigation**: Jetpack Navigation Compose
- **Sharing**: AndroidX `FileProvider`

---

## ⚙️ Building from Source

To build the project locally using Android Studio or Gradle:

```bash
# 1. Clone the repository
git clone https://github.com/harsh-vsh/upi-splitter.git

# 2. Open project directory
cd upi-splitter

# 3. Build debug APK using Gradle
./gradlew assembleDebug
```

The compiled APK will be output at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
