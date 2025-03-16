package com.cataloghub.android.ui.whatsnew

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.AppPrefs
import com.cataloghub.android.model.FeatureAnnouncement
import com.cataloghub.android.util.BuildConfigWrapper
import com.cataloghub.android.viewmodel.LiveDataDelegate
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class FeatureAnnouncementViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val prefs: AppPrefs,
    private val buildConfigWrapper: BuildConfigWrapper,
) : ScopedViewModel(savedState) {
    /**
     * Saving more data than necessary into the SavedState has associated risks which were not known at the time this
     * field was implemented - after we ensure we don't save unnecessary data, we can replace @Suppress("OPT_IN_USAGE")
     * with @OptIn(LiveDelegateSavedStateAPI::class).
     */
    @Suppress("OPT_IN_USAGE")
    val viewStateData = LiveDataDelegate(savedState, FeatureAnnouncementViewState())
    private var viewState by viewStateData

    fun setAnnouncementData(announcement: FeatureAnnouncement) {
        viewState = viewState.copy(announcement = announcement)
    }

    fun handleAnnouncementIsViewed() {
        prefs.setLastVersionWithAnnouncement(buildConfigWrapper.versionName)
    }

    @Parcelize
    data class FeatureAnnouncementViewState(
        val announcement: FeatureAnnouncement? = null
    ) : Parcelable
}
