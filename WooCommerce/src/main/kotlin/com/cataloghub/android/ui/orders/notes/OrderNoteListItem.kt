package com.cataloghub.android.ui.orders.notes

import com.cataloghub.android.model.OrderNote
import com.cataloghub.android.ui.orders.notes.OrderNoteListItem.ViewType.HEADER
import com.cataloghub.android.ui.orders.notes.OrderNoteListItem.ViewType.NOTE

sealed class OrderNoteListItem(val viewType: ViewType) {
    class Header(val text: String) : OrderNoteListItem(HEADER)
    class Note(val note: OrderNote, override val longId: Long = note.remoteNoteId) : OrderNoteListItem(NOTE)

    open val longId: Long
        get() = hashCode().toLong()

    enum class ViewType(val id: Int) {
        HEADER(0),
        NOTE(1)
    }
}
