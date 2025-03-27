package com.cataloghub.android.ui.payments.refunds

import com.cataloghub.android.R
import com.cataloghub.android.util.CurrencyFormatter
import com.cataloghub.android.viewmodel.fixedHiltNavGraphViewModels
import com.cataloghub.android.widgets.CurrencyAmountDialog
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class RefundAmountDialog : CurrencyAmountDialog() {
    @Inject lateinit var currencyFormatter: CurrencyFormatter

    private val viewModel: IssueRefundViewModel by fixedHiltNavGraphViewModels(R.id.nav_graph_refunds)

    override fun returnResult(enteredAmount: BigDecimal) {
        viewModel.onProductsRefundAmountChanged(enteredAmount)
    }
}
