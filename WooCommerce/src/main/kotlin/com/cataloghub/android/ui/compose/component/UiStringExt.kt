package com.cataloghub.android.ui.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.cataloghub.android.model.UiString
import com.cataloghub.android.util.UiHelpers

@Composable
fun UiString.getText(): String {
    return UiHelpers.getTextOfUiString(LocalContext.current, this)
}
