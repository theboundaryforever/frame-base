package com.yuehai


import com.yuehai.data.collection.path.Constants
import com.yuehai.network.BuildConfig

object UrlConfig {
    val Level_Center = if (BuildConfig.DEBUG) {
        Constants.yoppoWebUrl + "vue-h5/#/level"
    } else {
        Constants.yoppoWebUrl + "h5/vue-h5/index.html#/level"
    }

    val privacyPolicy = Constants.yoopoUrl + "api/article/content/id/7.html"
    val agreementPolicy = Constants.yoopoUrl + "api/article/content/id/33.html"

    //vip
    val VIP_LEVEL = if (BuildConfig.DEBUG) {
        Constants.yoppoWebUrl + "vue-h5/#/vip"
    } else {
        Constants.yoppoWebUrl + "h5/vue-h5/index.html#/vip"
    }
    //

    val CREATE_AGENCY = if (BuildConfig.DEBUG) {
        Constants.yoppoWebUrl + "/vue-h5/#/create-agency"
    } else {
        Constants.yoppoWebUrl + "/vue-h5/#/create-agency"
    }

    val AGENCY_ADMIN_CENTER = if (BuildConfig.DEBUG) {
        Constants.yoopolWebUrl + "/vue-h5/#/admin-center"
    } else {
        Constants.yoppoWebUrl + "/vue-h5/#/admin-center"
    }
}