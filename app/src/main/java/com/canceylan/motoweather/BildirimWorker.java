package com.canceylan.motoweather;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class BildirimWorker extends Worker {

    public BildirimWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("MotoWeatherPrefs", Context.MODE_PRIVATE);

        // DÜZELTME 1: MainActivity ile aynı anahtarları ("lat", "lon") kullandık
        String lat = prefs.getString("lat", "0");
        String lon = prefs.getString("lon", "0");

        if (lat.equals("0") || lon.equals("0")) return Result.failure();

        try {
            // API Bağlantısı
            String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon +
                    "&current_weather=true&daily=precipitation_probability_max&hourly=temperature_2m,precipitation_probability,windspeed_10m,weathercode,is_day&timezone=auto";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 5 saniye zaman aşımı

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONObject current = json.getJSONObject("current_weather");
            JSONObject hourly = json.getJSONObject("hourly");

            // --- AYARLARI GÜVENLİ ÇEKME (String to Int Çevirisi) ---
            // Ayarlar menüsünden gelen değerler String olabilir, patlamasın diye çeviriyoruz.
            int userLevel = safeParseInt(prefs, "pref_rider_level", "1");

            // Seviyeye göre limitler (Kendi mantığın)
            int limitRuzgar, limitYagmur;
            if(userLevel == 0) { limitRuzgar = 20; limitYagmur = 10; }
            else if(userLevel == 1) { limitRuzgar = 35; limitYagmur = 40; }
            else { limitRuzgar = 55; limitYagmur = 70; }

            // Kullanıcının elle girdiği özel limitler varsa onları kullan
            String customWind = prefs.getString("pref_wind_limit", "");
            if(!customWind.isEmpty()) limitRuzgar = Integer.parseInt(customWind);

            String customRain = prefs.getString("pref_rain_limit", "");
            if(!customRain.isEmpty()) limitYagmur = Integer.parseInt(customRain);

            // Mevcut Durum
            double ruzgar = current.getDouble("windspeed");
            double sicaklik = current.getDouble("temperature");
            int yagmur = 0;
            try { yagmur = json.getJSONObject("daily").getJSONArray("precipitation_probability_max").getInt(0); } catch(Exception e){}

            // --- RİSK ANALİZİ ---
            boolean riskVar = false;
            StringBuilder mesaj = new StringBuilder();

            if (ruzgar > limitRuzgar) {
                riskVar = true;
                mesaj.append("💨 Rüzgar sert (").append(ruzgar).append(" km/s). ");
            }
            if (yagmur > limitYagmur) {
                riskVar = true;
                mesaj.append("🌧️ Yağmur riski (%").append(yagmur).append("). ");
            }
            if (sicaklik < 4) {
                riskVar = true;
                mesaj.append("❄️ Buzlanma olabilir (").append(sicaklik).append("°). ");
            }

            // --- AKILLI ÖNERİ SİSTEMİ (Senin Kodun + İyileştirme) ---
            if (riskVar) {
                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                JSONArray hWinds = hourly.getJSONArray("windspeed_10m");
                JSONArray hRains = hourly.getJSONArray("precipitation_probability");

                int safeHour = -1;
                // Önümüzdeki 12 saate bak
                for (int i = 1; i <= 12; i++) {
                    int checkIndex = currentHour + i;
                    // Array sınırlarını aşmamak için kontrol
                    if (checkIndex < hWinds.length()) {
                        double futWind = hWinds.getDouble(checkIndex);
                        int futRain = hRains.getInt(checkIndex);

                        if (futWind <= limitRuzgar && futRain <= limitYagmur) {
                            safeHour = (currentHour + i) % 24; // 24 saati geçerse mod al
                            break;
                        }
                    }
                }

                if (safeHour != -1) {
                    mesaj.append("\n💡 Saat ").append(String.format("%02d:00", safeHour)).append(" civarı hava düzeliyor!");
                } else {
                    mesaj.append("\n🏠 Bugün motoru dinlendirsen iyi olur.");
                }
            } else {
                mesaj.append("Hava harika! Rüzgar ").append(ruzgar).append(" km/s. İyi sürüşler!");
            }

            String title = riskVar ? "⚠️ MotoWeather Uyarısı" : "🏍️ Sürüş Zamanı!";
            bildirimGonder(title, mesaj.toString());

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry(); // Hata olursa sonra tekrar dene
        }
    }

    // Güvenli Integer Çevirici (Settings patlamasın diye)
    private int safeParseInt(SharedPreferences prefs, String key, String defValue) {
        try {
            String val = prefs.getString(key, defValue);
            return Integer.parseInt(val);
        } catch (Exception e) {
            return Integer.parseInt(defValue);
        }
    }

    private void bildirimGonder(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "moto_gunluk_bildirim";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Günlük Rapor", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // İkon yoksa varsayılanı kullanır, varsa R.drawable.ic_sun yapabilirsin
        int icon = android.R.drawable.ic_menu_compass;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }
}