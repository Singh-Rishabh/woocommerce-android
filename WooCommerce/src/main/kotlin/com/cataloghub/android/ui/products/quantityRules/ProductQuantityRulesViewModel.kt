package com.cataloghub.android.ui.products.quantityRules

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.ui.products.models.QuantityRules
import com.cataloghub.android.viewmodel.LiveDataDelegate
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class ProductQuantityRulesViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val analyticsTracker: AnalyticsTrackerWrapper
) : ScopedViewModel(savedState) {

    private val navArgs: ProductQuantityRulesFragmentArgs by savedState.navArgs()

    /**
     * Saving more data than necessary into the SavedState has associated risks which were not known at the time this
     * field was implemented - after we ensure we don't save unnecessary data, we can replace @Suppress("OPT_IN_USAGE")
     * with @OptIn(LiveDelegateSavedStateAPI::class).
     */
    @Suppress("OPT_IN_USAGE")
    val viewStateData = LiveDataDelegate(
        savedState,
        ViewState(
            quantityRules = navArgs.quantityRules
        )
    )

    private val originalQuantityRules = navArgs.quantityRules

    private var viewState by viewStateData

    val quantityRules
        get() = viewState.quantityRules

    private val hasChanges: Boolean
        get() = quantityRules != originalQuantityRules

    fun onDataChanged(
        min: Int? = quantityRules.min,
        max: Int? = quantityRules.max,
        groupOf: Int? = quantityRules.groupOf
    ) {
        viewState = viewState.copy(
            quantityRules = quantityRules.copy(
                min = min,
                max = max,
                groupOf = groupOf
            )
        )
    }

    fun onExit() {
        analyticsTracker.track(
            navArgs.exitAnalyticsEvent,
            mapOf(AnalyticsTracker.KEY_HAS_CHANGED_DATA to hasChanges)
        )
        if (hasChanges) {
            triggerEvent(MultiLiveEvent.Event.ExitWithResult(quantityRules))
        } else {
            triggerEvent(MultiLiveEvent.Event.Exit)
        }
    }

    @Parcelize
    data class ViewState(
        val quantityRules: QuantityRules = QuantityRules()
    ) : Parcelable
}
