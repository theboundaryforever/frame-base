package com.yuehai.util.language;



import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


final class ActivityLanguages implements Application.ActivityLifecycleCallbacks {
    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityDestroyed(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityPaused(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStarted(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStopped(Activity activity) {
    }

    ActivityLanguages() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void inject(Application application) {
        application.registerActivityLifecycleCallbacks(new ActivityLanguages());
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityCreated(Activity activity, Bundle bundle) {
        MultiLanguages.updateAppLanguage(activity);
        MultiLanguages.updateAppLanguage(activity.getApplication());
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityResumed(Activity activity) {
        MultiLanguages.updateAppLanguage(activity);
        MultiLanguages.updateAppLanguage(activity.getApplication());
    }
}