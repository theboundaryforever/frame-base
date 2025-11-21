package com.yuehai.util.uri

import androidx.core.net.toUri


sealed class ParsedUrlResult {
    data class HttpWebUrl(val url: String) : ParsedUrlResult()
    data class SchemeResult(val parseUrlParam: Pair<String, Map<String, String>>) :
        ParsedUrlResult()
}

object parseUrlSchemeParams {

    fun parse(url: String): ParsedUrlResult {
        return try {
            val uri = url.toUri()
            val scheme = uri.scheme?.lowercase() ?: return ParsedUrlResult.HttpWebUrl(url)

            return if (scheme.startsWith("http")) {
                ParsedUrlResult.HttpWebUrl(url)
            } else {
                val type = uri.host ?: return ParsedUrlResult.HttpWebUrl(url)
                val paramMap = uri.queryParameterNames.associateWith { key ->
                    uri.getQueryParameter(key).orEmpty()
                }
                ParsedUrlResult.SchemeResult(Pair(type, paramMap))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ParsedUrlResult.HttpWebUrl(url)
        }
    }
}


//装扮商城页
const val BANNER_ACTIVITY_GO_TO_MALL = "mall"
const val BANNER_ACTIVITY_GO_TO_WALLET = "wallet"
const val BANNER_ACTIVITY_GO_TO_ROOM_VOICE_UID = "voice_uid"
const val BANNER_ACTIVITY_GO_TO_ROOM = "voice"

const val BANNER_ACTIVITY_GO_TO_RECHARGE="recharge_agent_list"
