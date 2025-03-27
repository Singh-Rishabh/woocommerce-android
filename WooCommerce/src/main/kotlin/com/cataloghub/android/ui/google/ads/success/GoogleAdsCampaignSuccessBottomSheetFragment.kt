package com.cataloghub.android.ui.google.ads.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.widgets.WCBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleAdsCampaignSuccessBottomSheetFragment : WCBottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return composeView {
            GoogleAdsCampaignSuccessBottomSheet(::onDoneClicked)
        }
    }

    private fun onDoneClicked() {
        dismiss()
    }
}
