# 🏍️ MotoWeather: The Ultimate Rider's Companion

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/XML-00599C?style=for-the-badge&logo=xml&logoColor=white" />
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge" />
</p>

MotoWeather is not just another weather app; it's a specialized weather dashboard designed strictly for motorcycle riders. By evaluating real-time weather conditions against personalized riding limits (wind, rain, and visibility), MotoWeather helps you decide if it's a safe day to hit the road.

## ✨ Key Features

* **Dynamic Astro-Path Tracking ☀️🌙:** A custom-built, highly aesthetic UI component (`AstroPathView`) that tracks the sun and moon's real-time position on a curved trajectory based on actual sunrise and sunset times.
* **Personalized Riding Profiles 🪖:** Choose between Beginner, Intermediate, and Advanced/Pro levels. Each level comes with tailored limits for:
    * 💨 Wind Speed Tolerance
    * 🌧️ Rain/Precipitation Probability
    * 🌫️ Visibility Distance
* **Smart Risk Analysis:** The app instantly warns you about potential hazards like black ice (frost warning) or extreme crosswinds.
* **Multi-Language Support 🌍:** Fully localized in 6 languages: English, Turkish, German, Japanese, Korean, and Russian. The app remembers your preference using a robust `LocaleHelper` and `SharedPreferences` implementation.
* **Modern Glassmorphism UI:** A sleek, dark-themed interface built entirely with custom XML drawables and layouts, designed to be readable even under direct sunlight.

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/16fbc1a8-e2e7-4237-af5c-7c57a44b877b" width="250" alt="MotoWeather Main Screen" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/8591f215-3386-4e55-beee-74e673154ee1" width="250" alt="MotoWeather Settings" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/cb8e0ec9-56ff-4fe9-85d0-a3b22af2cf63" width="250" alt="MotoWeather Night Mode" />
</p>

---

## 🛠️ Technology Stack

* **Logic & Functionality:** Java
* **UI & Design:** XML (Custom Drawables, Vectors, Material Components)
* **Build System:** Gradle
* **Networking:** [Volley](https://google.github.io/volley/) for asynchronous HTTP requests.
* **Location Services:** Google Play Services (`FusedLocationProviderClient`)
* **Background Tasks:** Android `WorkManager` (for daily ride notification checks)
* **Weather Data:** [Open-Meteo API](https://open-meteo.com/) - completely open-source and requires no API key!

---

## 🚀 How to Run the Project (Plug & Play)

Since this project uses the completely free Open-Meteo API, **NO API KEY IS REQUIRED**. You can clone and run it instantly.

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/YOUR_USERNAME/MotoWeather.git](https://github.com/YOUR_USERNAME/MotoWeather.git)
    ```
2.  **Open in Android Studio:**
    Launch Android Studio and select `Open an existing project`, then navigate to the cloned directory.
3.  **Build and Run:**
    Sync the Gradle files and hit the `Run` button (`Shift + F10`) to install the app on your emulator or physical device.

---

## 🤝 Contributing
Contributions, issues, and feature requests are welcome! Feel free to check the issues page.

## 📝 License
This project is open-source and available under the MIT License.
