package com.woocommerce.android.ui.woopos

import com.woocommerce.android.extensions.semverCompareTo
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderOnboardingChecker
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderOnboardingState
import com.woocommerce.android.ui.payments.cardreader.onboarding.PluginType
import com.woocommerce.android.ui.woopos.featureflags.WooPosIsPaymentsOnboardingSupportedInternally
import com.woocommerce.android.util.GetWooCorePluginCachedVersion
import com.woocommerce.android.util.IsRemoteFeatureFlagEnabled
import com.woocommerce.android.util.RemoteFeatureFlag.WOO_POS
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.wordpress.android.fluxc.store.WooCommerceStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WooPosIsEnabled @Inject constructor(
    private val selectedSite: SelectedSite,
    private val isScreenSizeAllowed: WooPosIsScreenSizeAllowed,
    private val getWooCoreVersion: GetWooCorePluginCachedVersion,
    private val cardReaderOnboardingChecker: CardReaderOnboardingChecker,
    private val wooCommerceStore: WooCommerceStore,
    private val isRemoteFeatureFlagEnabled: IsRemoteFeatureFlagEnabled,
    private val isWooPosPaymentsOnboardingSupportedInternally: WooPosIsPaymentsOnboardingSupportedInternally,
) {
    @Suppress("ReturnCount")
    suspend operator fun invoke(): Boolean = coroutineScope {
        val selectedSite = selectedSite.getOrNull() ?: return@coroutineScope false

        val onboardingStatusDeferred = async { cardReaderOnboardingChecker.getOnboardingState() }
        val siteSettingsDeferred =  async { wooCommerceStore.getSiteSettings(selectedSite) }

        if (!isRemoteFeatureFlagEnabled(WOO_POS)) return@coroutineScope false
        if (!isScreenSizeAllowed()) return@coroutineScope false
        if (!isWooCoreSupportsOrderAutoDraftsAndExtraPaymentsProps()) return@coroutineScope false

        val onboardingStatus = onboardingStatusDeferred.await()
        if (onboardingStatus.preferredPlugin != PluginType.WOOCOMMERCE_PAYMENTS) return@coroutineScope false

        if (!isWooPosPaymentsOnboardingSupportedInternally()) {
            if (!isIPPOnboardingCompleted(onboardingStatus)) return@coroutineScope false
        }

        val siteSettings = siteSettingsDeferred.await() ?: return@coroutineScope false
        if (siteSettings.countryCode.lowercase() !in SUPPORTED_COUNTRIES) return@coroutineScope false
        if (siteSettings.currencyCode.lowercase() !in SUPPORTED_CURRENCIES) return@coroutineScope false

        return@coroutineScope true
    }

    private fun isIPPOnboardingCompleted(onboardingStatus: CardReaderOnboardingState): Boolean =
        when (onboardingStatus) {
            CardReaderOnboardingState.ChoosePaymentGatewayProvider,
            is CardReaderOnboardingState.CashOnDeliveryDisabled,
            CardReaderOnboardingState.GenericError,
            CardReaderOnboardingState.NoConnectionError,
            is CardReaderOnboardingState.PluginInTestModeWithLiveStripeAccount,
            is CardReaderOnboardingState.PluginIsNotSupportedInTheCountry,
            is CardReaderOnboardingState.PluginUnsupportedVersion,
            is CardReaderOnboardingState.SetupNotCompleted,
            is CardReaderOnboardingState.StoreCountryNotSupported,
            is CardReaderOnboardingState.StripeAccountCountryNotSupported,
            is CardReaderOnboardingState.StripeAccountOverdueRequirement,
            is CardReaderOnboardingState.StripeAccountRejected,
            is CardReaderOnboardingState.StripeAccountUnderReview,
            is CardReaderOnboardingState.StripeAccountPendingRequirement,
            CardReaderOnboardingState.WcpayNotActivated,
            CardReaderOnboardingState.WcpayNotInstalled -> false

            is CardReaderOnboardingState.OnboardingCompleted -> true
        }

    private fun isWooCoreSupportsOrderAutoDraftsAndExtraPaymentsProps(): Boolean {
        val wooCoreVersion = getWooCoreVersion() ?: return false
        return wooCoreVersion.semverCompareTo(WC_VERSION_SUPPORTS_ORDER_AUTO_DRAFTS_AND_EXTRA_PAYMENTS_PROPS) >= 0
    }

    private companion object {
        const val WC_VERSION_SUPPORTS_ORDER_AUTO_DRAFTS_AND_EXTRA_PAYMENTS_PROPS = "6.6.0"

        val SUPPORTED_COUNTRIES = listOf("us")
        val SUPPORTED_CURRENCIES = listOf("usd")
    }
}
