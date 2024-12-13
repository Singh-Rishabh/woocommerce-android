package com.woocommerce.android.ui.woopos.home.totals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.component.Button
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosCircularLoadingIndicator
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosErrorScreen
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosOutlinedButton
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosShimmerBox
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding
import com.woocommerce.android.ui.woopos.home.totals.WooPosTotalsViewState.Totals.CashPaymentAvailability
import com.woocommerce.android.ui.woopos.home.totals.payment.failed.WooPosPaymentFailedScreen
import com.woocommerce.android.ui.woopos.home.totals.payment.inprogress.WooPosPaymentInProgressScreen
import com.woocommerce.android.ui.woopos.home.totals.payment.receipt.WooPosTotalsPaymentReceiptScreen
import com.woocommerce.android.ui.woopos.home.totals.payment.success.WooPosPaymentSuccessScreen
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent

@Composable
fun WooPosTotalsScreen(
    onNavigationEvent: (WooPosNavigationEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: WooPosTotalsViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value
    WooPosTotalsScreen(
        modifier = modifier,
        state = state,
        onUIEvent = viewModel::onUIEvent,
        onNavigationEvent = onNavigationEvent
    )
}

@Composable
private fun WooPosTotalsScreen(
    modifier: Modifier = Modifier,
    state: WooPosTotalsViewState,
    onUIEvent: (WooPosTotalsUIEvent) -> Unit,
    onNavigationEvent: (WooPosNavigationEvent) -> Unit,
) {
    Box(modifier = modifier) {
        StateChangeAnimated(visible = state is WooPosTotalsViewState.Totals) {
            if (state is WooPosTotalsViewState.Totals) {
                TotalsLoaded(
                    state = state,
                    onNavigationEvent = onNavigationEvent,
                )
            }
        }

        StateChangeAnimated(visible = state is WooPosTotalsViewState.PaymentSuccess) {
            if (state is WooPosTotalsViewState.PaymentSuccess) {
                WooPosPaymentSuccessScreen(
                    state,
                    onReceiptClicked = { onUIEvent(WooPosTotalsUIEvent.OnStartReceiptFlowClicked) },
                    onNewTransactionClicked = { onUIEvent(WooPosTotalsUIEvent.OnNewTransactionClicked) }
                )
            }
        }

        StateChangeAnimated(visible = state is WooPosTotalsViewState.Loading) {
            if (state is WooPosTotalsViewState.Loading) {
                TotalsLoading()
            }
        }

        StateChangeAnimated(visible = state is WooPosTotalsViewState.ReceiptSending) {
            if (state is WooPosTotalsViewState.ReceiptSending) {
                WooPosTotalsPaymentReceiptScreen(
                    state,
                    onEmailAddressChanged = { onUIEvent(WooPosTotalsUIEvent.OnEmailChanged(it)) },
                    onSendReceiptClicked = { onUIEvent(WooPosTotalsUIEvent.OnSendReceiptClicked) }
                )
            }
        }

        StateChangeAnimated(visible = state is WooPosTotalsViewState.Error) {
            if (state is WooPosTotalsViewState.Error) {
                TotalsErrorScreen(
                    errorMessage = state.message,
                    onUIEvent = onUIEvent
                )
            }
        }

        StateChangeAnimated(visible = state is WooPosTotalsViewState.PaymentInProgress) {
            if (state is WooPosTotalsViewState.PaymentInProgress) {
                WooPosPaymentInProgressScreen(state)
            }
        }

        StateChangeAnimated(visible = state is WooPosTotalsViewState.PaymentFailed) {
            if (state is WooPosTotalsViewState.PaymentFailed) {
                WooPosPaymentFailedScreen(
                    state = state,
                    onUIEvent = onUIEvent,
                )
            }
        }
    }
}

@Composable
private fun StateChangeAnimated(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        content = content
    )
}

@Composable
private fun TotalsLoaded(
    state: WooPosTotalsViewState.Totals,
    onNavigationEvent: (WooPosNavigationEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(WooPosTheme.colors.totalsBackground)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TotalsGrid(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .background(WooPosTheme.colors.totalsErrorBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val readerStatus = state.readerStatus) {
                is WooPosTotalsViewState.ReaderStatus.Disconnected -> {
                    ReaderDisconnected(modifier = Modifier, status = readerStatus)
                }
                is WooPosTotalsViewState.ReaderStatus.Preparing,
                is WooPosTotalsViewState.ReaderStatus.CheckingOrder -> {
                    PreparingReader(readerStatus)
                }
                is WooPosTotalsViewState.ReaderStatus.ReadyForPayment -> {
                    ReaderReadyForPayment(readerStatus)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 40.dp.toAdaptivePadding(),
                    vertical = 16.dp.toAdaptivePadding()
                )
                .weight(.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TotalsGrid(state = state)

            AnimatedVisibility(
                visible = isButtonVisible,
                enter = slideInVertically { it },
                modifier = Modifier.constrainAs(buttons) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp.toAdaptivePadding()))
                    when (state.cashPaymentAvailability) {
                        is CashPaymentAvailability.Available -> {
                            WooPosOutlinedButton(
                                text = stringResource(R.string.woopos_payment_take_cash_payment_label),
                                onClick = {
                                    onNavigationEvent(
                                        WooPosNavigationEvent.OpenCashPayment(
                                            orderId = state.cashPaymentAvailability.orderId
                                        )
                                    )
                                },
                            )
                            Spacer(modifier = Modifier.height(16.dp.toAdaptivePadding()))
                        }
                        CashPaymentAvailability.Unavailable -> {
                        }
                    }

                    WooPosButtonLarge(
                        text = stringResource(R.string.woopos_payment_collect_payment_label),
                        onClick = { onUIEvent(WooPosTotalsUIEvent.CollectPaymentClicked) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PreparingReader(readerStatus: WooPosTotalsViewState.ReaderStatus) {
    WooPosCircularLoadingIndicator(modifier = Modifier.size(156.dp))
    Spacer(modifier = Modifier.height(20.dp.toAdaptivePadding()))
    Text(
        text = readerStatus.title,
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(16.dp.toAdaptivePadding()))
    Text(
        text = readerStatus.subtitle,
        style = MaterialTheme.typography.h4,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ReaderReadyForPayment(readerStatus: WooPosTotalsViewState.ReaderStatus) {
    val tapCardAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.woopos_card_ilustration))
    LottieAnimation(
        modifier = Modifier.size(256.dp),
        composition = tapCardAnimation,
        clipSpec = LottieClipSpec.Markers("reader_awaiting_start", "reader_awaiting_end"),
        iterations = LottieConstants.IterateForever,
    )
    Spacer(modifier = Modifier.height(20.dp.toAdaptivePadding()))
    Text(
        text = readerStatus.title,
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(16.dp.toAdaptivePadding()))
    Text(
        text = readerStatus.subtitle,
        style = MaterialTheme.typography.h4,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun TotalsGrid(state: WooPosTotalsViewState.Totals) {
    Column(
        modifier = Modifier
            .padding(24.dp.toAdaptivePadding())
            .width(382.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TotalsGridRow(
            textOne = stringResource(R.string.woopos_payment_subtotal_label),
            textTwo = state.orderSubtotalText,
        )

        Spacer(modifier = Modifier.height(8.dp.toAdaptivePadding()))

        TotalsGridRow(
            textOne = stringResource(R.string.woopos_payment_tax_label),
            textTwo = state.orderTaxText,
        )

        Spacer(modifier = Modifier.height(16.dp.toAdaptivePadding()))

        Divider(color = WooPosTheme.colors.border, thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp.toAdaptivePadding()))

        TotalsGridRow(
            textOne = stringResource(R.string.woopos_payment_total_label),
            textTwo = state.orderTotalText,
            styleOne = MaterialTheme.typography.h4,
            styleTwo = MaterialTheme.typography.h4,
            fontWeightOne = FontWeight.Medium,
            fontWeightTwo = FontWeight.Bold,
        )
    }
}

@Composable
private fun TotalsGridRow(
    textOne: String,
    textTwo: String,
    styleOne: TextStyle = MaterialTheme.typography.h5,
    styleTwo: TextStyle = MaterialTheme.typography.h5,
    fontWeightOne: FontWeight = FontWeight.Normal,
    fontWeightTwo: FontWeight = FontWeight.Normal,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = textOne,
            style = styleOne,
            fontWeight = fontWeightOne,
        )
        Text(
            text = textTwo,
            style = styleTwo,
            fontWeight = fontWeightTwo,
        )
    }
}

@Composable
private fun TotalsLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            WooPosShimmerBox(
                modifier = Modifier
                    .height(24.dp)
                    .width(332.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(24.dp.toAdaptivePadding()))

            WooPosShimmerBox(
                modifier = Modifier
                    .height(24.dp)
                    .width(332.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(24.dp.toAdaptivePadding()))

            WooPosShimmerBox(
                modifier = Modifier
                    .height(40.dp)
                    .width(332.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun TotalsErrorScreen(
    errorMessage: String,
    onUIEvent: (WooPosTotalsUIEvent) -> Unit
) {
    WooPosErrorScreen(
        message = stringResource(R.string.woopos_totals_main_error_label),
        reason = errorMessage,
        primaryButton = Button(
            text = stringResource(R.string.retry),
            click = { onUIEvent(WooPosTotalsUIEvent.RetryOrderCreationClicked) }
        ),
        adaptToScreenHeight = true,
    )
}

@Composable
@WooPosPreview
fun WooPosTotalsScreenPreview(modifier: Modifier = Modifier) {
    WooPosTheme {
        WooPosTotalsScreen(
            modifier = modifier,
            state = WooPosTotalsViewState.Totals(
                orderSubtotalText = "$420.00",
                orderTotalText = "$462.00",
                orderTaxText = "$42.00",
                cashPaymentAvailability = CashPaymentAvailability.Unavailable,
                readerStatus = WooPosTotalsViewState.ReaderStatus.ReadyForPayment(
                    title = "Ready for payment",
                    subtitle = "Tap, swipe or insert card"
                )
            ),
            onUIEvent = {},
            onNavigationEvent = {}
        )
    }
}

@Composable
@WooPosPreview
fun WooPosTotalsScreenPreviewReaderNotConnected(modifier: Modifier = Modifier) {
    WooPosTheme {
        WooPosTotalsScreen(
            modifier = modifier,
            state = WooPosTotalsViewState.Totals(
                orderSubtotalText = "$420.00",
                orderTotalText = "$462.00",
                orderTaxText = "$42.00",
                cashPaymentAvailability = CashPaymentAvailability.Unavailable,
                readerStatus = WooPosTotalsViewState.ReaderStatus.Disconnected(
                    title = "Reader not connected",
                    subtitle = "To process this payment, please connect your reader.",
                    actionButonLabel = "Connect to a reader",
                    onAction = {}
                )
            ),
            onUIEvent = {},
            onNavigationEvent = {}
        )
    }
}

@Composable
@WooPosPreview
fun WooPosTotalsScreenPreviewWithCashPaymentAvailable() {
    WooPosTheme {
        WooPosTotalsScreen(
            modifier = Modifier,
            state = WooPosTotalsViewState.Totals(
                orderSubtotalText = "$420.00",
                orderTotalText = "$462.00",
                orderTaxText = "$42.00",
                cashPaymentAvailability = CashPaymentAvailability.Available(orderId = 1),
                readerStatus = WooPosTotalsViewState.ReaderStatus.Disconnected(
                    title = "Reader not connected",
                    subtitle = "To process this payment, please connect your reader.",
                    actionButonLabel = "Connect to a reader",
                    onAction = {}
                )
            ),
            onUIEvent = {},
            onNavigationEvent = {}
        )
    }
}

@Composable
@WooPosPreview
fun TotalsErrorPreview() {
    val readerStatus = WooPosTotalsViewState.ReaderStatus.Disconnected(
        title = "Reader not connected",
        subtitle = "To process this payment, please connect your reader.",
        actionButonLabel = "Connect to a reader",
        onAction = {}
    )
    WooPosTheme {
        ReaderDisconnected(modifier = Modifier, status = readerStatus)
    }
}

@Composable
@WooPosPreview
fun WooPosTotalsScreenLoadingPreview() {
    WooPosTheme {
        WooPosTotalsScreen(
            state = WooPosTotalsViewState.Loading,
            onUIEvent = {},
            onNavigationEvent = {}
        )
    }
}

@Composable
@WooPosPreview
fun WooPosTotalsErrorScreenPreview() {
    WooPosTheme {
        TotalsErrorScreen(
            errorMessage = "An error occurred. Please try again.",
            onUIEvent = {}
        )
    }
}

@Composable
@WooPosPreview
fun PreparingReaderPReview() {
    val readerStatus = WooPosTotalsViewState.ReaderStatus.Preparing(
        title = "Getting ready",
        subtitle = "Preparing reader for payment"
    )
    WooPosTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WooPosTheme.colors.totalsErrorBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PreparingReader(readerStatus)
        }
    }
}
