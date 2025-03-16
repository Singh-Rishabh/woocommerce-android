package com.cataloghub.android.ui.inbox

import com.cataloghub.android.viewmodel.MultiLiveEvent.Event

sealed class InboxNoteActionEvent : Event() {
    data class OpenUrlEvent(val url: String) : InboxNoteActionEvent()
}
