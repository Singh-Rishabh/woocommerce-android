package com.cataloghub.android.ui.moremenu

import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.ui.moremenu.MoreMenuNewFeature.Payments
import com.cataloghub.android.ui.payments.taptopay.TapToPayAvailabilityStatus
import com.cataloghub.android.ui.payments.taptopay.isAvailable
import dagger.Reusable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@Reusable
class MoreMenuNewFeatureHandler @Inject constructor(
    private val appPrefsWrapper: AppPrefsWrapper,
    private val tapToPayAvailabilityStatus: TapToPayAvailabilityStatus,
) {
    val moreMenuNewFeaturesAvailable = appPrefsWrapper.observePrefs()
        .onStart { emit(Unit) }
        .map {
            when {
                appPrefsWrapper.isUserSeenNewFeatureOnMoreScreen() -> emptyList()
                tapToPayAvailabilityStatus().isAvailable -> listOf(Payments)
                else -> emptyList()
            }
        }

    val moreMenuPaymentsFeatureWasClicked = appPrefsWrapper.observePrefs()
        .onStart { emit(Unit) }
        .map { appPrefsWrapper.isPaymentsIconWasClickedOnMoreScreen() }

    fun markPaymentsIconAsClicked() {
        appPrefsWrapper.setPaymentsIconWasClickedOnMoreScreen()
    }

    fun markNewFeatureAsSeen() {
        appPrefsWrapper.setUserSeenNewFeatureOnMoreScreen()
    }
}

enum class MoreMenuNewFeature {
    Payments
}
