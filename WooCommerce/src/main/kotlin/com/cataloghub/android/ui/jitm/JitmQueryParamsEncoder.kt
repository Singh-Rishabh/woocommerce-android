package com.cataloghub.android.ui.jitm

import com.cataloghub.android.BuildConfig
import com.cataloghub.android.util.BuildConfigWrapper
import com.cataloghub.android.util.DeviceFeatures
import com.cataloghub.android.util.DeviceInfoWrapper
import java.net.URLEncoder
import javax.inject.Inject

class JitmQueryParamsEncoder @Inject constructor(
    private val buildConfigWrapper: BuildConfigWrapper,
    private val deviceInfo: DeviceInfoWrapper,
    private val deviceFeatures: DeviceFeatures,
) {
    fun getEncodedQueryParams(): String {
        val init = if (buildConfigWrapper.debug) "build_type=developer" else ""
        val query = StringBuilder(init)
            .appendKeyValue("platform", "android")
            .appendKeyValue("version", BuildConfig.VERSION_NAME)
            .appendKeyValue("os_version", deviceInfo.osVersionCode.toString())
            .appendKeyValue("device", deviceInfo.name)
            .appendKeyValue("nfc", deviceFeatures.isNFCAvailable().toString())
            .appendKeyValue("locale", deviceInfo.locale ?: "unknown")
            .replace("\\s".toRegex(), "_")
            .removePrefix("&")
        return URLEncoder.encode(query, Charsets.UTF_8.name())
    }

    private fun StringBuilder.appendKeyValue(key: String, value: String) =
        append("&").append(key).append("=").append(value)
}
