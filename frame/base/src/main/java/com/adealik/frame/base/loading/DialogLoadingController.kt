package com.adealik.frame.base.loading


import androidx.fragment.app.DialogFragment
import com.kaopiz.kprogresshud.KProgressHUD

class DialogLoadingController(
    fragment: DialogFragment
) : BaseLoadingController(
    createHud = {
        KProgressHUD.create(fragment.requireContext())
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
    }
)
