package com.cataloghub.android.ui.plans.di

import android.content.Context
import com.cataloghub.android.ui.plans.trial.TrialStatusBarFormatter
import dagger.assisted.AssistedFactory

@AssistedFactory
interface TrialStatusBarFormatterFactory {
    fun create(context: Context): TrialStatusBarFormatter
}
