package com.cataloghub.android.ui.prefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.cataloghub.android.AppUrls
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.prefs.PrivacySettingsViewModel.PrivacySettingsEvent.OpenPolicies
import com.cataloghub.android.ui.prefs.PrivacySettingsViewModel.PrivacySettingsEvent.ShowUsageTracker
import com.cataloghub.android.ui.prefs.PrivacySettingsViewModel.PrivacySettingsEvent.ShowWebOptions
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PrivacySettingsFragment : BaseFragment() {
    companion object {
        const val TAG = "privacy-settings"
    }

    private val viewModel: PrivacySettingsViewModel by viewModels()

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    private var snackbar: Snackbar? = null

    override fun getFragmentTitle() = getString(R.string.privacy_settings)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeEvents()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                WooThemeWithBackground {
                    PrivacySettingsScreen(viewModel)
                }
            }
        }
    }

    private fun observeEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowWebOptions -> showWebOptions()
                is ShowUsageTracker -> showUsageTracker()
                is OpenPolicies -> findNavController().navigateSafely(
                    PrivacySettingsFragmentDirections.actionPrivacySettingsFragmentToPrivacySettingsPolicesFragment()
                )
                is MultiLiveEvent.Event.ShowActionSnackbar ->
                    snackbar = uiMessageResolver.getIndefiniteActionSnack(
                        event.message,
                        actionText = event.actionText,
                        actionListener = event.action
                    ).apply {
                        show()
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onPause() {
        super.onPause()
        snackbar?.dismiss()
    }

    private fun showWebOptions() {
        ChromeCustomTabUtils.launchUrl(requireActivity(), AppUrls.WOOCOMMERCE_WEB_OPTIONS)
    }

    private fun showUsageTracker() {
        ChromeCustomTabUtils.launchUrl(requireActivity(), AppUrls.WOOCOMMERCE_USAGE_TRACKER)
    }
}
