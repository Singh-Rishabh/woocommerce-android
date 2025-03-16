package com.cataloghub.android.ui.login.jetpack.dispatcher

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.model.JetpackStatus
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.tools.SiteConnectionType
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class JetpackActivationDispatcherViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    selectedSite: SelectedSite
) : ScopedViewModel(savedStateHandle) {
    private val args: JetpackActivationDispatcherFragmentArgs by savedState.navArgs()

    init {
        val jetpackStatus = args.jetpackStatus
        when (selectedSite.connectionType) {
            SiteConnectionType.ApplicationPasswords -> {
                if (jetpackStatus.isJetpackConnected && jetpackStatus.wpComEmail != null) {
                    // Jetpack is already connected and we know the address email, handle the authentication
                    triggerEvent(
                        StartWPComAuthenticationForEmail(
                            wpComEmail = jetpackStatus.wpComEmail,
                            jetpackStatus = jetpackStatus
                        )
                    )
                } else {
                    // Start regular WordPress.com authentication
                    triggerEvent(StartWPComLoginForJetpackActivation(jetpackStatus))
                }
            }

            else -> {
                // Handle connecting a new site
                triggerEvent(
                    StartJetpackActivationForNewSite(
                        args.siteUrl,
                        jetpackStatus.isJetpackInstalled
                    )
                )
            }
        }
    }

    data class StartJetpackActivationForNewSite(
        val siteUrl: String,
        val isJetpackInstalled: Boolean
    ) : MultiLiveEvent.Event()

    data class StartWPComLoginForJetpackActivation(
        val jetpackStatus: JetpackStatus
    ) : MultiLiveEvent.Event()

    data class StartWPComAuthenticationForEmail(
        val wpComEmail: String,
        val jetpackStatus: JetpackStatus
    ) : MultiLiveEvent.Event()
}
