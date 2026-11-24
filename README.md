# CalcForge 🧮

[![GitHub Repository](https://img.shields.io/badge/GitHub-Repository-blue?logo=github)](https://github.com/gathamediaofficial-lab/CalcForge)
[![Check for Updates](https://img.shields.io/badge/GitHub-Check_for_Updates-2ea44f?logo=github)](https://github.com/gathamediaofficial-lab/CalcForge/releases)

**CalcForge** is a modern, feature-rich Android application designed for scientific calculations, unit conversions, financial planning, date arithmetic, and health tracking. Built using **Jetpack Compose** and **Material 3**, it delivers an intuitive, responsive, and adaptive UI for phones, foldables, and tablets.

---

## ✨ Features

### 1. 🔢 Standard & Scientific Calculator
* **Rich Math Engine:** Supports basic arithmetic, trigonometric functions (`sin`, `cos`, `tan`), logarithmic calculations (`log`, `ln`), powers, roots, factorials, and mathematical constants ($\pi$, $e$).
* **Live Result Preview:** Real-time preview of calculation results as you type.
* **Editable Display & Cursor:** Full tap-to-edit cursor movement and selection support.
* **Scientific Mode:** Expandable scientific panel for advanced calculations.

### 2. 🔄 Unit & Currency Converter
* Multi-category unit conversion support:
  * **Length:** Meters, Kilometers, Miles, Feet, Inches, etc.
  * **Mass / Weight:** Grams, Kilograms, Pounds, Ounces, Tons.
  * **Temperature:** Celsius, Fahrenheit, Kelvin.
  * **Area & Volume:** Square meters, Acres, Liters, Gallons, Cubic units.
  * **Time & Speed:** Seconds, Hours, Days, Km/h, Mph.
  * **Data Storage:** Bytes, Megabytes, Gigabytes, Terabytes.
* **Dual Display:** Instant bidirectional conversion.

### 3. 🛠️ Specialized Toolkits & Calculators
* **Date Calculator:** Calculate difference between dates or add/subtract days.
* **Health / BMI Calculator:** Calculate BMI, body fat estimation, and health categories.
* **Loan & EMI Calculator:** Estimate monthly loan repayments, total interest, and total payable amounts.
* **Tip & Bill Splitter:** Compute tip percentages and split costs evenly among groups.
* **Discount & Tax Calculator:** Fast calculation of final price after discounts and sales tax.

### 4. 📜 Calculation History
* **Local Persistence:** Powered by Room Database for fast, offline history storage.
* **Interactive Entries:** Tap any historical calculation to re-use expression or result in the active calculator.
* **Management:** Search, filter, copy, and clear calculation history.

### 5. 🎨 Customization & Accessibility
* **Material 3 Design:** Dynamic color support with light, dark, and system themes.
* **Adaptive Layouts:** Seamless transition between Navigation Bar (compact screens) and Navigation Rail (expanded screens / tablets).
* **Haptic Feedback:** Configurable tactile feedback for button presses.
* **Decimal Precision:** Adjustable decimal display formatting.

---

## 🛠️ Tech Stack & Architecture

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 components
* **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Domain Layer
* **Local Database:** [Room Database](https://developer.android.com/training/data-storage/room) with KSP
* **Preferences:** [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
* **Asynchronous Programming:** Kotlin Coroutines & `StateFlow`
* **Build System:** Gradle (Kotlin DSL) with KSP (Kotlin Symbol Processing)

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio:** Ladybug or newer recommended.
* **JDK:** Java 17 / Java 21 environment.
* **Android SDK:** API Level 24+ (Android 7.0) minimum, compiled against API 36.

### Installation & Execution

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/gathamediaofficial-lab/CalcForge.git
   cd CalcForge
   ```

2. **Open in Android Studio:**
   * Launch Android Studio, choose **Open**, and navigate to the project root directory.
   * Allow Gradle sync to download dependencies and setup build targets.

3. **Environment Setup (Optional):**
   * If using environment configuration, copy `.env.example` to `.env` in the root folder:
     ```bash
     cp .env.example .env
     ```

4. **Build and Run:**
   * Select the `:app` configuration.
   * Choose an emulator or connected target physical device.
   * Click **Run (Shift + F10)**.

---

## 📁 Project Structure

```
CalcForge/
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/            # Room entities, DAOs, DataStore repositories
│   │   ├── domain/          # Math evaluator, unit & date conversion engines
│   │   ├── ui/
│   │   │   ├── components/  # Reusable keypad buttons, displays, UI elements
│   │   │   ├── screens/     # Calculator, Converter, History, Tools, Settings screens
│   │   │   ├── theme/       # Compose Material 3 color schemes, typography, theme
│   │   │   └── viewmodel/   # ViewModels managing state flows for screens
│   │   └── MainActivity.kt  # Main activity entry point
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── debug.keystore            # Debug signing keystore
```

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
