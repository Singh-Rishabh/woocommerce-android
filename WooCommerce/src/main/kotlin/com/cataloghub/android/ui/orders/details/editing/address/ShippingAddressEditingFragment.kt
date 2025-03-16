package com.cataloghub.android.ui.orders.details.editing.address

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentBaseEditAddressBinding
import com.cataloghub.android.model.Address
import com.cataloghub.android.ui.main.AppBarStatus

class ShippingAddressEditingFragment : BaseAddressEditingFragment() {
    override val analyticsValue: String = AnalyticsTracker.ORDER_EDIT_SHIPPING_ADDRESS

    private val args by navArgs<ShippingAddressEditingFragmentArgs>()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override val storedAddress: Address by lazy {
        args.storedAddress
    }

    override val addressType: AddressViewModel.AddressType = AddressViewModel.AddressType.SHIPPING

    override fun saveChanges() = sharedViewModel.updateShippingAddress(addressDraft)

    override fun getFragmentTitle() = getString(R.string.order_detail_shipping_address_section)

    override fun onViewBound(binding: FragmentBaseEditAddressBinding) {
        binding.form.email.visibility = View.GONE
        binding.form.addressSectionHeader.text = getString(R.string.order_detail_shipping_address_section)
        replicateAddressSwitch.text = getString(R.string.order_detail_use_as_billing_address)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.title = getString(R.string.order_detail_shipping_address_section)
    }
}
