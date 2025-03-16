package com.cataloghub.android.cardreader.internal.payments

import com.cataloghub.android.cardreader.internal.payments.actions.CollectInteracRefundAction
import com.cataloghub.android.cardreader.internal.payments.actions.ProcessInteracRefundAction
import com.cataloghub.android.cardreader.payments.CardInteracRefundStatus
import com.cataloghub.android.cardreader.payments.RefundConfig
import com.cataloghub.android.cardreader.payments.RefundParams
import com.cataloghub.android.cardreader.payments.toStripeRefundConfiguration
import com.cataloghub.android.cardreader.payments.toStripeRefundParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

internal class InteracRefundManager(
    private val collectInteracRefundAction: CollectInteracRefundAction,
    private val processInteracRefundAction: ProcessInteracRefundAction,
    private val refundErrorMapper: RefundErrorMapper,
    private val paymentsUtils: PaymentUtils,
) {
    fun refundInteracPayment(
        refundParameters: RefundParams,
        refundConfig: RefundConfig,
    ): Flow<CardInteracRefundStatus> = flow {
        collectInteracRefund(refundParameters, refundConfig)
    }

    private suspend fun FlowCollector<CardInteracRefundStatus>.collectInteracRefund(
        refundParameters: RefundParams,
        refundConfig: RefundConfig,
    ) {
        emit(CardInteracRefundStatus.CollectingInteracRefund)
        collectInteracRefundAction.collectRefund(
            refundParameters.toStripeRefundParameters(paymentsUtils),
            refundConfig.toStripeRefundConfiguration()
        ).collect { refundStatus ->
            when (refundStatus) {
                CollectInteracRefundAction.CollectInteracRefundStatus.Success -> {
                    processInteracRefund(refundParameters)
                }
                is CollectInteracRefundAction.CollectInteracRefundStatus.Failure -> {
                    emit(refundErrorMapper.mapTerminalError(refundParameters, refundStatus.exception))
                }
            }
        }
    }

    private suspend fun FlowCollector<CardInteracRefundStatus>.processInteracRefund(refundParameters: RefundParams) {
        emit(CardInteracRefundStatus.ProcessingInteracRefund)
        processInteracRefundAction.processRefund().collect { status ->
            when (status) {
                is ProcessInteracRefundAction.ProcessRefundStatus.Success -> {
                    emit(CardInteracRefundStatus.InteracRefundSuccess)
                }
                is ProcessInteracRefundAction.ProcessRefundStatus.Failure -> {
                    emit(refundErrorMapper.mapTerminalError(refundParameters, status.exception))
                }
            }
        }
    }
}
