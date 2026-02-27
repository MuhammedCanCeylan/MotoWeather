package com.canceylan.motoweather;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SettingsDialog {

    private Activity activity;
    private SettingsListener listener;

    // MainActivity ile haberleşmek için arabirim
    public interface SettingsListener {
        void onSettingsChanged();
    }

    public SettingsDialog(Activity activity, SettingsListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    public void show() {
        try {
            // 1. Pencereyi (Dialog) Oluştur
            // Tema ile oluşturuyoruz ki stil düzgün olsun
            BottomSheetDialog dialog = new BottomSheetDialog(activity, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);

            // 2. Tasarımı (XML) Yükle
            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_settings, null);
            dialog.setContentView(view);

            // 3. Arka planı şeffaf yap (Yuvarlak köşeler düzgün görünsün diye)
            try {
                if (dialog.getWindow() != null) {
                    dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet)
                            .setBackgroundResource(android.R.color.transparent);
                }
            } catch (Exception e) { e.printStackTrace(); }

            // 4. ASIL İŞ BURADA: Bütün yönetimi 'SettingsManager'a veriyoruz!
            // (Listener'ı da oraya paslıyoruz ki kaydetme bitince haberimiz olsun)
            new SettingsManager(activity, view, dialog, new SettingsManager.OnSettingsChangedListener() {
                @Override
                public void onSettingsChanged() {
                    // SettingsManager işini bitirdiğinde burası çalışır
                    if (listener != null) {
                        listener.onSettingsChanged(); // MainActivity'ye haber ver
                    }
                }
            });

            // 5. Pencereyi Göster
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}