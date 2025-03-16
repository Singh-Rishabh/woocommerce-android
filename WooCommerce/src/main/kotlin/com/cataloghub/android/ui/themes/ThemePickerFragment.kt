package com.cataloghub.android.ui.themes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.themes.ThemePickerViewModel.NavigateToThemePreview
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ThemePickerFragment : BaseFragment() {
    private val viewModel: ThemePickerViewModel by viewModels()

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    ThemePickerScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        handleResults()
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MultiLiveEvent.Event.Exit -> findNavController().popBackStack()
                is NavigateToThemePreview -> navigateToThemePreviewFragment(event)
                is MultiLiveEvent.Event.ShowSnackbar -> uiMessageResolver.showSnack(event.message)
            }
        }
    }

    private fun handleResults() {
        handleResult<ThemePreviewViewModel.SelectedTheme>(
            key = ThemePreviewFragment.CURRENT_THEME_UPDATED
        ) {
            viewModel.onCurrentThemeUpdated(it.themeId, it.themeName)
        }
    }

    private fun navigateToThemePreviewFragment(event: NavigateToThemePreview) {
        findNavController().navigateSafely(
            ThemePickerFragmentDirections
                .actionThemePickerFragmentToThemePreviewFragment(themeId = event.themeId)
        )
    }
}
