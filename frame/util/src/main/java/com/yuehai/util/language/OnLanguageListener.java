package com.yuehai.util.language;

import java.util.Locale;

public interface OnLanguageListener {
    void onAppLocaleChange(Locale locale, Locale locale2);

    void onSystemLocaleChange(Locale locale, Locale locale2);
}