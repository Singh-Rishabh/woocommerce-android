package com.cataloghub.android.ui.common.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.cataloghub.android.R
import com.cataloghub.android.extensions.navigateBackWithNotice
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import dagger.hilt.android.AndroidEntryPoint

/**
 * This fragments allows loading specific pages from WordPress.com with the current user logged in.
 */
@AndroidEntryPoint
class AuthenticatedWebViewFragment : BaseFragment(R.layout.fragment_wpcom_webview) {
    companion object {
        const val WEBVIEW_RESULT = "webview-result"
        const val WEBVIEW_DISMISSED = "webview-dismissed"
    }

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    private val viewModel: AuthenticatedWebViewViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WooThemeWithBackground {
                    AuthenticatedWebViewScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ExitWithResult<*> -> navigateBackWithNotice(WEBVIEW_RESULT)
                is Exit -> navigateBackWithNotice(WEBVIEW_DISMISSED)
            }
        }
    }
}
