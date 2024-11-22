package com.woocommerce.android.ui.woopos.cardreader

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class WooPosCardReaderConnectionStatus : Parcelable {
    data object Success : WooPosCardReaderConnectionStatus()
    data object Failure : WooPosCardReaderConnectionStatus()
    data object Unknown : WooPosCardReaderConnectionStatus()
}
