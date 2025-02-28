package com.woocommerce.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JetpackStatus(
    val isJetpackInstalled: Boolean,
    val jetpackConnectionStatus: JetpackConnectionStatus
) : Parcelable {

    // TODO: remove these properties later
    val isJetpackConnected: Boolean
        get() = jetpackConnectionStatus is JetpackConnectionStatus.AccountConnected

    val wpComEmail: String?
        get() = (jetpackConnectionStatus as? JetpackConnectionStatus.AccountConnected)?.wpComEmail

    companion object {
        // TODO: remove this function later
        operator fun invoke(
            isJetpackInstalled: Boolean,
            isJetpackConnected: Boolean,
            wpComEmail: String?
        ): JetpackStatus {
            return JetpackStatus(
                isJetpackInstalled = isJetpackInstalled,
                jetpackConnectionStatus = if (isJetpackConnected) {
                    JetpackConnectionStatus.AccountConnected(wpComEmail.orEmpty())
                } else {
                    JetpackConnectionStatus.AccountNotConnected(
                        siteRegistrationStatus = JetpackSiteRegistrationStatus.UNKNOWN,
                        blogId = null
                    )
                }
            )
        }
    }
}

sealed interface JetpackConnectionStatus : Parcelable {
    @Parcelize
    data class AccountConnected(val wpComEmail: String) : JetpackConnectionStatus

    @Parcelize
    data class AccountNotConnected(
        val siteRegistrationStatus: JetpackSiteRegistrationStatus,
        val blogId: Long?
    ) : JetpackConnectionStatus {
        // The `isRegistered` field was added at the same time as the connection API support,
        // so we can use this as a proxy for whether the site supports the connection API.
        // See: pe5sF9-401-p2
        val supportsConnectionApi: Boolean
            get() = siteRegistrationStatus != JetpackSiteRegistrationStatus.UNKNOWN
    }
}

enum class JetpackSiteRegistrationStatus {
    UNKNOWN, REGISTERED, NOT_REGISTERED
}
