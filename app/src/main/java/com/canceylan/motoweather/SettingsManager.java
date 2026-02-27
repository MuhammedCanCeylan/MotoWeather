package com.canceylan.motoweather;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;

public class SettingsManager {

    private static final String FILE_NAME = "MotoWeatherPrefs";

    private Context context;
    private View rootView;
    private BottomSheetDialog dialog;
    private OnSettingsChangedListener listener;

    public interface OnSettingsChangedListener {
        void onSettingsChanged();
    }

    private LinearLayout cardBaslangic, cardOrta, cardIleri, layoutGizliAyarlar;
    private SeekBar seekRuzgar, seekYagmur, seekGorus;
    private TextView txtRuzgarInfo, txtYagmurInfo, txtGorusInfo, txtDuzenlenenProfil;
    private SwitchCompat swGeceModu, swBuzKoruma, swFahrenheit;
    private MaterialButton btnKaydet;
    private TextView btnSifirla;
    private AppCompatButton btnKapatGizli;
    private TextView lblAlarmSaati, lblGeceSaatleri;
    private LinearLayout btnDilSecimi;
    private TextView txtSeciliDil;

    private String currentEditingLevel = "intermediate";
    private boolean isProgrammaticChange = false;

    // GEÇİCİ DİL DEĞİŞKENİ (Kaydet'e basana kadar burada tutacağız)
    private String pendingLanguageCode = null;

    public SettingsManager(Context context, View rootView, BottomSheetDialog dialog, OnSettingsChangedListener listener) {
        this.context = context;
        this.rootView = rootView;
        this.dialog = dialog;
        this.listener = listener;

        initViews();
        setupListeners();
        loadInitialState();
    }

    private void initViews() {
        cardBaslangic = rootView.findViewById(R.id.cardBaslangic);
        cardOrta = rootView.findViewById(R.id.cardOrta);
        cardIleri = rootView.findViewById(R.id.cardIleri);
        layoutGizliAyarlar = rootView.findViewById(R.id.layoutGizliAyarlar);
        seekRuzgar = rootView.findViewById(R.id.seekRuzgar);
        seekYagmur = rootView.findViewById(R.id.seekYagmur);
        seekGorus = rootView.findViewById(R.id.seekGorus);
        txtRuzgarInfo = rootView.findViewById(R.id.txtRuzgarInfo);
        txtYagmurInfo = rootView.findViewById(R.id.txtYagmurInfo);
        txtGorusInfo = rootView.findViewById(R.id.txtGorusInfo);
        txtDuzenlenenProfil = rootView.findViewById(R.id.txtDuzenlenenProfil);
        swGeceModu = rootView.findViewById(R.id.swGeceModu);
        swBuzKoruma = rootView.findViewById(R.id.swBuzKoruma);
        swFahrenheit = rootView.findViewById(R.id.swFahrenheit);
        btnKaydet = rootView.findViewById(R.id.btnKaydet);
        btnSifirla = rootView.findViewById(R.id.btnSifirla);
        btnKapatGizli = rootView.findViewById(R.id.btnKapatGizli);
        lblAlarmSaati = rootView.findViewById(R.id.lblAlarmSaati);
        lblGeceSaatleri = rootView.findViewById(R.id.lblGeceSaatleri);
        btnDilSecimi = rootView.findViewById(R.id.btnDilSecimi);
        txtSeciliDil = rootView.findViewById(R.id.txtSeciliDil);
    }

    private void setupListeners() {
        cardBaslangic.setOnClickListener(v -> switchLevel("beginner", false));
        cardOrta.setOnClickListener(v -> switchLevel("intermediate", false));
        cardIleri.setOnClickListener(v -> switchLevel("expert", false));

        cardBaslangic.setOnLongClickListener(v -> { switchLevel("beginner", true); return true; });
        cardOrta.setOnLongClickListener(v -> { switchLevel("intermediate", true); return true; });
        cardIleri.setOnLongClickListener(v -> { switchLevel("expert", true); return true; });

        if (btnDilSecimi != null) {
            btnDilSecimi.setOnClickListener(v -> showLanguageDialog());
        }

        if (lblAlarmSaati != null) {
            ((View) lblAlarmSaati.getParent()).setOnClickListener(v ->
                    showTimePicker("alarmSaat", "alarmDakika", lblAlarmSaati, false));
        }

        if (lblGeceSaatleri != null) {
            ((View) lblGeceSaatleri.getParent()).setOnClickListener(v ->
                    showTimePicker("geceBaslangicSaat", "geceBaslangicDakika", lblGeceSaatleri, true));
        }

        seekRuzgar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 5) { seekBar.setProgress(5); return; }
                if (fromUser) {
                    txtRuzgarInfo.setText(context.getString(R.string.etiket_ruzgar_kisa) + ": " + progress + " km/h");
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // SIFIRLAMA BUTONU
        if(btnSifirla != null) btnSifirla.setOnClickListener(v -> showResetConfirmation());

        if(btnKapatGizli != null) btnKapatGizli.setOnClickListener(v -> layoutGizliAyarlar.setVisibility(View.GONE));

        // KAYDETME BUTONU
        btnKaydet.setOnClickListener(v -> saveGeneralAndClose());
    }

    // --- DİL SEÇİM PENCERESİ ---
    private void showLanguageDialog() {
        final String[] languages = { "Türkçe", "English", "Deutsch", "Français", "Русский", "中文", "日本語", "한국어", "العربية" };
        final String[] codes = { "tr", "en", "de", "fr", "ru", "zh", "ja", "ko", "ar" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.lbl_dil_secenekleri));

        builder.setItems(languages, (dialogInterface, which) -> {
            pendingLanguageCode = codes[which];
            txtSeciliDil.setText(languages[which]);
            Toast.makeText(context, context.getString(R.string.kaydet_kapat) + "...", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    // --- SIFIRLAMA ONAY PENCERESİ (TAMAMEN İNGİLİZCE) ---
    private void showResetConfirmation() {
        new AlertDialog.Builder(context)
                .setTitle("Reset Settings?") // Başlık İngilizce
                .setMessage("All your settings will return to default. Are you sure?") // Mesaj İngilizce
                .setPositiveButton("YES", (dialog, which) -> resetToFactorySettings()) // Evet İngilizce
                .setNegativeButton("NO", null) // Hayır İngilizce
                .show();
    }

    private void resetToFactorySettings() {
        // TÜM AYARLARI SİL
        SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();

        // MOTO CONFIG dosyasını da temizle (Emin olmak için)
        context.getSharedPreferences("MOTO_FINAL_CONFIG", Context.MODE_PRIVATE).edit().clear().commit();

        Toast.makeText(context, "Reset Completed", Toast.LENGTH_SHORT).show();

        restartApp();
    }

    private void restartApp() {
        if (dialog != null) dialog.dismiss();
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).finish();
        }
        System.exit(0);
    }

    private void loadInitialState() {
        SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        currentEditingLevel = prefs.getString("selected_level_key", "intermediate");

        pendingLanguageCode = null;

        loadValuesForLevel(currentEditingLevel);
        updateCardStyles();

        if (txtSeciliDil != null) {
            String lang = prefs.getString("Language", Locale.getDefault().getLanguage());
            updateLanguageText(lang);
        }

        if (lblAlarmSaati != null) {
            int h = prefs.getInt("alarmSaat", 9);
            int m = prefs.getInt("alarmDakika", 0);
            lblAlarmSaati.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        }

        if (swGeceModu != null) swGeceModu.setChecked(prefs.getBoolean("pref_night_ride", true));
        if (swBuzKoruma != null) swBuzKoruma.setChecked(prefs.getBoolean("pref_ice_protection", true));
        if (swFahrenheit != null) swFahrenheit.setChecked(prefs.getBoolean("useFahrenheit", false));

        layoutGizliAyarlar.setVisibility(View.GONE);
    }

    private void updateLanguageText(String code) {
        String display = code.toUpperCase();
        if (code.equals("tr")) display = "Türkçe";
        else if (code.equals("en")) display = "English";
        else if (code.equals("de")) display = "Deutsch";
        else if (code.equals("fr")) display = "Français";
        else if (code.equals("ru")) display = "Русский";
        else if (code.equals("zh")) display = "中文";
        else if (code.equals("ja")) display = "日本語";
        else if (code.equals("ko")) display = "한국어";
        else if (code.equals("ar")) display = "العربية";

        if(txtSeciliDil != null) txtSeciliDil.setText(display);
    }

    private void loadValuesForLevel(String level) {
        isProgrammaticChange = true;
        SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        int wind = prefs.getInt("limit_" + level + "_wind", getDefaultWind(level));
        int rain = prefs.getInt("limit_" + level + "_rain", getDefaultRain(level));

        seekRuzgar.setProgress(wind);
        txtRuzgarInfo.setText(context.getString(R.string.etiket_ruzgar_kisa) + ": " + wind + " km/h");

        if (seekYagmur != null) {
            seekYagmur.setProgress(rain);
            txtYagmurInfo.setText(context.getString(R.string.etiket_yagmur_kisa) + ": " + rain + " mm");
        }

        isProgrammaticChange = false;
    }

    private void saveGeneralAndClose() {
        SharedPreferences.Editor editor = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit();

        boolean languageChanged = false;
        if (pendingLanguageCode != null) {
            editor.putString("Language", pendingLanguageCode);
            setAppLocale(pendingLanguageCode);
            languageChanged = true;
        }

        editor.putString("pref_rider_level", currentEditingLevel);
        editor.putString("selected_level_key", currentEditingLevel);

        editor.putInt("limit_" + currentEditingLevel + "_wind", seekRuzgar.getProgress());
        if(seekYagmur != null) editor.putInt("limit_" + currentEditingLevel + "_rain", seekYagmur.getProgress());

        if (swGeceModu != null) editor.putBoolean("pref_night_ride", swGeceModu.isChecked());
        if (swBuzKoruma != null) editor.putBoolean("pref_ice_protection", swBuzKoruma.isChecked());
        if (swFahrenheit != null) editor.putBoolean("useFahrenheit", swFahrenheit.isChecked());

        editor.commit();

        if (languageChanged) {
            restartApp();
        } else {
            if (listener != null) listener.onSettingsChanged();
            if (dialog != null) dialog.dismiss();
        }
    }

    private void setAppLocale(String localeCode) {
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        android.content.res.Resources resources = context.getResources();
        android.content.res.Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void switchLevel(String level, boolean forceOpenPanel) {
        currentEditingLevel = level;
        loadValuesForLevel(level);
        updateCardStyles();
        if (forceOpenPanel) {
            layoutGizliAyarlar.setVisibility(View.VISIBLE);
        }
    }

    private void showTimePicker(String prefHourKey, String prefMinuteKey, TextView targetTextView, boolean isNight) {
        SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        int currentHour = prefs.getInt(prefHourKey, isNight ? 22 : 9);
        int currentMinute = prefs.getInt(prefMinuteKey, 0);

        TimePickerDialog timePicker = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            prefs.edit().putInt(prefHourKey, hourOfDay).putInt(prefMinuteKey, minute).apply();
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            targetTextView.setText(isNight ? formattedTime + " - 06:00" : formattedTime);
        }, currentHour, currentMinute, true);
        timePicker.show();
    }

    private int getDefaultWind(String level) {
        if (level.equals("beginner")) return 20;
        if (level.equals("expert")) return 55;
        return 35;
    }

    private int getDefaultRain(String level) {
        if (level.equals("beginner")) return 1;
        if (level.equals("expert")) return 6;
        return 3;
    }

    private void updateCardStyles() {
        setCardAlpha(cardBaslangic, currentEditingLevel.equals("beginner"));
        setCardAlpha(cardOrta, currentEditingLevel.equals("intermediate"));
        setCardAlpha(cardIleri, currentEditingLevel.equals("expert"));
    }

    private void setCardAlpha(View card, boolean isSelected) {
        if(card == null) return;
        card.setAlpha(isSelected ? 1.0f : 0.4f);
        card.setBackgroundResource(R.drawable.bg_card_rounded);
    }
}