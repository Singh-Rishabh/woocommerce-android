package com.cataloghub.android.ui.payments.cardreader

import com.cataloghub.android.cardreader.internal.payments.PaymentUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class CardReaderPaymentsUtilsModule {
    @Provides
    fun providePaymentsUtils() = PaymentUtils
}
