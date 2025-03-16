package com.cataloghub.android.ui.payments.refunds

import androidx.navigation.fragment.navArgs
import com.cataloghub.android.R
import com.cataloghub.android.viewmodel.fixedHiltNavGraphViewModels
import com.cataloghub.android.widgets.NumberPickerDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RefundItemsPickerDialog : NumberPickerDialog() {
    private val viewModel: IssueRefundViewModel by fixedHiltNavGraphViewModels(R.id.nav_graph_refunds)

    private val navArgs: RefundItemsPickerDialogArgs by navArgs()

    override fun returnResult(selectedValue: Int) {
        viewModel.onRefundQuantityChanged(navArgs.uniqueId, selectedValue)
    }
}
