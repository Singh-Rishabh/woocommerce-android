package com.cataloghub.android.ui.sitepicker.sitevisibility

import com.cataloghub.android.ui.sitepicker.SitePickerRepository
import kotlinx.coroutines.flow.first
import org.wordpress.android.fluxc.model.SiteModel
import javax.inject.Inject

class GetWooVisibleSites @Inject constructor(
    private val sitePickerRepository: SitePickerRepository,
    private val visibleSitesDataStore: VisibleWooSitesDataStore
) {
    suspend operator fun invoke(): List<SiteModel> =
        sitePickerRepository.getSites()
            .filter { it.hasWooCommerce && isSiteVisible(it.siteId) }

    private suspend fun isSiteVisible(siteId: Long): Boolean {
        return visibleSitesDataStore.isSiteVisible(siteId).first()
    }
}
