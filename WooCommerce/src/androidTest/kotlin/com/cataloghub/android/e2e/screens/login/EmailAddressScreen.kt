package com.cataloghub.android.e2e.screens.login

import com.cataloghub.android.R
import com.cataloghub.android.e2e.helpers.util.Screen

class EmailAddressScreen : Screen {
    constructor() : super(org.wordpress.android.login.R.id.input)

    fun proceedWith(emailAddress: String): PasswordScreen {
        typeTextInto(org.wordpress.android.login.R.id.input, emailAddress)
        clickOn(R.id.login_continue_button)

        return PasswordScreen()
    }
}
