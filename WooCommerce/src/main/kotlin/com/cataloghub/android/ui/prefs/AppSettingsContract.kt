package com.cataloghub.android.ui.prefs

import com.cataloghub.android.ui.base.BasePresenter
import com.cataloghub.android.ui.base.BaseView

interface AppSettingsContract {
    interface Presenter : BasePresenter<View> {
        fun logout()
        fun userIsLoggedIn(): Boolean
        fun getAccountDisplayName(): String
    }

    interface View : BaseView<Presenter> {
        fun finishLogout()
        fun confirmLogout()
    }
}
