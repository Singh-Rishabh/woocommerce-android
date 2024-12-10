package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.NullableCurrencyTextFieldValueMapper
import com.woocommerce.android.ui.payments.changeduecalculator.CurrencyVisualTransformation
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosButton
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosButtonState
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosTypedInputField
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent
import org.wordpress.android.fluxc.model.WCSettingsModel
import java.math.BigDecimal

@Composable
fun WooPosCashPaymentScreen(onNavigationEvent: (WooPosNavigationEvent) -> Unit) {
    val viewModel = hiltViewModel<WooPosCashPaymentViewModel>()
    val state = viewModel.state.collectAsState().value

    WooPosCashPaymentScreen(
        state = state,
        onAmountChanged = { viewModel.onUIEvent(WooPosCashPaymentUIEvent.AmountChanged(it)) },
        onCompleteOrderClicked = { viewModel.onUIEvent(WooPosCashPaymentUIEvent.CompleteOrderClicked) },
        onBackClicked = { onNavigationEvent(WooPosNavigationEvent.BackFromCashPayment) },
        onOrderComplete = { onNavigationEvent(WooPosNavigationEvent.OpenHomeFromCashPaymentAfterSuccessfulPayment) },
    )
}

@Composable
fun WooPosCashPaymentScreen(
    state: WooPosCashPaymentState,
    onAmountChanged: (BigDecimal?) -> Unit,
    onCompleteOrderClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onOrderComplete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Toolbar(onBackClicked)

        when (state) {
            is WooPosCashPaymentState.Collecting -> {
                Collecting(
                    state = state,
                    onAmountChanged = onAmountChanged,
                    onCompleteOrderClicked = onCompleteOrderClicked,
                )
            }

            WooPosCashPaymentState.Complete -> onOrderComplete()
            WooPosCashPaymentState.Initiating -> {
                // show nothing
            }
        }
    }
}

@Suppress("DestructuringDeclarationWithTooManyEntries")
@Composable
private fun Collecting(
    state: WooPosCashPaymentState.Collecting,
    onAmountChanged: (BigDecimal?) -> Unit,
    onCompleteOrderClicked: () -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 64.dp,
                bottom = 16.dp,
            )
    ) {
        val (input, total, changeDue, button) = createRefs()
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }

        val totalBarrier = createStartBarrier(total, changeDue)

        var inputText by remember { mutableStateOf(state.enteredAmount) }

        WooPosTypedInputField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .constrainAs(input) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(totalBarrier, margin = 16.dp)
                    width = Dimension.fillToConstraints
                },
            value = inputText,
            label = stringResource(R.string.woopos_cash_payment_enter_amount_label),
            valueMapper = NullableCurrencyTextFieldValueMapper.create(
                decimalSeparator = state.decimalSeparator,
                numberOfDecimals = state.numberOfDecimals
            ),
            onValueChange = {
                onAmountChanged(it)
                inputText = it
            },
            visualTransformation = CurrencyVisualTransformation(
                state.currencySymbol,
                state.currencyPosition
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            errorMessage = state.errorMessage,
        )

        Text(
            text = state.totalText,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .constrainAs(total) {
                    top.linkTo(input.top)
                    bottom.linkTo(input.bottom)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = state.changeDue,
            style = MaterialTheme.typography.subtitle2,
            color = WooPosTheme.colors.warning,
            modifier = Modifier
                .constrainAs(changeDue) {
                    bottom.linkTo(input.bottom)
                    end.linkTo(parent.end)
                }
        )

        val buttonTopMargin = 48.dp.toAdaptivePadding()

        WooPosButton(
            text = state.button.text,
            onClick = onCompleteOrderClicked,
            state = when (state.button.status) {
                WooPosCashPaymentState.Collecting.Button.Status.ENABLED -> WooPosButtonState.ENABLED
                WooPosCashPaymentState.Collecting.Button.Status.DISABLED -> WooPosButtonState.DISABLED
                WooPosCashPaymentState.Collecting.Button.Status.LOADING -> WooPosButtonState.LOADING
            },
            modifier = Modifier
                .constrainAs(button) {
                    top.linkTo(changeDue.bottom, margin = buttonTopMargin)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }
        )
    }
}

@Composable
private fun Toolbar(onBackClicked: () -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp.toAdaptivePadding())
            .height(40.dp)
    ) {
        val (backButton, title) = createRefs()
        IconButton(
            onClick = { onBackClicked() },
            modifier = Modifier
                .constrainAs(backButton) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    centerVerticallyTo(parent)
                }
                .padding(start = 8.dp.toAdaptivePadding())
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_back_24dp),
                contentDescription = stringResource(R.string.woopos_cart_back_content_description),
                tint = MaterialTheme.colors.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }

        val iconTitlePadding = 8.dp.toAdaptivePadding()
        Text(
            text = stringResource(R.string.woopos_cash_payment_title),
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(backButton.top)
                    start.linkTo(backButton.end, margin = iconTitlePadding)
                    centerVerticallyTo(parent)
                }
        )
    }
}

@WooPosPreview
@Composable
fun WooPosTotalsPaymentCashScreen() {
    WooPosTheme {
        WooPosCashPaymentScreen(
            state = WooPosCashPaymentState.Collecting(
                enteredAmount = BigDecimal(100),
                errorMessage = null,
                changeDue = "5$",
                total = BigDecimal(10),
                totalText = "10$",
                currencySymbol = "$",
                currencyPosition = WCSettingsModel.CurrencyPosition.LEFT,
                decimalSeparator = ".",
                numberOfDecimals = 2,
                button = WooPosCashPaymentState.Collecting.Button(
                    text = "Mark order as complete",
                    status = WooPosCashPaymentState.Collecting.Button.Status.DISABLED
                )
            ),
            onAmountChanged = {},
            onCompleteOrderClicked = {},
            onBackClicked = {},
            onOrderComplete = {},
        )
    }
}

@WooPosPreview
@Composable
fun WooPosTotalsPaymentCashWithLabelScreen() {
    WooPosTheme {
        WooPosCashPaymentScreen(
            state = WooPosCashPaymentState.Collecting(
                enteredAmount = null,
                errorMessage = null,
                changeDue = "5$",
                total = BigDecimal(10),
                totalText = "10$",
                currencySymbol = "$",
                currencyPosition = WCSettingsModel.CurrencyPosition.LEFT,
                decimalSeparator = ".",
                numberOfDecimals = 2,
                button = WooPosCashPaymentState.Collecting.Button(
                    text = "Mark order as complete",
                    status = WooPosCashPaymentState.Collecting.Button.Status.LOADING
                )
            ),
            onAmountChanged = {},
            onCompleteOrderClicked = {},
            onBackClicked = {},
            onOrderComplete = {},
        )
    }
}

@WooPosPreview
@Composable
fun WooPosTotalsPaymentCashWithErrorScreen() {
    WooPosTheme {
        WooPosCashPaymentScreen(
            state = WooPosCashPaymentState.Collecting(
                enteredAmount = BigDecimal(500),
                errorMessage = "Amount must be more or equal to total",
                changeDue = "5$",
                total = BigDecimal(10),
                totalText = "10$",
                currencySymbol = "$",
                currencyPosition = WCSettingsModel.CurrencyPosition.LEFT,
                decimalSeparator = ".",
                numberOfDecimals = 2,
                button = WooPosCashPaymentState.Collecting.Button(
                    text = "Mark order as complete",
                    status = WooPosCashPaymentState.Collecting.Button.Status.ENABLED
                )
            ),
            onAmountChanged = {},
            onCompleteOrderClicked = {},
            onBackClicked = {},
            onOrderComplete = {},
        )
    }
}
