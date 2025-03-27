package com.cataloghub.android.ui.orders.wooshippinglabels.address.origin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.model.Location
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.orders.details.editing.address.LocationCode
import com.cataloghub.android.ui.orders.wooshippinglabels.address.WooShippingEditAddressScreen
import com.cataloghub.android.ui.orders.wooshippinglabels.address.origin.WooShippingEditOriginViewModel.ShowCountrySelector
import com.cataloghub.android.ui.orders.wooshippinglabels.address.origin.WooShippingEditOriginViewModel.ShowStateSelector
import com.cataloghub.android.ui.searchfilter.SearchFilterItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WooShippingEditOriginAddressFragment : BaseFragment() {
    private companion object {
        const val SELECT_COUNTRY_REQUEST = "select_address_country_request"
        const val SELECT_STATE_REQUEST = "select_address_state_request"
    }

    private val viewModel: WooShippingEditOriginViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    Surface {
                        WooShippingEditAddressScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHandlingResults()
        observeEvents()
    }

    private fun observeEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowCountrySelector -> showCountrySearchScreen(event.countries)
                is ShowStateSelector -> showStatesSearchScreen(event.states)
            }
        }
    }

    private fun setupHandlingResults() {
        handleResult<LocationCode>(SELECT_COUNTRY_REQUEST) { countryCode ->
            viewModel.onCountryChanged(countryCode)
        }
        handleResult<LocationCode>(SELECT_STATE_REQUEST) { stateCode ->
            viewModel.onStateChanged(stateCode)
        }
    }

    private fun showCountrySearchScreen(countries: List<Location>) {
        val action = WooShippingEditOriginAddressFragmentDirections.actionSearchFilterFragment(
            items = countries.map {
                SearchFilterItem(
                    name = it.name,
                    value = it.code
                )
            }.toTypedArray(),
            hint = getString(R.string.shipping_label_edit_address_country_search_hint),
            requestKey = SELECT_COUNTRY_REQUEST,
            title = getString(R.string.shipping_label_edit_address_country)
        )
        findNavController().navigateSafely(action)
    }

    private fun showStatesSearchScreen(states: List<Location>) {
        val action = WooShippingEditOriginAddressFragmentDirections.actionSearchFilterFragment(
            items = states.map {
                SearchFilterItem(
                    name = it.name,
                    value = it.code
                )
            }.toTypedArray(),
            hint = getString(R.string.shipping_label_edit_address_state_search_hint),
            requestKey = SELECT_STATE_REQUEST,
            title = getString(R.string.shipping_label_edit_address_state)
        )
        findNavController().navigateSafely(action)
    }
}
