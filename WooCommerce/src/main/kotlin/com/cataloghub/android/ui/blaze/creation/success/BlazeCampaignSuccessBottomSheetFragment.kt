package com.cataloghub.android.ui.blaze.creation.success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cataloghub.android.AppUrls.BLAZE_CAMPAIGN_CREATION_SURVEY_URL_I1
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.widgets.WCBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BlazeCampaignSuccessBottomSheetFragment : WCBottomSheetDialogFragment() {
    @Inject
    lateinit var analyticsTracker: AnalyticsTrackerWrapper

    @Inject
    lateinit var shouldShowFeedbackRequest: ShouldShowFeedbackRequest

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return composeView {
            BlazeCampaignSuccessBottomSheet(
                ::onDoneClicked,
                ::onFeedbackRequestTapped,
                shouldShowFeedbackRequest
            )
        }
    }

    private fun onDoneClicked() {
        dismiss()
    }

    private fun onFeedbackRequestTapped(isPositive: Boolean) {
        analyticsTracker.track(
            stat = AnalyticsEvent.BLAZE_CAMPAIGN_CREATION_FEEDBACK,
            properties = mapOf("satisfied" to isPositive)
        )
        ChromeCustomTabUtils.launchUrl(requireContext(), BLAZE_CAMPAIGN_CREATION_SURVEY_URL_I1)
        dismiss()
    }
}
