package com.yuehai.util.language;



import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;

/* loaded from: classes4.dex */
final class ConfigurationObserver implements ComponentCallbacks {
    private final Application mApplication;

    @Override // android.content.ComponentCallbacks
    public void onLowMemory() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void register(Application application) {
        application.registerComponentCallbacks(new ConfigurationObserver(application));
    }

    private ConfigurationObserver(Application application) {
        this.mApplication = application;
    }

    @Override // android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        if (configuration == null || MultiLanguages.isSystemLanguage(this.mApplication)) {
            return;
        }
        Application application = this.mApplication;
        LanguagesUtils.updateConfigurationChanged(application, configuration, MultiLanguages.getAppLanguage(application));
    }
}