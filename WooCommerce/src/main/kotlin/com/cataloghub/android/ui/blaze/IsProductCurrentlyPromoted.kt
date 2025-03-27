package com.cataloghub.android.ui.blaze

import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.blaze.CampaignStatusUi.Active
import com.cataloghub.android.ui.blaze.CampaignStatusUi.InModeration
import com.cataloghub.android.ui.blaze.CampaignStatusUi.Scheduled
import org.wordpress.android.fluxc.store.blaze.BlazeCampaignsStore
import javax.inject.Inject

class IsProductCurrentlyPromoted @Inject constructor(
    private val blazeStore: BlazeCampaignsStore,
    private val selectedSite: SelectedSite
) {
    suspend operator fun invoke(productId: String): Boolean {
        return blazeStore.getBlazeCampaigns(selectedSite.get())
            .filter {
                CampaignStatusUi.fromString(it.uiStatus) == InModeration ||
                    CampaignStatusUi.fromString(it.uiStatus) == Scheduled ||
                    CampaignStatusUi.fromString(it.uiStatus) == Active
            }
            .any { it.targetUrn?.split(":")?.last() == productId }
    }
}
