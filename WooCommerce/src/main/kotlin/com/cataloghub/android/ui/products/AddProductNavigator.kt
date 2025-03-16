package com.cataloghub.android.ui.products

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.cataloghub.android.extensions.isEligibleForAI
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.tools.SelectedSite
import javax.inject.Inject

class AddProductNavigator @Inject constructor(
    private val selectedSite: SelectedSite
) {
    fun NavController.navigateToAddProducts(
        aiBottomSheetAction: NavDirections,
        typesBottomSheetAction: NavDirections
    ) {
        if (selectedSite.get().isEligibleForAI) {
            navigateSafely(aiBottomSheetAction)
        } else {
            navigateSafely(typesBottomSheetAction)
        }
    }
}
