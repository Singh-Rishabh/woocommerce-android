package com.cataloghub.android.di

import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.payments.cardreader.payment.controller.CardReaderTrackCanceledFlowAction
import com.cataloghub.android.ui.payments.receipt.PaymentReceiptHelper
import com.cataloghub.android.ui.payments.tracking.CardReaderTrackingInfoProvider
import com.cataloghub.android.ui.payments.tracking.PaymentsFlowTracker
import com.cataloghub.android.ui.payments.tracking.PaymentsFlowTrackerEventProvider
import com.cataloghub.android.ui.payments.tracking.StoreManagementPaymentsFlowTrackerEventProvider
import com.cataloghub.android.ui.woopos.util.analytics.WooPosPaymentsFlowTrackerEventProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(ViewModelComponent::class, SingletonComponent::class)
class AppModePaymentsFlowTrackingModule {
    @Provides
    fun provideDefaultPaymentsFlowTrackerEventProvider(): PaymentsFlowTrackerEventProvider =
        StoreManagementPaymentsFlowTrackerEventProvider()

    @Provides
    @PointOfSaleMode
    fun providePointOfSaleModePaymentsFlowTrackerEventProvider(): PaymentsFlowTrackerEventProvider =
        WooPosPaymentsFlowTrackerEventProvider()

    @Provides
    @StoreManagementMode
    fun provideStoreManagementModePaymentsFlowTrackerEventProvider(): PaymentsFlowTrackerEventProvider =
        StoreManagementPaymentsFlowTrackerEventProvider()

    @Provides
    @PointOfSaleMode
    @Suppress("LongParameterList")
    fun providePointOfSaleModePaymentsFlowTracker(
        trackerWrapper: AnalyticsTrackerWrapper,
        appPrefsWrapper: AppPrefsWrapper,
        selectedSite: SelectedSite,
        cardReaderTrackingInfoProvider: CardReaderTrackingInfoProvider,
        paymentReceiptHelper: PaymentReceiptHelper,
        @PointOfSaleMode paymentsFlowTrackerEventProvider: PaymentsFlowTrackerEventProvider,
    ): PaymentsFlowTracker = PaymentsFlowTracker(
        trackerWrapper = trackerWrapper,
        appPrefsWrapper = appPrefsWrapper,
        selectedSite = selectedSite,
        cardReaderTrackingInfoProvider = cardReaderTrackingInfoProvider,
        paymentReceiptHelper = paymentReceiptHelper,
        eventProvider = paymentsFlowTrackerEventProvider,
    )

    @Provides
    @StoreManagementMode
    @Suppress("LongParameterList")
    fun provideStoreManagementModePaymentsFlowTracker(
        trackerWrapper: AnalyticsTrackerWrapper,
        appPrefsWrapper: AppPrefsWrapper,
        selectedSite: SelectedSite,
        cardReaderTrackingInfoProvider: CardReaderTrackingInfoProvider,
        paymentReceiptHelper: PaymentReceiptHelper,
        @StoreManagementMode paymentsFlowTrackerEventProvider: PaymentsFlowTrackerEventProvider,
    ): PaymentsFlowTracker = PaymentsFlowTracker(
        trackerWrapper = trackerWrapper,
        appPrefsWrapper = appPrefsWrapper,
        selectedSite = selectedSite,
        cardReaderTrackingInfoProvider = cardReaderTrackingInfoProvider,
        paymentReceiptHelper = paymentReceiptHelper,
        eventProvider = paymentsFlowTrackerEventProvider,
    )

    @Provides
    @PointOfSaleMode
    fun providePointOfSaleModeCardReaderTrackCanceledFlowAction(
        @PointOfSaleMode tracker: PaymentsFlowTracker
    ): CardReaderTrackCanceledFlowAction = CardReaderTrackCanceledFlowAction(tracker)

    @Provides
    @StoreManagementMode
    fun provideStoreManagementModeCardReaderTrackCanceledFlowAction(
        @StoreManagementMode tracker: PaymentsFlowTracker
    ): CardReaderTrackCanceledFlowAction = CardReaderTrackCanceledFlowAction(tracker)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PointOfSaleMode

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StoreManagementMode
