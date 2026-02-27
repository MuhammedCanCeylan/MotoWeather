package com.canceylan.motoweather;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;

public class InitialSetupDialog {

    private Context context;
    private Runnable onComplete;
    private SharedPreferences prefs; // Ana Ayarlar Dosyası

    // Seçilen Değerler (Varsayılanlar)
    private String selectedLevel = "Orta";
    private String selectedMotor = "Street";

    // Görünümler (5 Motor Tipi)
    private LinearLayout cardScooter, cardNaked, cardSport, cardTouring, cardAdv;

    // Görünümler (3 Seviye)
    private LinearLayout cardBaslangic, cardOrta, cardIleri;

    private Button btnTamamla;

    public InitialSetupDialog(Context context, Runnable onComplete) {
        this.context = context;
        this.onComplete = onComplete;
        // MainActivity'nin baktığı dosya ile aynı dosyayı açıyoruz
        this.prefs = context.getSharedPreferences("MotoWeatherPrefs", Context.MODE_PRIVATE);
    }

    public void show() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_initial_setup, null);
            builder.setView(view);
            builder.setCancelable(false); // Kapatılamaz, mecburen tamamlayacak

            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            initViews(view, dialog);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews(View view, Dialog dialog) {
        // --- 1. MOTOR KARTLARINI BUL ---
        cardScooter = view.findViewById(R.id.cardScooter);
        cardNaked   = view.findViewById(R.id.cardNaked);
        cardSport   = view.findViewById(R.id.cardSport);
        cardTouring = view.findViewById(R.id.cardTouring);
        cardAdv     = view.findViewById(R.id.cardAdv);

        // Tıklama Olayı
        View.OnClickListener motorListener = v -> {
            int id = v.getId();
            if (id == R.id.cardScooter) selectedMotor = "Scooter";
            else if (id == R.id.cardNaked) selectedMotor = "Street";
            else if (id == R.id.cardSport) selectedMotor = "Sport";
            else if (id == R.id.cardTouring) selectedMotor = "Touring";
            else if (id == R.id.cardAdv) selectedMotor = "Adv";

            updateMotorUI();
        };

        if(cardScooter != null) cardScooter.setOnClickListener(motorListener);
        if(cardNaked != null) cardNaked.setOnClickListener(motorListener);
        if(cardSport != null) cardSport.setOnClickListener(motorListener);
        if(cardTouring != null) cardTouring.setOnClickListener(motorListener);
        if(cardAdv != null) cardAdv.setOnClickListener(motorListener);

        // --- 2. SEVİYE KARTLARI ---
        cardBaslangic = view.findViewById(R.id.cardBaslangicSetting);
        cardOrta = view.findViewById(R.id.cardOrtaSetting);
        cardIleri = view.findViewById(R.id.cardIleriSetting);

        View.OnClickListener levelListener = v -> {
            int id = v.getId();
            if (id == R.id.cardOrtaSetting) selectedLevel = "Orta";
            else if (id == R.id.cardIleriSetting) selectedLevel = "Ileri";
            else selectedLevel = "Baslangic";
            updateLevelUI();
        };

        if(cardBaslangic != null) cardBaslangic.setOnClickListener(levelListener);
        if(cardOrta != null) cardOrta.setOnClickListener(levelListener);
        if(cardIleri != null) cardIleri.setOnClickListener(levelListener);

        // --- 3. TAMAMLA BUTONU ---
        btnTamamla = view.findViewById(R.id.btnKurulumuTamamla);
        if(btnTamamla != null) {
            btnTamamla.setOnClickListener(v -> saveAndClose(dialog));
        }

        // Açılışta seçili olanı boya
        updateMotorUI();
        updateLevelUI();
    }

    private void updateMotorUI() {
        if(cardScooter != null) updateCardStyle(cardScooter, selectedMotor.equals("Scooter"));
        if(cardNaked != null)   updateCardStyle(cardNaked, selectedMotor.equals("Street"));
        if(cardSport != null)   updateCardStyle(cardSport, selectedMotor.equals("Sport"));
        if(cardTouring != null) updateCardStyle(cardTouring, selectedMotor.equals("Touring"));
        if(cardAdv != null)     updateCardStyle(cardAdv, selectedMotor.equals("Adv"));
    }

    private void updateLevelUI() {
        if(cardBaslangic == null) return;
        updateCardStyle(cardBaslangic, selectedLevel.equals("Baslangic"));
        updateCardStyle(cardOrta, selectedLevel.equals("Orta"));
        updateCardStyle(cardIleri, selectedLevel.equals("Ileri"));
    }

    private void updateCardStyle(LinearLayout card, boolean isSelected) {
        if(card != null) {
            if (isSelected) {
                card.setAlpha(1.0f);
                card.setBackgroundResource(R.drawable.bg_card_rounded); // Parlak çerçeve
            } else {
                card.setAlpha(0.4f);
                card.setBackgroundResource(R.drawable.bg_card_unselected); // Sönük çerçeve
            }
        }
    }

    private void saveAndClose(Dialog dialog) {
        try {
            // 1. MOTOR VE GÜVENLİK AYARLARINI KAYDET
            SharedPreferences enginePrefs = context.getSharedPreferences("MOTO_FINAL_CONFIG", Context.MODE_PRIVATE);
            SharedPreferences.Editor engEditor = enginePrefs.edit();

            // Limitleri Ayarla
            int ruzgar = 20, yagmur = 1, gorus = 8;
            boolean gece = false;
            String levelCode = "beginner";

            if(selectedLevel.equals("Orta")) {
                ruzgar = 35; yagmur = 3; gorus = 5; gece = true;
                levelCode = "intermediate";
            }
            else if(selectedLevel.equals("Ileri")) {
                ruzgar = 50; yagmur = 5; gorus = 2; gece = true;
                levelCode = "expert";
            }

            engEditor.putString("pref_wind_limit", String.valueOf(ruzgar));
            engEditor.putString("pref_rain_limit", String.valueOf(yagmur));
            engEditor.putString("pref_visibility_limit", String.valueOf(gorus));
            engEditor.putString("pref_rider_level", levelCode);
            engEditor.putBoolean("pref_night_ride", !gece);

            int typeCode = 2;
            if (selectedMotor.equals("Scooter")) typeCode = 1;
            else if (selectedMotor.equals("Street")) typeCode = 2;
            else if (selectedMotor.equals("Sport")) typeCode = 3;
            else if (selectedMotor.equals("Touring")) typeCode = 4;
            else if (selectedMotor.equals("Adv")) typeCode = 5;

            engEditor.putInt("pref_motor_type", typeCode);
            engEditor.commit();

            // -----------------------------------------------------------
            // 2. KRİTİK NOKTA: KURULUMUN BİTTİĞİNİ ANA HAFIZAYA YAZ!
            // MainActivity burayı kontrol ediyor ("setupDone")
            // -----------------------------------------------------------
            SharedPreferences.Editor mainEditor = prefs.edit();
            mainEditor.putBoolean("setupDone", true);
            mainEditor.commit(); // Kesin kaydet

            // 3. KAPAT VE DEVAM ET
            dialog.dismiss();
            if (onComplete != null) onComplete.run();

        } catch (Exception e) { e.printStackTrace(); }
    }
}