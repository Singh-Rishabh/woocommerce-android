package com.cataloghub.android.ui.payments.cardreader.detail

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.cataloghub.android.R
import com.cataloghub.android.R.color
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentCardReaderDetailBinding
import com.cataloghub.android.extensions.copyToClipboard
import com.cataloghub.android.extensions.expandHitArea
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.extensions.setDrawableColor
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.payments.cardreader.detail.CardReaderDetailViewModel.CardReaderDetailEvent.CopyReadersNameToClipboard
import com.cataloghub.android.ui.payments.cardreader.detail.CardReaderDetailViewModel.CardReaderDetailEvent.NavigateToUrlInGenericWebView
import com.cataloghub.android.ui.payments.cardreader.detail.CardReaderDetailViewModel.NavigationTarget.CardReaderConnectScreen
import com.cataloghub.android.ui.payments.cardreader.detail.CardReaderDetailViewModel.NavigationTarget.CardReaderUpdateScreen
import com.cataloghub.android.ui.payments.cardreader.detail.CardReaderDetailViewModel.ViewState
import com.cataloghub.android.ui.payments.cardreader.detail.CardReaderDetailViewModel.ViewState.ConnectedState
import com.cataloghub.android.ui.payments.cardreader.detail.CardReaderDetailViewModel.ViewState.Loading
import com.cataloghub.android.ui.payments.cardreader.detail.CardReaderDetailViewModel.ViewState.NotConnectedState
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderType.EXTERNAL
import com.cataloghub.android.ui.payments.cardreader.update.CardReaderUpdateDialogFragment
import com.cataloghub.android.ui.payments.cardreader.update.CardReaderUpdateViewModel.UpdateResult
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.util.ChromeCustomTabUtils.Height.Partial.ThreeQuarters
import com.cataloghub.android.util.UiHelpers
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.util.DisplayUtils.dpToPx

private const val HIT_AREA_EXPANSION_DP = 16

@AndroidEntryPoint
class CardReaderDetailFragment : BaseFragment(R.layout.fragment_card_reader_detail) {
    val viewModel: CardReaderDetailViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCardReaderDetailBinding.bind(view)
        setupToolbar(binding)

        observeEvents(binding)
        observeViewState(binding)
        initResultHandlers()
    }

    private fun setupToolbar(binding: FragmentCardReaderDetailBinding) {
        binding.toolbar.title = resources.getString(R.string.payments_hub_title)
        binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireActivity(),
            R.drawable.ic_back_24dp
        )
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeEvents(binding: FragmentCardReaderDetailBinding) {
        viewModel.event.observe(
            viewLifecycleOwner
        ) { event ->
            when (event) {
                is CardReaderConnectScreen ->
                    findNavController()
                        .navigateSafely(
                            CardReaderDetailFragmentDirections
                                .actionCardReaderDetailFragmentToCardReaderConnectFragment(
                                    event.cardReaderFlowParam,
                                    EXTERNAL,
                                )
                        )
                is CardReaderUpdateScreen ->
                    findNavController().navigateSafely(
                        CardReaderDetailFragmentDirections
                            .actionCardReaderDetailFragmentToCardReaderUpdateDialogFragment(requiredUpdate = false)
                    )
                is ShowSnackbar -> {
                    Snackbar.make(
                        binding.root,
                        getString(event.message),
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                }
                is CopyReadersNameToClipboard -> requireContext().copyToClipboard(
                    event.readersName,
                    event.readersName
                )
                is CardReaderDetailViewModel.CardReaderDetailEvent.CardReaderDisconnected ->
                    binding.readerDisconnectedState.cardReaderDetailConnectBtn.announceForAccessibility(
                        getString(event.accessibilityDisconnectedText)
                    )
                is CardReaderDetailViewModel.CardReaderDetailEvent.CardReaderConnected ->
                    binding.readerConnectedState.primaryActionBtn.announceForAccessibility(
                        getString(event.accessibilityConnectedText)
                    )
                is NavigateToUrlInGenericWebView ->
                    ChromeCustomTabUtils.launchUrl(requireContext(), event.url, ThreeQuarters)
                else -> event.isHandled = false
            }
        }
    }

    private fun observeViewState(binding: FragmentCardReaderDetailBinding) {
        viewModel.viewStateData.observe(
            viewLifecycleOwner
        ) { state ->
            makeStateVisible(binding, state)
            when (state) {
                is ConnectedState -> {
                    with(binding.readerConnectedState) {
                        UiHelpers.setTextOrHide(enforcedUpdateTv, state.enforceReaderUpdate)
                        enforcedUpdateDivider.visibility = enforcedUpdateTv.visibility
                        with(readerNameTv) {
                            UiHelpers.setTextOrHide(this, state.readerName)
                            setOnLongClickListener {
                                state.onReaderNameLongClick()
                                true
                            }
                            expandHitArea(0, dpToPx(requireContext(), HIT_AREA_EXPANSION_DP))
                        }
                        UiHelpers.setTextOrHide(readerBatteryTv, state.readerBattery)
                        UiHelpers.setTextOrHide(readerFirmwareVersionTv, state.readerFirmwareVersion)
                        UiHelpers.setTextOrHide(primaryActionBtn, state.primaryButtonState?.text)
                        primaryActionBtn.setOnClickListener { state.primaryButtonState?.onActionClicked?.invoke() }
                        UiHelpers.setTextOrHide(secondaryActionBtn, state.secondaryButtonState?.text)
                        secondaryActionBtn.setOnClickListener {
                            state.secondaryButtonState?.onActionClicked?.invoke()
                        }
                        binding.readerConnectedState.enforcedUpdateTv.setDrawableColor(
                            color.warning_banner_foreground_color
                        )
                        with(cardReaderDetailLearnMoreTv.root) {
                            movementMethod = LinkMovementMethod.getInstance()
                            UiHelpers.setTextOrHide(this, state.learnMoreLabel)
                            setOnClickListener { state.onLearnMoreClicked.invoke() }
                        }
                    }
                }
                is NotConnectedState -> {
                    with(binding.readerDisconnectedState) {
                        UiHelpers.setTextOrHide(cardReaderDetailConnectHeaderLabel, state.headerLabel)
                        UiHelpers.setImageOrHideInLandscapeOnCompactScreenHeightSizeClass(
                            cardReaderDetailIllustration,
                            state.illustration
                        )
                        UiHelpers.setTextOrHide(cardReaderDetailFirstHintLabel, state.firstHintLabel)
                        UiHelpers.setTextOrHide(cardReaderDetailFirstHintNumberLabel, state.firstHintNumber)
                        UiHelpers.setTextOrHide(cardReaderDetailSecondHintLabel, state.secondHintLabel)
                        UiHelpers.setTextOrHide(cardReaderDetailSecondHintNumberLabel, state.secondHintNumber)
                        UiHelpers.setTextOrHide(cardReaderDetailThirdHintLabel, state.thirdHintLabel)
                        UiHelpers.setTextOrHide(cardReaderDetailThirdHintNumberLabel, state.thirdHintNumber)
                        UiHelpers.setTextOrHide(cardReaderDetailConnectBtn, state.connectBtnLabel)
                        cardReaderDetailConnectBtn.setOnClickListener { state.onPrimaryActionClicked.invoke() }
                        with(cardReaderDetailLearnMoreTv.root) {
                            movementMethod = LinkMovementMethod.getInstance()
                            UiHelpers.setTextOrHide(this, state.learnMoreLabel)
                            setOnClickListener { state.onLearnMoreClicked.invoke() }
                        }
                    }
                }
                Loading -> {
                }
            }
        }
    }

    private fun initResultHandlers() {
        handleResult<UpdateResult>(CardReaderUpdateDialogFragment.KEY_READER_UPDATE_RESULT) {
            viewModel.onUpdateReaderResult(it)
        }
    }

    private fun makeStateVisible(binding: FragmentCardReaderDetailBinding, state: ViewState) {
        UiHelpers.updateVisibility(binding.readerConnectedState.root, state is ConnectedState)
        UiHelpers.updateVisibility(binding.readerDisconnectedState.root, state is NotConnectedState)
        UiHelpers.updateVisibility(binding.readerConnectedLoading, state is Loading)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }
}
