package com.cataloghub.android.ui.orders.tracking

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Carrier(val name: String, val isCustom: Boolean) : Parcelable
