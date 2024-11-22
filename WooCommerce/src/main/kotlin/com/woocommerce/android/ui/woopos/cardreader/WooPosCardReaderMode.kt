package com.woocommerce.android.ui.woopos.cardreader

import android.os.Parcelable
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderFlowParam
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderType
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class WooPosCardReaderMode(
    val cardReaderFlowParam: CardReaderFlowParam,
    val cardReaderType: CardReaderType
) : Parcelable {
    @Parcelize
    data object Connection : WooPosCardReaderMode(
        cardReaderFlowParam = CardReaderFlowParam.WooPosConnection,
        cardReaderType = CardReaderType.EXTERNAL
    )
}
