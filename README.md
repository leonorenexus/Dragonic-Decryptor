# 🐉 Dragonic Decryptor

<p align="center">
  <img src="https://img.shields.io/badge/Android-24%2B-3DDC84?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=for-the-badge&logo=kotlin" />
  <img src="https://img.shields.io/badge/Material%20Design-3-757575?style=for-the-badge&logo=materialdesign" />
  <img src="https://img.shields.io/badge/Offline-100%25-00FFB3?style=for-the-badge" />
</p>

<p align="center">
  <b>Advanced Decryption Toolkit — 100% Offline — Cyber Security Dashboard UI</b>
</p>

---

## ✨ Features

### 🔐 Universal Decryptor
| Algorithm | Status |
|-----------|--------|
| AES-128 / AES-192 / AES-256 | ✅ |
| DES / 3DES | ✅ |
| Blowfish | ✅ |
| RC4 | ✅ |
| XOR | ✅ |
| ChaCha20 | ✅ |

### 🔓 Decoder Tools
- Base64 / Base32 / Base16 / Hex
- Binary / Octal
- ROT13
- URL Decode / HTML Decode
- Unicode Escape Decode
- JWT Decode (header + payload)
- Gzip / Zlib Decompress

### 🧠 Smart Analyzer
- Auto-detect encoding & encryption
- Entropy calculation & chart
- File type detection
- Header signature analysis
- Confidence scoring

### 📁 File Support
TXT · JSON · XML · YAML · INI · CONF · LOG · CSV · BIN · DAT · ENC · DB · SQL · ZIP

### 🗂️ History & Files
- Every operation saved to local Room database
- Search & filter history
- Favorite entries
- Export / Share results

### 🎨 UI Design
- Premium cybersecurity dashboard aesthetic
- Dark neon background `#0A0E1A`
- Neon cyan/green accents `#00FFB3`
- Glassmorphism cards
- Animated radar scanner
- Animated splash screen
- Bottom navigation

---

## 🏗️ Tech Stack

```
Language     : Kotlin 1.9
UI           : Material Design 3 + ViewBinding
Architecture : MVVM
Database     : Room 2.6
Coroutines   : kotlinx.coroutines 1.7
Navigation   : Navigation Component 2.7
Charts       : MPAndroidChart 3.1
Min SDK      : 24 (Android 7.0)
Target SDK   : 34 (Android 14)
```

---

## 🚀 Build & Install

### Requirements
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34

### Build steps
```bash
git clone https://github.com/YOUR_USERNAME/dragonic-decryptor.git
cd dragonic-decryptor
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/
```

### Install on device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚡ GitHub Actions CI/CD

Every push to `main` / `master` automatically:
1. Compiles the project
2. Builds a Debug APK
3. Uploads the APK as a **GitHub Actions Artifact**
4. Creates a **GitHub Release** with the APK attached

See `.github/workflows/build.yml`

---

## 📂 Project Structure

```
app/src/main/
├── java/com/dragonic/decryptor/
│   ├── ui/
│   │   ├── SplashActivity.kt
│   │   ├── MainActivity.kt
│   │   ├── home/HomeFragment.kt
│   │   ├── tools/ToolsFragment.kt
│   │   ├── tools/FileDecryptorFragment.kt
│   │   ├── analyzer/AnalyzerFragment.kt
│   │   ├── result/ResultFragment.kt
│   │   ├── history/HistoryFragment.kt
│   │   ├── files/FilesFragment.kt
│   │   └── settings/SettingsFragment.kt
│   ├── data/
│   │   ├── db/  (Room entities, DAOs, Database)
│   │   └── repository/DragonicRepository.kt
│   ├── domain/model/Models.kt
│   └── util/
│       ├── DecryptionEngine.kt   ← All crypto algorithms
│       ├── FileAnalyzer.kt       ← Entropy & type detection
│       └── ToolsAdapter.kt
└── res/
    ├── layout/   (14 XML layouts)
    ├── drawable/ (Icons + backgrounds)
    ├── values/   (colors, strings, themes, dimens)
    ├── navigation/nav_graph.xml
    └── anim/     (Animations)
```

---

## 🔒 Privacy

- **100% offline** — zero network requests
- All data stored locally in Room database
- No analytics, no ads, no tracking

---

## 👨‍💻 Developer

**Dragonic Team**  
Version: 1.0.0  
License: MIT
