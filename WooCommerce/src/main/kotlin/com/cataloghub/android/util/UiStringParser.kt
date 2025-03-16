package com.cataloghub.android.util

import android.content.Context
import com.cataloghub.android.model.UiString
import com.cataloghub.android.util.UiHelpers.getTextOfUiString
import javax.inject.Inject

class UiStringParser @Inject constructor(
    private val context: Context
) {
    fun asString(uiString: UiString): String = getTextOfUiString(context, uiString)
}
