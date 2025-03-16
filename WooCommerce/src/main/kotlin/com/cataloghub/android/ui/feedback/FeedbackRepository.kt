package com.cataloghub.android.ui.feedback

import com.cataloghub.android.FeedbackPrefs
import com.cataloghub.android.model.FeatureFeedbackSettings
import com.cataloghub.android.model.FeatureFeedbackSettings.Feature
import com.cataloghub.android.model.FeatureFeedbackSettings.FeedbackState
import javax.inject.Inject

class FeedbackRepository @Inject constructor(private val feedbackPrefs: FeedbackPrefs) {
    fun getFeatureFeedbackState(feature: Feature): FeedbackState {
        return feedbackPrefs.getFeatureFeedbackSettings(feature)?.feedbackState ?: FeedbackState.UNANSWERED
    }

    fun saveFeatureFeedback(feature: Feature, feedbackState: FeedbackState) {
        feedbackPrefs.setFeatureFeedbackSettings(FeatureFeedbackSettings(feature, feedbackState))
    }

    fun getFeatureFeedbackSetting(feature: Feature): FeatureFeedbackSettings {
        return feedbackPrefs.getFeatureFeedbackSettings(feature) ?: FeatureFeedbackSettings(feature)
    }
}
