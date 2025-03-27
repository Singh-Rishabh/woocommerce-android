package com.cataloghub.android.ui.orders.wooshippinglabels.models

import com.cataloghub.android.model.Address

data class AddressNormalizationModel(
    val address: Address,
    val normalizedAddress: Address,
    val isTrivial: Boolean
)
