package com.cataloghub.android.ui.prefs.domain

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.extensions.getTitle
import com.cataloghub.android.model.UiString.UiStringText
import com.cataloghub.android.support.help.HelpOrigin
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.prefs.domain.DomainSuggestionsRepository.DomainSuggestion
import com.cataloghub.android.ui.prefs.domain.DomainSuggestionsRepository.DomainSuggestion.Paid
import com.cataloghub.android.ui.prefs.domain.DomainSuggestionsRepository.DomainSuggestion.Premium
import com.cataloghub.android.util.CurrencyFormatter
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowUiStringSnackbar
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DomainSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    domainSuggestionsRepository: DomainSuggestionsRepository,
    currencyFormatter: CurrencyFormatter,
    selectedSite: SelectedSite,
    private val appPrefsWrapper: AppPrefsWrapper,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    private val domainChangeRepository: DomainChangeRepository
) : DomainSuggestionsViewModel(
    savedStateHandle = savedStateHandle,
    domainSuggestionsRepository = domainSuggestionsRepository,
    currencyFormatter = currencyFormatter,
    initialQuery = selectedSite.get().getTitle(""),
    searchOnlyFreeDomains = false,
    isFreeCreditAvailable = savedStateHandle[KEY_IS_FREE_CREDIT_AVAILABLE]!!,
    freeUrl = savedStateHandle[KEY_FREE_DOMAIN_URL]
) {
    companion object {
        const val KEY_FREE_DOMAIN_URL = "freeDomainUrl"
    }
    override val helpOrigin = HelpOrigin.DOMAIN_CHANGE

    private val navArgs: DomainSearchFragmentArgs by savedStateHandle.navArgs()

    init {
        analyticsTrackerWrapper.track(
            AnalyticsEvent.CUSTOM_DOMAINS_STEP,
            mapOf(
                AnalyticsTracker.KEY_SOURCE to appPrefsWrapper.getCustomDomainsSourceAsString(),
                AnalyticsTracker.KEY_STEP to AnalyticsTracker.VALUE_STEP_PICKER
            )
        )
    }

    override fun navigateToNextStep(selectedDomain: DomainSuggestion) {
        when (selectedDomain) {
            is Premium -> {
                createShoppingCart(selectedDomain.name, selectedDomain.productId, selectedDomain.supportsPrivacy)
            }
            is Paid -> {
                if (navArgs.isFreeCreditAvailable) {
                    triggerEvent(NavigateToDomainRegistration(selectedDomain.name, selectedDomain.productId))
                } else {
                    createShoppingCart(selectedDomain.name, selectedDomain.productId, selectedDomain.supportsPrivacy)
                }
            }
            else -> throw UnsupportedOperationException("This domain search is only for paid domains")
        }
    }

    private fun createShoppingCart(domain: String, productId: Int, supportsPrivacy: Boolean) {
        launch {
            val result = domainChangeRepository.addDomainToCart(productId, domain, supportsPrivacy)

            if (!result.isError) {
                triggerEvent(ShowCheckoutWebView(domain))
            } else {
                triggerEvent(
                    ShowUiStringSnackbar(UiStringText(result.error.message ?: "Unable to create a shopping cart"))
                )
                triggerEvent(Exit)

                analyticsTrackerWrapper.track(
                    AnalyticsEvent.CUSTOM_DOMAIN_PURCHASE_FAILED,
                    mapOf(
                        AnalyticsTracker.KEY_SOURCE to appPrefsWrapper.getCustomDomainsSourceAsString(),
                        AnalyticsTracker.KEY_USE_DOMAIN_CREDIT to false,
                        AnalyticsTracker.KEY_ERROR_CONTEXT to this::class.java.simpleName,
                        AnalyticsTracker.KEY_ERROR_TYPE to result.error.type.name,
                        AnalyticsTracker.KEY_ERROR_DESC to result.error.message
                    )
                )
            }
        }
    }

    data class NavigateToDomainRegistration(val domain: String, val productId: Int) : MultiLiveEvent.Event()
    data class ShowCheckoutWebView(val domain: String) : MultiLiveEvent.Event()
}
