package com.woocommerce.android.ui.orders.wooshippinglabels.customs.domain

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ShouldRequireITN @Inject constructor() {
    operator fun invoke(): Flow<Boolean> {
        return flowOf(false)
    }
}
