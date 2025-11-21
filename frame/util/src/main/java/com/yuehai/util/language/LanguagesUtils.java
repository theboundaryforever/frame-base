package com.yuehai.util.language;


import android.app.LocaleManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/* loaded from: classes4.dex */
final class LanguagesUtils {
    LanguagesUtils() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Locale getLocale(Context context) {
        return getLocale(context.getResources().getConfiguration());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Locale getLocale(Configuration configuration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return configuration.getLocales().get(0);
        }
        return configuration.locale;
    }

    static void setLocale(Configuration configuration, Locale locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(new LocaleList(locale));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Locale getSystemLocale(Context context) {
        LocaleManager localeManager;
        if (Build.VERSION.SDK_INT >= 33 && (localeManager = (LocaleManager) context.getSystemService(LocaleManager.class)) != null) {
            return localeManager.getSystemLocales().get(0);
        }
        return getLocale(Resources.getSystem().getConfiguration());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setDefaultLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.setDefault(context.getResources().getConfiguration().getLocales());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Context attachLanguages(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        setLocale(configuration, locale);
        Context createConfigurationContext = context.createConfigurationContext(configuration);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return createConfigurationContext;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void updateLanguages(Resources resources, Locale locale) {
        Configuration configuration = resources.getConfiguration();
        setLocale(configuration, locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void updateConfigurationChanged(Context context, Configuration configuration, Locale locale) {
        Configuration configuration2 = new Configuration(configuration);
        setLocale(configuration2, locale);
        Resources resources = context.getResources();
        resources.updateConfiguration(configuration2, resources.getDisplayMetrics());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static Resources generateLanguageResources(Context context, Locale locale) {
        Configuration configuration = new Configuration();
        setLocale(configuration, locale);
        return context.createConfigurationContext(configuration).getResources();
    }
}