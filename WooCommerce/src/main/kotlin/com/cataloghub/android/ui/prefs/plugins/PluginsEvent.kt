package com.cataloghub.android.ui.prefs.plugins

import com.cataloghub.android.viewmodel.MultiLiveEvent

sealed class PluginsEvent : MultiLiveEvent.Event() {
    data class NavigateToPluginsWeb(val url: String) : PluginsEvent()
}
