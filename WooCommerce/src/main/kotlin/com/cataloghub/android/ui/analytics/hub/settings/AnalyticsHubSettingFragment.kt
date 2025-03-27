package com.cataloghub.android.ui.analytics.hub.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsHubSettingFragment : BaseFragment() {

    private val viewModel: AnalyticsHubSettingsViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            id = R.id.analytics_hub_settings_view

            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WooThemeWithBackground {
                    AnalyticsHubSettingScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.event.observe(viewLifecycleOwner) { event -> handleEvent(event) }
    }

    private fun handleEvent(event: MultiLiveEvent.Event) {
        when (event) {
            is MultiLiveEvent.Event.Exit -> findNavController().popBackStack()
            is MultiLiveEvent.Event.LaunchUrlInChromeTab -> {
                ChromeCustomTabUtils.launchUrl(requireContext(), event.url)
            }
        }
    }
}
