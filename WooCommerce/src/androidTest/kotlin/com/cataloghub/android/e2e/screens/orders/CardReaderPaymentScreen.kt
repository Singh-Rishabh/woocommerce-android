package com.cataloghub.android.e2e.screens.orders

import com.cataloghub.android.R
import com.cataloghub.android.e2e.helpers.util.Screen

class CardReaderPaymentScreen : Screen(R.id.header_label) {
    fun goBackToPaymentSelection(): PaymentSelectionScreen {
        pressBack()
        return PaymentSelectionScreen()
    }
}
