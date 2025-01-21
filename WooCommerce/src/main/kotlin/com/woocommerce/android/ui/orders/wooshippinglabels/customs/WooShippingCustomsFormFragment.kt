package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import androidx.fragment.app.viewModels
import com.woocommerce.android.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WooShippingCustomsFormFragment : BaseFragment() {
    private val viewModel: WooShippingCustomsFormViewModel by viewModels()
}
