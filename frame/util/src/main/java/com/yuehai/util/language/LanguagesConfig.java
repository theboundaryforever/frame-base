package com.yuehai.util.language;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Locale;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes4.dex */
public final class LanguagesConfig {
    private static final String KEY_COUNTRY = "key_country";
    private static final String KEY_LANGUAGE = "key_language";
    private static volatile Locale sCurrentLocale = null;
    private static volatile Locale sDefaultLocale = null;
    private static String sSharedPreferencesName = "language_setting";

    LanguagesConfig() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setSharedPreferencesName(String str) {
        sSharedPreferencesName = str;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(sSharedPreferencesName, 0);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Locale readAppLanguageSetting(Context context) {
        if (sCurrentLocale != null) {
            return sCurrentLocale;
        }
        String string = getSharedPreferences(context).getString(KEY_LANGUAGE, "");
        String string2 = getSharedPreferences(context).getString(KEY_COUNTRY, "");
        if (!TextUtils.isEmpty(string)) {
            sCurrentLocale = new Locale(string, string2);
            return sCurrentLocale;
        }
        if (sDefaultLocale != null) {
            sCurrentLocale = sDefaultLocale;
            return sCurrentLocale;
        }
        sCurrentLocale = LanguagesUtils.getLocale(context);
        return sCurrentLocale;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void saveAppLanguageSetting(Context context, Locale locale) {
        sCurrentLocale = locale;
        getSharedPreferences(context).edit().putString(KEY_LANGUAGE, locale.getLanguage()).putString(KEY_COUNTRY, locale.getCountry()).apply();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void clearLanguageSetting(Context context) {
        sCurrentLocale = MultiLanguages.getSystemLanguage(context);
        getSharedPreferences(context).edit().remove(KEY_LANGUAGE).remove(KEY_COUNTRY).apply();
    }

    public static boolean isSystemLanguage(Context context) {
        if (sDefaultLocale != null) {
            return false;
        }
        return TextUtils.isEmpty(getSharedPreferences(context).getString(KEY_LANGUAGE, ""));
    }

    public static void setDefaultLanguage(Locale locale) {
        if (sCurrentLocale != null) {
            throw new IllegalStateException("Please set this before application initialization");
        }
        sDefaultLocale = locale;
    }
}