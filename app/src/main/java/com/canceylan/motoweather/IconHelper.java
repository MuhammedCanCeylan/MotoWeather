package com.canceylan.motoweather;

public class IconHelper {

    /**
     * Rehberdeki (dialog_ikon_rehberi.xml) BÜTÜN emojileri kullanır.
     * Hiçbir emoji dışarıda kalmadı.
     */
    public static String getWeatherEmoji(int code, double temp, boolean isNight) {

        // 1. ÖZEL SICAKLIK DURUMLARI (Önce bunlara bakar)
        // Eğer hava "Açık" veya "Az Bulutlu" ise (Yağış yoksa) ve sıcaklık ekstremse:
        if (code <= 3) {
            if (temp >= 35) return "🔥"; // Aşırı Sıcak
            if (temp <= -5) return "🥶"; // Dondurucu Soğuk
        }

        switch (code) {
            // --- GÖKYÜZÜ (Klasikler) ---
            case 0: return isNight ? "🌙" : "☀️"; // Açık
            case 1: return isNight ? "🌚" : "🌤️"; // Az Bulutlu
            case 2: return "⛅"; // Parçalı Bulutlu
            case 3: return "☁️"; // Çok Bulutlu / Kapalı

            // --- SİS GRUBU ---
            case 45: return "🌫️"; // Sis (Standart)
            case 48: return "🌁"; // Kırağılı Sis / Pus (Rehberdeki Pus İkonu)

            // --- YAĞMUR GRUBU ---
            case 51: case 53: case 55:
                return "💧"; // Çiseleme (Hafif)

            case 61: case 63:
                return "🌧️"; // Yağmur (Normal)

            case 65:
                return "☔"; // Kuvvetli Yağmur (Şemsiye)

            case 80: case 81:
                return isNight ? "🌧️" : "🌦️"; // Sağanak (Gündüz Güneşli Sağanak)

            case 82:
                return "🌊"; // Şiddetli Sağanak / Sel Riski (Dalga İkonu)

            // --- BUZLANMA GRUBU ---
            case 56: case 57: // Dondurucu Çiseleme
            case 66: case 67: // Dondurucu Yağmur
                return "🧊"; // Buzlanma / Gizli Buz

            // --- KAR GRUBU ---
            case 71: case 73:
                return "🌨️"; // Kar Yağışı (Standart)

            case 77:
                return "❄️"; // Kar Taneleri / Dolu Benzeri

            case 75: // Yoğun Kar
            case 85: case 86: // Kar Sağanağı
                return "⛄"; // Tipi / Yoğun Kar (Kardan Adam)

            // --- FIRTINA VE RÜZGAR GRUBU ---
            case 95:
                return "⛈️"; // Fırtına (Gök Gürültülü)

            case 96:
                return "💨"; // Hafif Dolulu Fırtına -> Bunu "Rüzgar/Fırtına" olarak atadık

            case 99:
                return "⚡"; // Şiddetli Dolulu Fırtına -> Bunu "Şimşek/Yıldırım" olarak atadık

            default:
                return "❓"; // Bilinmeyen
        }
    }

    /**
     * Resim kaynağı sorulursa 0 dönüyoruz çünkü artık tamamen EMOJİ kullanıyoruz.
     */
    public static int getIconResource(int code, boolean isNight) {
        return 0;
    }
}