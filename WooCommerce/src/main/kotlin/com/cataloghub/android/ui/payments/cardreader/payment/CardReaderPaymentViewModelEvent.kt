package com.cataloghub.android.ui.payments.cardreader.payment

import androidx.annotation.StringRes
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event

class ShowSnackbarInDialog(@StringRes val message: Int) : Event()

object PlayChaChing : Event()

object InteracRefundSuccessful : Event()

object ContactSupport : Event()

object EnableNfc : Event()

data class PurchaseCardReader(val url: String) : Event()

data class PrintReceipt(val receiptUrl: String, val documentName: String) : Event()
