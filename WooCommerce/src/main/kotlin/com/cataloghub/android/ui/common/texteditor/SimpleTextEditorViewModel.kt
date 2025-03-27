package com.cataloghub.android.ui.common.texteditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import com.cataloghub.android.ui.common.texteditor.SimpleTextEditorStrategy.SEND_RESULT_ON_CONFIRMATION
import com.cataloghub.android.ui.common.texteditor.SimpleTextEditorStrategy.SEND_RESULT_ON_NAVIGATE_BACK
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SimpleTextEditorViewModel @Inject constructor(savedState: SavedStateHandle) : ScopedViewModel(savedState) {
    companion object {
        private const val TEXT_KEY = "text"
    }

    private val navArgs: SimpleTextEditorFragmentArgs by savedState.navArgs()
    private val textLiveData = savedState.getLiveData(TEXT_KEY, navArgs.currentText)
    val viewState = textLiveData.map {
        ViewState(
            text = it,
            hint = navArgs.hint,
            hasChanges = it != navArgs.currentText
        )
    }

    fun onTextChanged(text: String) {
        textLiveData.value = text
    }

    fun onBackPressed() {
        when (navArgs.strategy) {
            SEND_RESULT_ON_NAVIGATE_BACK -> {
                exitWithResult()
            }
            SEND_RESULT_ON_CONFIRMATION -> {
                exit()
            }
        }
    }

    fun onDonePressed() {
        exitWithResult()
    }

    private fun exit() {
        triggerEvent(Exit)
    }

    private fun exitWithResult() {
        val event = viewState.value?.takeIf { it.hasChanges }?.let { viewState ->
            ExitWithResult(
                SimpleTextEditorResult(
                    text = viewState.text,
                    requestCode = navArgs.requestCode.takeIf { it != -1 }
                )
            )
        } ?: Exit

        triggerEvent(event)
    }

    data class ViewState(
        val text: String?,
        val hint: String,
        val hasChanges: Boolean
    )

    data class SimpleTextEditorResult(
        val text: String?,
        val requestCode: Int?
    )
}
