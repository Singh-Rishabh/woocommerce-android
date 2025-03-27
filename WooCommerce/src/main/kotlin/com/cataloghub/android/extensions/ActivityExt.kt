package com.cataloghub.android.extensions

import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import androidx.annotation.AnimRes
import androidx.fragment.app.FragmentActivity
import com.cataloghub.android.support.help.HelpActivity
import com.cataloghub.android.support.help.HelpOrigin
import com.cataloghub.android.util.SystemVersionUtils

/**
 * Used for starting the HelpActivity in a wrapped way whenever a troubleshooting URL click happens
 */
fun FragmentActivity.startHelpActivity(origin: HelpOrigin) =
    startActivity(
        HelpActivity.createIntent(
            this,
            origin,
            null
        )
    )

var Activity.currentScreenBrightness: Float
    get() = window.attributes.screenBrightness
    set(value) {
        window.attributes = window.attributes.apply { screenBrightness = value }
    }

fun Activity.adjustActivityTransition(
    overrideTransitionOpen: Boolean = true,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int,
) {
    if (SystemVersionUtils.isAtLeastU()) {
        overrideActivityTransition(
            if (overrideTransitionOpen) OVERRIDE_TRANSITION_OPEN else OVERRIDE_TRANSITION_CLOSE,
            enterAnim,
            exitAnim
        )
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(enterAnim, exitAnim)
    }
}
