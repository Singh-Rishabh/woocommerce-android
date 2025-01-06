package com.woocommerce.android.ui.prefs.plugins

import androidx.annotation.ColorRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.extensions.isNotNullOrEmpty
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.prefs.plugins.PluginsViewModel.ViewState.Error
import com.woocommerce.android.ui.prefs.plugins.PluginsViewModel.ViewState.Loaded
import com.woocommerce.android.ui.prefs.plugins.PluginsViewModel.ViewState.Loaded.Plugin
import com.woocommerce.android.ui.prefs.plugins.PluginsViewModel.ViewState.Loaded.Plugin.PluginStatus.Inactive
import com.woocommerce.android.ui.prefs.plugins.PluginsViewModel.ViewState.Loaded.Plugin.PluginStatus.Unknown
import com.woocommerce.android.ui.prefs.plugins.PluginsViewModel.ViewState.Loaded.Plugin.PluginStatus.UpToDate
import com.woocommerce.android.ui.prefs.plugins.PluginsViewModel.ViewState.Loaded.Plugin.PluginStatus.UpdateAvailable
import com.woocommerce.android.ui.prefs.plugins.PluginsViewModel.ViewState.Loading
import com.woocommerce.android.util.isGreaterThanPluginVersion
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event.Exit
import com.woocommerce.android.viewmodel.ResourceProvider
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import org.wordpress.android.fluxc.network.rest.wpcom.wc.system.WCSystemPluginResponse.SystemPluginModel
import org.wordpress.android.fluxc.store.WooCommerceStore
import javax.inject.Inject

@HiltViewModel
class PluginsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val site: SelectedSite,
    private val wooCommerceStore: WooCommerceStore,
    private val resourceProvider: ResourceProvider
) : ScopedViewModel(savedStateHandle) {
    private val _viewState = MutableSharedFlow<ViewState>(1)
    val viewState = _viewState.asLiveData()

    init {
        loadPlugins()
    }

    private fun loadPlugins() {
        viewModelScope.launch(Dispatchers.IO) {
            _viewState.emit(Loading)
            val response = wooCommerceStore.fetchSystemPlugins(site.get())
            if (!response.isError && response.model != null) {
                _viewState.emit(
                    Loaded(
                        plugins = response.model!!
                            .filter { it.version.isNotNullOrEmpty() && it.name.isNotNullOrEmpty() }
                            .map {
                                Plugin(
                                    name = StringEscapeUtils.unescapeHtml4(it.name),
                                    authorName = StringEscapeUtils.unescapeHtml4(it.authorName),
                                    version = it.version!!,
                                    status = it.getState()
                                )
                            }
                    )
                )
            } else {
                _viewState.emit(Error)
            }
        }
    }

    private fun SystemPluginModel.getState(): Plugin.PluginStatus {
        return when {
            !isActive -> Inactive(
                resourceProvider.getString(R.string.plugin_state_inactive),
                R.color.color_on_surface_disabled,
            )
            versionLatest.isNullOrEmpty() -> Unknown
            isUpdateAvailable() -> UpdateAvailable(
                resourceProvider.getString(R.string.plugin_state_update_available, versionLatest!!),
                R.color.color_primary,
            )
            else -> UpToDate(resourceProvider.getString(R.string.plugin_state_up_to_date), R.color.color_info)
        }
    }

    private fun SystemPluginModel.isUpdateAvailable(): Boolean {
        return versionLatest.isGreaterThanPluginVersion(version)
    }

    fun onRetryClicked() {
        loadPlugins()
    }

    fun onBackPressed() {
        triggerEvent(Exit)
    }

    sealed interface ViewState {
        data object Loading : ViewState
        data object Error : ViewState
        data class Loaded(
            val plugins: List<Plugin> = emptyList()
        ) : ViewState {
            data class Plugin(
                val name: String,
                val authorName: String?,
                val version: String,
                val status: PluginStatus
            ) {
                sealed class PluginStatus {
                    data class UpToDate(val title: String, @ColorRes val color: Int) : PluginStatus()
                    data class UpdateAvailable(val title: String, @ColorRes val color: Int) : PluginStatus()
                    data class Inactive(val title: String, @ColorRes val color: Int) : PluginStatus()
                    data object Unknown : PluginStatus()
                }
            }
        }
    }
}
