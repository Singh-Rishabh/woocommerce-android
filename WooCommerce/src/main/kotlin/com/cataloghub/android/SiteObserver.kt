package com.cataloghub.android

import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.config.WPComRemoteFeatureFlagRepository
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.tools.SiteConnectionType
import com.cataloghub.android.tools.connectionType
import com.cataloghub.android.ui.common.environment.EnvironmentRepository
import com.cataloghub.android.util.GetAppVersionName
import com.cataloghub.android.util.WooLog
import com.cataloghub.android.util.dispatchAndAwait
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.WCOrderActionBuilder
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.SiteStore
import org.wordpress.android.fluxc.store.WCOrderStore.FetchOrderStatusOptionsPayload
import org.wordpress.android.fluxc.store.WCOrderStore.OnOrderStatusOptionsChanged
import org.wordpress.android.fluxc.store.WooCommerceStore
import javax.inject.Inject

/**
 * A utility class that can be used to force fetching data specific to current site,
 * the fetching will occur on app launch, and on each site switching
 */
class SiteObserver @Inject constructor(
    private val selectedSite: SelectedSite,
    private val wooCommerceStore: WooCommerceStore,
    private val environmentRepository: EnvironmentRepository,
    private val featureFlagRepository: WPComRemoteFeatureFlagRepository,
    private val siteStore: SiteStore,
    private val appPrefs: AppPrefsWrapper,
    private val analyticsTracker: AnalyticsTrackerWrapper,
    private val dispatcher: Dispatcher,
    private val appVersionName: GetAppVersionName,
) {
    suspend fun observeAndUpdateSelectedSiteData() {
        selectedSite.observe()
            .filterNotNull()
            .distinctUntilChanged { old, new -> new.id == old.id }
            .collectLatest { site ->
                coroutineScope {
                    launch { fetchPlugins(site) }

                    launch { fetchStoreId(site) }

                    launch { fetchOrderStatusOptions(site) }

                    launch { fetchRemoteFeatureFlags() }

                    if (site.connectionType == SiteConnectionType.ApplicationPasswords) {
                        launch { checkIfSiteIsWPComSuspended(site) }
                    }
                }
            }
    }

    private suspend fun fetchPlugins(site: SiteModel) {
        WooLog.d(WooLog.T.UTILS, "Fetch plugins for site ${site.name}")
        wooCommerceStore.fetchSitePlugins(site)
    }

    private suspend fun fetchStoreId(site: SiteModel) {
        // Makes sure the store ID is fetched for the site.
        environmentRepository.fetchOrGetStoreID(site)
            .fold(
                onSuccess = { storeID ->
                    WooLog.d(WooLog.T.UTILS, "Fetched StoreID $storeID for site ${site.name}")
                },
                onFailure = { error ->
                    WooLog.e(WooLog.T.UTILS, "Error fetching StoreID for site ${site.name}: ${error.message}")
                }
            )
    }

    private suspend fun fetchOrderStatusOptions(site: SiteModel) {
        WooLog.d(WooLog.T.UTILS, "Fetch status options for site ${site.name}")
        dispatcher.dispatchAndAwait<FetchOrderStatusOptionsPayload, OnOrderStatusOptionsChanged>(
            WCOrderActionBuilder.newFetchOrderStatusOptionsAction(
                FetchOrderStatusOptionsPayload(site)
            )
        )
    }

    private suspend fun fetchRemoteFeatureFlags() {
        WooLog.d(WooLog.T.UTILS, "Fetching remote feature flags")
        featureFlagRepository.fetchAndCacheFeatureFlags(appVersionName())
    }

    private suspend fun checkIfSiteIsWPComSuspended(site: SiteModel) {
        val isSiteSuspended = siteStore.fetchConnectSiteInfoSync(site.url).let {
            when {
                !it.isError -> false
                it.error.type == SiteStore.SiteErrorType.WPCOM_SITE_SUSPENDED -> true
                else -> {
                    WooLog.e(WooLog.T.LOGIN, "Error fetching site info for ${site.name}: ${it.error}")
                    null
                }
            }
        } ?: return

        WooLog.d(WooLog.T.LOGIN, "Site ${site.url} is WPCom suspended: $isSiteSuspended")
        appPrefs.isSiteWPComSuspended = isSiteSuspended
        if (isSiteSuspended) {
            analyticsTracker.track(
                stat = AnalyticsEvent.BLACK_FLAGGED_WEBSITE_DETECTED,
                properties = mapOf("event" to "app_launch")
            )
        }
    }
}
