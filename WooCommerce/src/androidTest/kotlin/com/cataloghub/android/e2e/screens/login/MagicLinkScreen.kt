package com.cataloghub.android.e2e.screens.login

import com.cataloghub.android.R
import com.cataloghub.android.e2e.helpers.util.Screen

class MagicLinkScreen : Screen {
    constructor() : super(R.id.login_magic_link_fallback_button)

    fun proceedWithPassword(): PasswordScreen {
        clickOn(R.id.login_magic_link_fallback_button)
        return PasswordScreen()
    }
}
