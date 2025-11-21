package com.yuehai.util.oss

import android.content.Context
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider

object OSSManager {

    private var ossClient: OSSClient? = null

    fun init(context: Context, ak: String, sk: String, endpoint: String) {
        if (ossClient == null) {
            val credentialProvider = OSSPlainTextAKSKCredentialProvider(ak, sk)
            ossClient = OSSClient(/* context = */ context.applicationContext, /* endpoint = */
                endpoint, /* credentialProvider = */
                credentialProvider)
        }
    }

    fun getClient(): OSSClient? {
        return ossClient
    }
}
