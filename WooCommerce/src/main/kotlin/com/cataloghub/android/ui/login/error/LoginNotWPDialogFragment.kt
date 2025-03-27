package com.cataloghub.android.ui.login.error

import android.os.Bundle
import com.cataloghub.android.R
import com.cataloghub.android.support.help.HelpOrigin
import com.cataloghub.android.ui.login.UnifiedLoginTracker.Click
import com.cataloghub.android.ui.login.UnifiedLoginTracker.Step
import com.cataloghub.android.ui.login.error.base.LoginBaseErrorDialogFragment
import org.wordpress.android.login.LoginListener

class LoginNotWPDialogFragment : LoginBaseErrorDialogFragment() {
    companion object {
        const val TAG = "LoginNotWPDialogFragment"
    }

    override val text: CharSequence
        get() = getString(R.string.login_not_wordpress_site_v2)

    override val helpOrigin: HelpOrigin
        get() = HelpOrigin.LOGIN_SITE_ADDRESS

    override val primaryButton: LoginErrorButton
        get() = LoginErrorButton(
            title = R.string.login_try_another_store,
            onClick = {
                unifiedLoginTracker.trackClick(Click.TRY_ANOTHER_STORE)

                dismiss()
            }
        )

    override val secondaryButton: LoginErrorButton
        get() = LoginErrorButton(
            title = R.string.login_try_another_account,
            onClick = {
                unifiedLoginTracker.trackClick(Click.TRY_ANOTHER_ACCOUNT)

                (requireActivity() as LoginListener).startOver()
            }
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            unifiedLoginTracker.track(step = Step.NOT_WORDPRESS_SITE)
        }
    }
}
