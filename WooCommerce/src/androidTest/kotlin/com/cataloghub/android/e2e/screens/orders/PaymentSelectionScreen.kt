package com.cataloghub.android.e2e.screens.orders

import com.cataloghub.android.R
import com.cataloghub.android.e2e.helpers.util.Screen

class PaymentSelectionScreen : Screen {
    constructor() : super(R.id.clCardReader)

    fun chooseCardPayment(): CardReaderPaymentScreen {
        clickOn(R.id.clCardReader)
        return CardReaderPaymentScreen()
    }

    fun goBackToOrderDetails(): SingleOrderScreen {
        pressBack()
        return SingleOrderScreen()
    }
}
