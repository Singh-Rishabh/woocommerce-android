package com.cataloghub.android.ui.common

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.R
import com.cataloghub.android.R.string
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.model.User
import com.cataloghub.android.ui.login.AccountRepository
import com.cataloghub.android.viewmodel.LiveDataDelegate
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Logout
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class UserEligibilityErrorViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val userEligibilityFetcher: UserEligibilityFetcher,
    analyticsTracker: AnalyticsTrackerWrapper
) : ScopedViewModel(savedState) {
    companion object {
        private const val ROLES_KEY = "current_roles"
    }

    /**
     * Saving more data than necessary into the SavedState has associated risks which were not known at the time this
     * field was implemented - after we ensure we don't save unnecessary data, we can replace @Suppress("OPT_IN_USAGE")
     * with @OptIn(LiveDelegateSavedStateAPI::class).
     */
    @Suppress("OPT_IN_USAGE")
    val viewStateData = LiveDataDelegate(savedState, ViewState())
    private var viewState by viewStateData

    init {
        userEligibilityFetcher.getUser()?.let { user ->
            viewState = viewState.copy(user = user)
            analyticsTracker.track(
                AnalyticsEvent.LOGIN_INSUFFICIENT_ROLE,
                mapOf(
                    ROLES_KEY to user.roles.joinToString(",") { it.value }
                )
            )
        }
    }

    fun onLogoutButtonClicked() = launch {
        accountRepository.logout().let {
            if (it) {
                triggerEvent(Logout)
            }
        }
    }

    fun onRetryButtonClicked() {
        launch {
            viewState = viewState.copy(isProgressDialogShown = true)
            userEligibilityFetcher.fetchUserInfo().fold(
                onSuccess = {
                    val isUserEligible = it.isEligible

                    if (isUserEligible) {
                        triggerEvent(Exit)
                    } else {
                        triggerEvent(ShowSnackbar(string.user_role_access_error_retry))
                    }
                },
                onFailure = {
                    triggerEvent(ShowSnackbar(R.string.error_generic))
                }
            )

            viewState = viewState.copy(isProgressDialogShown = false)
        }
    }

    @Parcelize
    data class ViewState(
        val user: User? = null,
        val isProgressDialogShown: Boolean? = null
    ) : Parcelable
}
