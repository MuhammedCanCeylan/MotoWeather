package com.canceylan.motoweather;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Language";

    // DESTEKLEDİĞİMİZ DİLLERİN LİSTESİ
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "tr", "en", "de", "fr", "ru", "zh", "ja", "ko", "ar"
    );

    // Uygulama başlarken burası çalışır
    public static Context onAttach(Context context) {
        // 1. Önce kayıtlı dil var mı diye bak
        String lang = getPersistedData(context, null);

        // 2. Eğer kayıtlı dil YOKSA (İlk kurulumsa)
        if (lang == null) {
            String systemLang = Locale.getDefault().getLanguage();

            // Telefonun dili bizim listede var mı?
            if (SUPPORTED_LANGUAGES.contains(systemLang)) {
                lang = systemLang; // Varsa onu kullan
            } else {
                lang = "en"; // Yoksa (örn: İtalyanca ise) İngilizce yap
            }
        }

        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = context.getSharedPreferences("MotoWeatherPrefs", Context.MODE_PRIVATE);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences("MotoWeatherPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}