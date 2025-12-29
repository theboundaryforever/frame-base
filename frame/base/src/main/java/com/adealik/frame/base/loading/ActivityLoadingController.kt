package com.adealik.frame.base.loading


import androidx.appcompat.app.AppCompatActivity
import com.kaopiz.kprogresshud.KProgressHUD

class ActivityLoadingController(
    activity: AppCompatActivity
) : BaseLoadingController(
    createHud = {
        KProgressHUD.create(activity)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
    }
)

