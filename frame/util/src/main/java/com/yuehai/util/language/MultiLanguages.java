package com.yuehai.util.language;

import android.app.Application;
import android.app.LocaleManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.TextUtils;


import java.util.Locale;


public final class MultiLanguages {
    private static Application sApplication;
    private static OnLanguageListener sLanguageListener;

    public static void init(Application application) {
        init(application, true);
    }

    public static void init(final Application application, boolean z) {
        LocaleManager localeManager;
        if (sApplication != null) {
            return;
        }
        sApplication = application;
        LanguagesUtils.setDefaultLocale(application);
        if (z) {
            ActivityLanguages.inject(application);
        }
        if (Build.VERSION.SDK_INT >= 33 && (localeManager = (LocaleManager) application.getSystemService(LocaleManager.class)) != null) {
            if (isSystemLanguage(application)) {
                localeManager.setApplicationLocales(LocaleList.getEmptyLocaleList());
            } else {
                localeManager.setApplicationLocales(new LocaleList(getAppLanguage(application)));
            }
        }
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() { // from class: com.hjq.language.MultiLanguages.1
            @Override // android.os.MessageQueue.IdleHandler
            public boolean queueIdle() {
                ConfigurationObserver.register(application);
                LocaleChangeReceiver.register(application);
                return false;
            }
        });
    }

    public static Context attach(Context context) {
        Locale appLanguage = getAppLanguage(context);
        return LanguagesUtils.getLocale(context).equals(appLanguage) ? context : LanguagesUtils.attachLanguages(context, appLanguage);
    }

    public static void updateAppLanguage(Context context) {
        updateAppLanguage(context, context.getResources());
    }

    public static void updateAppLanguage(Context context, Resources resources) {
        if (resources == null || LanguagesUtils.getLocale(resources.getConfiguration()).equals(getAppLanguage(context))) {
            return;
        }
        LanguagesUtils.updateLanguages(resources, getAppLanguage(context));
    }

    public static Locale getAppLanguage(Context context) {
        if (isSystemLanguage(context)) {
            return getSystemLanguage(context);
        }
        return LanguagesConfig.readAppLanguageSetting(context);
    }

    public static boolean setAppLanguage(Context context, Locale locale) {
        LanguagesConfig.saveAppLanguageSetting(context, locale);
        if (LanguagesUtils.getLocale(context).equals(locale)) {
            return false;
        }
        Locale locale2 = LanguagesUtils.getLocale(context);
        LanguagesUtils.updateLanguages(context.getResources(), locale);
        Application application = sApplication;
        if (context != application) {
            LanguagesUtils.updateLanguages(application.getResources(), locale);
        }
        LanguagesUtils.setDefaultLocale(context);
        OnLanguageListener onLanguageListener = sLanguageListener;
        if (onLanguageListener == null) {
            return true;
        }
        onLanguageListener.onAppLocaleChange(locale2, locale);
        return true;
    }

    public static Locale getSystemLanguage(Context context) {
        return LanguagesUtils.getSystemLocale(context);
    }

    public static boolean isSystemLanguage(Context context) {
        return LanguagesConfig.isSystemLanguage(context);
    }

    public static boolean clearAppLanguage(Context context) {
        LanguagesConfig.clearLanguageSetting(context);
        if (LanguagesUtils.getLocale(context).equals(getSystemLanguage(sApplication))) {
            return false;
        }
        LanguagesUtils.updateLanguages(context.getResources(), getSystemLanguage(sApplication));
        LanguagesUtils.setDefaultLocale(context);
        Application application = sApplication;
        if (context == application) {
            return true;
        }
        LanguagesUtils.updateLanguages(application.getResources(), getSystemLanguage(sApplication));
        return true;
    }

    public static void setDefaultLanguage(Locale locale) {
        LanguagesConfig.setDefaultLanguage(locale);
    }

    public static boolean equalsLanguage(Locale locale, Locale locale2) {
        return TextUtils.equals(locale.getLanguage(), locale2.getLanguage());
    }

    public static boolean equalsCountry(Locale locale, Locale locale2) {
        return equalsLanguage(locale, locale2) && TextUtils.equals(locale.getCountry(), locale2.getCountry());
    }

    public static String getLanguageString(Context context, Locale locale, int i) {
        return generateLanguageResources(context, locale).getString(i);
    }

    public static Resources generateLanguageResources(Context context, Locale locale) {
        return LanguagesUtils.generateLanguageResources(context, locale);
    }

    public static void setOnLanguageListener(OnLanguageListener onLanguageListener) {
        sLanguageListener = onLanguageListener;
    }

    public static void setSharedPreferencesName(String str) {
        LanguagesConfig.setSharedPreferencesName(str);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static OnLanguageListener getOnLanguagesListener() {
        return sLanguageListener;
    }
}