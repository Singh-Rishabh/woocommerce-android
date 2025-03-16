package com.cataloghub.android.ui.onboarding

import com.cataloghub.android.R
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.viewmodel.ResourceProvider
import javax.inject.Inject

class IsLocalTaskNameYourStoreCompleted @Inject constructor(
    private val selectedSite: SelectedSite,
    private val resourceProvider: ResourceProvider
) {
    operator fun invoke(): Boolean {
        val defaultStoreName = resourceProvider.getString(R.string.store_name_default)
        return selectedSite.getIfExists()?.name != defaultStoreName
    }
}
