package com.woocommerce.android.ui.orders

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.model.WCSettingsModel
import org.wordpress.android.fluxc.store.WooCommerceStore
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class IsStoreCurrencyMisMatchTest : BaseUnitTest() {
    private val wooCommerceStore: WooCommerceStore = mock()
    private val selectedSite: SelectedSite = mock()
    val wcSettingsModel = mock<WCSettingsModel> {
        on { currencyCode } doReturn "USD"
    }
    private val isStoreCurrencyMatch: IsStoreCurrencyMatch = IsStoreCurrencyMatch(wooCommerceStore, selectedSite)

    @Test
    fun `when order currency is different than store, then return data model with correct data`() = testBlocking {
        whenever(wooCommerceStore.getSiteSettings(selectedSite.get())).thenReturn(wcSettingsModel)

        assertFalse(isStoreCurrencyMatch("INR").isMatch)
        assertThat(isStoreCurrencyMatch("INR").storeCurrency).isEqualTo("USD")
    }
}
