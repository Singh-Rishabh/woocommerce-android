package com.cataloghub.android.ui.sitepicker.sitevisibility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WooSitesVisibilityFragment : BaseFragment() {
    companion object {
        const val WOO_SITES_VISIBILITY_UPDATED = "woo_sites_visibility_updated"
    }

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    val viewModel: WooSitesVisibilityViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return composeView {
            WooSitesVisibilityScreen(viewModel)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleEvents()
    }

    private fun handleEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MultiLiveEvent.Event.Exit -> findNavController().navigateUp()
                is ExitWithResult<*> -> navigateBackWithResult(WOO_SITES_VISIBILITY_UPDATED, event.data)
                is ShowDialog -> event.showDialog()
            }
        }
    }
}
