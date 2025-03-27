package com.cataloghub.android.ui.login.jetpack.wpcom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.OnChangedException
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent.JETPACK_SETUP_LOGIN_FLOW
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.model.JetpackStatus
import com.cataloghub.android.ui.login.WPComLoginRepository
import com.cataloghub.android.util.StringUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.getStateFlow
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.store.AccountStore.AuthOptionsError
import org.wordpress.android.fluxc.store.AccountStore.AuthOptionsErrorType
import org.wordpress.android.login.MagicLinkFallbackButton
import javax.inject.Inject

@HiltViewModel
class JetpackActivationWPComEmailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wpComLoginRepository: WPComLoginRepository,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    private val stringUtils: StringUtils,
) : ScopedViewModel(savedStateHandle) {
    private val navArgs: JetpackActivationWPComEmailFragmentArgs by savedStateHandle.navArgs()

    private val emailOrUsername = savedStateHandle.getStateFlow(
        scope = viewModelScope,
        initialValue = "",
        key = "email"
    )
    private val errorMessage = savedStateHandle.getStateFlow(
        scope = viewModelScope,
        initialValue = 0,
        key = "error-message"
    )
    private val isLoadingDialogShown = MutableStateFlow(false)

    val viewState = combine(
        emailOrUsername,
        isLoadingDialogShown,
        errorMessage
    ) { emailOrUsername, isLoadingDialogShown, errorMessage ->
        ViewState(
            usernameOnly = navArgs.usernameOnly,
            emailOrUsername = emailOrUsername,
            isJetpackInstalled = navArgs.jetpackStatus.isJetpackInstalled,
            isLoadingDialogShown = isLoadingDialogShown,
            errorMessage = errorMessage.takeIf { it != 0 }
        )
    }.asLiveData()

    fun onEmailOrUsernameChanged(emailOrUsername: String) {
        errorMessage.value = 0
        this.emailOrUsername.value = emailOrUsername
    }

    fun onCloseClick() {
        wpComLoginRepository.clearAccessToken()
        triggerEvent(Exit)

        analyticsTrackerWrapper.track(
            JETPACK_SETUP_LOGIN_FLOW,
            mapOf(
                AnalyticsTracker.KEY_STEP to AnalyticsTracker.VALUE_JETPACK_SETUP_STEP_EMAIL_ADDRESS,
                AnalyticsTracker.KEY_TAP to AnalyticsTracker.VALUE_DISMISS
            )
        )
    }

    fun onContinueClick() = launch {
        val emailOrUsername = emailOrUsername.value.trim()

        analyticsTrackerWrapper.track(
            JETPACK_SETUP_LOGIN_FLOW,
            mapOf(
                AnalyticsTracker.KEY_STEP to AnalyticsTracker.VALUE_JETPACK_SETUP_STEP_EMAIL_ADDRESS,
                AnalyticsTracker.KEY_TAP to AnalyticsTracker.VALUE_SUBMIT
            )
        )

        isLoadingDialogShown.value = true
        wpComLoginRepository.fetchAuthOptions(emailOrUsername).fold(
            onSuccess = {
                if (it.isPasswordless) {
                    triggerEvent(
                        ShowMagicLinkScreen(
                            emailOrUsername = emailOrUsername,
                            jetpackStatus = navArgs.jetpackStatus,
                            magicLinkFallbackButton = MagicLinkFallbackButton.None,
                            requestAtStart = true,
                            isNewWpComAccount = false
                        )
                    )
                } else {
                    triggerEvent(ShowPasswordScreen(emailOrUsername, navArgs.jetpackStatus))
                }
            },
            onFailure = {
                handleLoginFailure(it, emailOrUsername)
            }
        )
        isLoadingDialogShown.value = false
    }

    private fun handleLoginFailure(error: Throwable, emailOrUsername: String) {
        val failure = (error as? OnChangedException)?.error as? AuthOptionsError
        var isSignup = false

        when (failure?.type) {
            AuthOptionsErrorType.UNKNOWN_USER -> {
                when {
                    !stringUtils.isValidEmail(emailOrUsername) ->
                        errorMessage.value = R.string.username_not_registered_wpcom

                    else -> {
                        triggerEvent(
                            ShowMagicLinkScreen(
                                emailOrUsername = emailOrUsername,
                                jetpackStatus = navArgs.jetpackStatus,
                                magicLinkFallbackButton = MagicLinkFallbackButton.None,
                                requestAtStart = true,
                                isNewWpComAccount = true
                            )
                        )
                        isSignup = true
                    }
                }
            }

            AuthOptionsErrorType.EMAIL_LOGIN_NOT_ALLOWED -> {
                triggerEvent(
                    ShowMagicLinkScreen(
                        emailOrUsername = emailOrUsername,
                        jetpackStatus = navArgs.jetpackStatus,
                        magicLinkFallbackButton = MagicLinkFallbackButton.UsernameAndPassword,
                        requestAtStart = false,
                        isNewWpComAccount = false
                    )
                )
            }

            else -> {
                triggerEvent(ShowSnackbar(R.string.error_generic))
            }
        }
        if (!isSignup) {
            trackLoginFlowAuthOptionError(failure)
        }
    }

    private fun trackLoginFlowAuthOptionError(failure: AuthOptionsError?) {
        analyticsTrackerWrapper.track(
            JETPACK_SETUP_LOGIN_FLOW,
            mapOf(
                AnalyticsTracker.KEY_STEP to AnalyticsTracker.VALUE_JETPACK_SETUP_STEP_EMAIL_ADDRESS,
                AnalyticsTracker.KEY_FAILURE to (failure?.type?.name ?: "Unknown error"),
            )
        )
    }

    data class ViewState(
        val usernameOnly: Boolean,
        val emailOrUsername: String,
        val isJetpackInstalled: Boolean,
        val isLoadingDialogShown: Boolean = false,
        val errorMessage: Int? = null
    ) {
        val enableSubmit = emailOrUsername.isNotBlank()
    }

    data class ShowPasswordScreen(
        val emailOrUsername: String,
        val jetpackStatus: JetpackStatus
    ) : MultiLiveEvent.Event()

    data class ShowMagicLinkScreen(
        val emailOrUsername: String,
        val jetpackStatus: JetpackStatus,
        val magicLinkFallbackButton: MagicLinkFallbackButton,
        val requestAtStart: Boolean,
        val isNewWpComAccount: Boolean,
    ) : MultiLiveEvent.Event()
}
