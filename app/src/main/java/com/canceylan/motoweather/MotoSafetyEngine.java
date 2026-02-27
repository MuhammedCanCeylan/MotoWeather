package com.canceylan.motoweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class MotoSafetyEngine {

    private Context context;
    private static final String FILE_NAME = "MOTO_FINAL_CONFIG"; // InitialSetupDialog ile aynı dosya

    // Limitler
    private int limitWind = 35;
    private int limitRain = 40;
    private int limitMinTemp = 5;
    private boolean isNightAllowed = true;
    private boolean isIceProtection = true;
    private int motorType = 2;
    private String riderLevel = "intermediate"; // beginner, intermediate, expert

    // Renkler
    private static final String COLOR_PERFECT = "#00C853"; // Yeşil
    private static final String COLOR_GOOD = "#8BC34A";    // Açık Yeşil
    private static final String COLOR_MODERATE = "#FFC107"; // Sarı
    private static final String COLOR_RISKY = "#FF5722";   // Turuncu
    private static final String COLOR_DANGER = "#D32F2F";  // Kırmızı
    private static final String COLOR_FATAL = "#B71C1C";   // Koyu Kırmızı

    public enum RiskStatus { SAFE, RISK, DANGER }

    public MotoSafetyEngine(Context context) {
        this.context = context;
        reloadSettings();
    }

    public void reloadSettings() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            limitWind = getSafeInt(prefs, "pref_wind_limit", 35);
            limitRain = getSafeInt(prefs, "pref_rain_limit", 40);
            // Min sıcaklık genelde sabittir ama ayardan da çekilebilir
            limitMinTemp = 4;

            isNightAllowed = prefs.getBoolean("pref_night_ride", true);
            // Buz koruması varsayılan açık olsun
            isIceProtection = true;

            motorType = getSafeInt(prefs, "pref_motor_type", 2);
            riderLevel = prefs.getString("pref_rider_level", "intermediate");

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- ANA BEYİN ---
    public RiskAnalysis analyze(double temp, double wind, int rainProb, boolean isDay) {
        reloadSettings();

        // ---------------------------------------------------------
        // AŞAMA 1: KIRMIZI ÇİZGİLER (HESAPLAMA YOK, DİREKT RED)
        // ---------------------------------------------------------

        // 1. Gece Yasağı
        if (!isDay && !isNightAllowed) {
            return new RiskAnalysis(
                    context.getString(R.string.durum_yasak),
                    COLOR_FATAL,
                    context.getString(R.string.lbl_gece_surus_yasagi),
                    RiskStatus.DANGER
            );
        }

        // 2. Buzlanma (Tartışmaya kapalı)
        if (isIceProtection && temp <= limitMinTemp) {
            return new RiskAnalysis(
                    context.getString(R.string.durum_tehlike),
                    COLOR_DANGER,
                    context.getString(R.string.adv_body_danger_ice),
                    RiskStatus.DANGER
            );
        }

        // 3. Limit Aşımı (Fırtına Varsa Asla Yeşil Yanmaz)
        // Kullanıcının belirlediği limiti aştıysa direkt kırmızı.
        if (wind >= limitWind) {
            return new RiskAnalysis(
                    context.getString(R.string.item_firtina),
                    COLOR_DANGER,
                    context.getString(R.string.adv_body_danger_wind),
                    RiskStatus.DANGER
            );
        }

        // ---------------------------------------------------------
        // AŞAMA 2: DETAYLI PUANLAMA (0 - 100)
        // ---------------------------------------------------------
        double score = calculateRideScore(temp, wind, rainProb, isDay);

        // ---------------------------------------------------------
        // AŞAMA 3: KARAR VERME
        // ---------------------------------------------------------
        return getResultFromScore(score, wind, rainProb);
    }

    private double calculateRideScore(double temp, double wind, int rainProb, boolean isDay) {
        double score = 100.0;

        // 1. DENEYİM KATSAYISI (Experience Factor)
        // Acemiysen rüzgar ve yağmur puanını daha çok düşürür.
        double skillPenaltyMultiplier = 1.0;
        if (riderLevel.equals("beginner")) skillPenaltyMultiplier = 1.5; // %50 daha fazla ceza
        else if (riderLevel.equals("expert")) skillPenaltyMultiplier = 0.8; // %20 tolerans

        // 2. MOTOR TİPİ DENGESİ
        // Hafif motorlar rüzgardan çok etkilenir.
        double bikeStability = 1.0;
        if (motorType == 1) bikeStability = 1.3; // Scooter (Dengesiz)
        else if (motorType == 4 || motorType == 5) bikeStability = 0.8; // Touring/Adv (Sağlam)

        // 3. RÜZGAR CEZASI (Karesel Artış)
        // Rüzgar arttıkça tehlike katlanarak artar.
        double windRatio = wind / (double) limitWind;
        double windPenalty = (Math.pow(windRatio, 2) * 50) * bikeStability * skillPenaltyMultiplier;

        // Yağmur varsa rüzgar daha tehlikelidir (+%30 Ceza)
        if (rainProb > 30) windPenalty *= 1.3;

        score -= windPenalty;

        // 4. YAĞMUR CEZASI
        double rainPenalty = 0;
        if (rainProb > 15) {
            rainPenalty = (rainProb / (double) limitRain) * 40 * skillPenaltyMultiplier;
        }
        score -= rainPenalty;

        // 5. SICAKLIK KONFORU
        // Güvenlikten çok konforu etkiler ama aşırı soğuk dikkati dağıtır.
        if (temp < 10) score -= (10 - temp) * 3;
        else if (temp > 35) score -= (temp - 35) * 2;

        // 6. GECE FAKTÖRÜ
        // Yasak değilse bile risktir.
        if (!isDay) score -= 15 * skillPenaltyMultiplier; // Acemiye gece daha zor

        return Math.max(0, score);
    }

    private RiskAnalysis getResultFromScore(double score, double wind, int rainProb) {
        // Puan aralıklarına göre karar ver
        if (score >= 85) {
            return new RiskAnalysis(context.getString(R.string.risk_uygun_baslik), COLOR_PERFECT, getMotorAdvice(), RiskStatus.SAFE);
        } else if (score >= 65) {
            return new RiskAnalysis(context.getString(R.string.durum_uygun), COLOR_GOOD, getMotorAdvice(), RiskStatus.SAFE);
        } else if (score >= 45) {
            // SARI BÖLGE (Dikkat)
            String msg = context.getString(R.string.durum_dikkat);
            String detail = (wind > limitWind * 0.6) ? context.getString(R.string.adv_body_wind_strong) : context.getString(R.string.ekipman_rahat);
            return new RiskAnalysis(msg, COLOR_MODERATE, detail, RiskStatus.RISK);
        } else if (score >= 25) {
            // TURUNCU BÖLGE (Riskli)
            String msg = context.getString(R.string.durum_riskli);
            String detail = (rainProb > 40) ? context.getString(R.string.adv_body_wet) : context.getString(R.string.uyari_riskli);
            return new RiskAnalysis(msg, COLOR_RISKY, detail, RiskStatus.RISK);
        } else {
            // KIRMIZI BÖLGE (Tehlike - Puan çok düştüyse)
            return new RiskAnalysis(context.getString(R.string.durum_tehlike), COLOR_DANGER, context.getString(R.string.adv_body_danger_general), RiskStatus.DANGER);
        }
    }

    // --- SAATLİK ANALİZ (Grafikler için) ---
    public int analyzeHour(double wind, int rainProb, double temp, int hourOfDay) {
        boolean isNight = isNight(hourOfDay);

        // ÖNCE KIRMIZI ÇİZGİLER
        if (isNight && !isNightAllowed) return Color.parseColor(COLOR_FATAL);
        if (isIceProtection && temp <= limitMinTemp) return Color.parseColor(COLOR_DANGER);
        if (wind >= limitWind) return Color.parseColor(COLOR_DANGER); // Limit aşımı kesin kırmızı

        // SONRA PUANLAMA
        double score = calculateRideScore(temp, wind, rainProb, !isNight);

        if (score >= 85) return Color.parseColor(COLOR_PERFECT);
        if (score >= 65) return Color.parseColor(COLOR_GOOD);
        if (score >= 45) return Color.parseColor(COLOR_MODERATE);
        if (score >= 25) return Color.parseColor(COLOR_RISKY);
        return Color.parseColor(COLOR_DANGER);
    }

    public boolean isNight(int hour) {
        return (hour >= 20 || hour < 6);
    }

    public String getRiskMessage(double wind, double rain, double temp, int hour) {
        if (isNight(hour) && !isNightAllowed) return context.getString(R.string.durum_yasak);
        if (temp <= limitMinTemp) return context.getString(R.string.item_buzlanma);
        if (wind >= limitWind) return context.getString(R.string.item_firtina);

        double score = calculateRideScore(temp, wind, (int)rain, !isNight(hour));

        if (score < 25) return context.getString(R.string.durum_tehlike);
        if (score < 45) return context.getString(R.string.durum_riskli);
        if (score < 65) return context.getString(R.string.durum_dikkat);
        return context.getString(R.string.risk_uygun_baslik);
    }

    private String getMotorAdvice() {
        switch (motorType) {
            case 1: return context.getString(R.string.advice_scooter);
            case 2: return context.getString(R.string.advice_naked);
            case 3: return context.getString(R.string.advice_racing);
            case 4: return context.getString(R.string.advice_touring);
            case 5: return context.getString(R.string.advice_cross);
            default: return context.getString(R.string.advice_standard);
        }
    }

    private int getSafeInt(SharedPreferences prefs, String key, int def) {
        try {
            String val = prefs.getString(key, null);
            if (val != null) return Integer.parseInt(val);
        } catch (Exception e) { }
        return prefs.getInt(key, def);
    }

    public static class RiskAnalysis {
        public String message;
        public String colorCode;
        public String equipment;
        public RiskStatus status;

        public RiskAnalysis(String msg, String color, String equip, RiskStatus stat) {
            this.message = msg;
            this.colorCode = color;
            this.equipment = equip;
            this.status = stat;
        }
    }
}