package com.cataloghub.android.ui.orders.notes

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.cataloghub.android.databinding.OrderDetailNoteListHeaderBinding
import com.cataloghub.android.databinding.OrderDetailNoteListNoteBinding

abstract class OrderNoteViewHolder(viewBinding: ViewBinding) : RecyclerView.ViewHolder(viewBinding.getRoot())

class HeaderItemViewHolder(val viewBinding: OrderDetailNoteListHeaderBinding) : OrderNoteViewHolder(viewBinding) {
    fun bind(item: OrderNoteListItem.Header) {
        viewBinding.orderDetailNoteListHeader.text = item.text
    }
}

class NoteItemViewHolder(val viewBinding: OrderDetailNoteListNoteBinding) : OrderNoteViewHolder(viewBinding) {
    fun bind(item: OrderNoteListItem.Note) {
        viewBinding.noteItemView.initView(item.note)
    }
}
