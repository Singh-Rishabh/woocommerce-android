package com.cataloghub.android.util

import com.cataloghub.android.BuildConfig
import dagger.Reusable
import javax.inject.Inject

@Reusable
class BuildConfigWrapper @Inject constructor() {
    val debug = BuildConfig.DEBUG
    val versionName = BuildConfig.VERSION_NAME
}
