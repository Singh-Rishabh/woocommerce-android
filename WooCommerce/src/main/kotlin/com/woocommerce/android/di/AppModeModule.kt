package com.woocommerce.android.di

import com.woocommerce.android.AppPrefsWrapper
import com.woocommerce.android.analytics.AnalyticsTrackerWrapper
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.AppMode
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptHelper
import com.woocommerce.android.ui.payments.tracking.CardReaderTrackingInfoProvider
import com.woocommerce.android.ui.payments.tracking.PaymentsFlowTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(ViewModelComponent::class, SingletonComponent::class)
class AppModeModule {
    @Provides
    fun provideAppMode(): AppMode = AppMode.StoreManagement

    @Provides
    @PointOfSaleMode
    fun providePointOfSaleMode(): AppMode = AppMode.PointOfSale

    @Provides
    @StoreManagementMode
    fun provideStoreManagementMode(): AppMode = AppMode.StoreManagement

    @Provides
    @PointOfSaleMode
    @Suppress("LongParameterList")
    fun providePointOfSaleModePaymentsFlowTracker(
        trackerWrapper: AnalyticsTrackerWrapper,
        appPrefsWrapper: AppPrefsWrapper,
        selectedSite: SelectedSite,
        cardReaderTrackingInfoProvider: CardReaderTrackingInfoProvider,
        paymentReceiptHelper: PaymentReceiptHelper,
        @PointOfSaleMode appFlow: AppMode
    ): PaymentsFlowTracker = PaymentsFlowTracker(
        trackerWrapper,
        appPrefsWrapper,
        selectedSite,
        cardReaderTrackingInfoProvider,
        paymentReceiptHelper,
        appFlow
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
        @StoreManagementMode appFlow: AppMode
    ): PaymentsFlowTracker = PaymentsFlowTracker(
        trackerWrapper,
        appPrefsWrapper,
        selectedSite,
        cardReaderTrackingInfoProvider,
        paymentReceiptHelper,
        appFlow
    )
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PointOfSaleMode

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StoreManagementMode
