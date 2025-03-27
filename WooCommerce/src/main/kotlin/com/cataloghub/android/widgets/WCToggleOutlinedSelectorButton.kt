package com.cataloghub.android.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import com.google.android.material.button.MaterialButton
import com.cataloghub.android.R

class WCToggleOutlinedSelectorButton @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = R.attr.wcToggleOutlinedSelectorButtonStyle
) : MaterialButton(ctx, attrs, defStyleAttr)
