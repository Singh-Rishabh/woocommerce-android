package com.woocommerce.android.ui.orders.wooshippinglabels.address.origin

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.extensions.combine
import com.woocommerce.android.ui.orders.wooshippinglabels.address.AddressValidationHelper
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooShippingEditOriginViewModel @Inject constructor(
    private val addressValidator: AddressValidationHelper,
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    private var name by mutableStateOf(InputValue(""))
    private var company by mutableStateOf(InputValue(""))
    private var country by mutableStateOf("USA")
    private var address by mutableStateOf(InputValue(""))
    private var city by mutableStateOf(InputValue(""))
    private var state by mutableStateOf("CALIFORNIA")
    private var postalCode by mutableStateOf(InputValue(""))
    private var email by mutableStateOf(InputValue(""))
    private var phone by mutableStateOf(InputValue(""))

    private val nameValidatedFlow = snapshotFlow { name }
        .combine(snapshotFlow { company }) { name, company ->
            Pair(name, company)
        }.transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
            transformAfterIndex = 1
        ) { inputValues ->
            inputValues.copy(
                first = inputValues.first.copy(
                    error = addressValidator.validateAtLeastOneOf(inputValues.first.value, inputValues.second.value)
                )
            )
        }

    private val addressValidatedFlow = snapshotFlow { address }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
            transformAfterIndex = 1
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateFieldRequired(inputValue.value))
        }

    private val cityValidatedFlow = snapshotFlow { city }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
            transformAfterIndex = 1
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateFieldRequired(inputValue.value))
        }

    private val postalCodeValidatedFlow = snapshotFlow { postalCode }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
            transformAfterIndex = 1
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateFieldRequired(inputValue.value))
        }

    private val emailValidatedFlow = snapshotFlow { email }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
            transformAfterIndex = 1
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateFieldRequired(inputValue.value))
        }

    private val phoneValidatedFlow = snapshotFlow { phone }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
            transformAfterIndex = 1
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateCustomsPhone(inputValue.value))
        }

    private val editableAddress = combine(
        nameValidatedFlow,
        addressValidatedFlow,
        cityValidatedFlow,
        postalCodeValidatedFlow,
        emailValidatedFlow,
        phoneValidatedFlow
    ) { name, address, city, postalCode, email, phone ->
        Log.d("Name Input", name.toString())
        EditableAddress(
            name = name.first,
            company = name.second,
            country = country,
            state = state,
            address = address,
            city = city,
            postalCode = postalCode,
            email = email,
            phone = phone
        )
    }

    val viewState: MutableStateFlow<EditOrderViewState> =
        MutableStateFlow(EditOrderViewState.DataState(EditableAddress()))

    init {
        launch { observeAddressChanges() }
    }

    private suspend fun observeAddressChanges() {
        editableAddress
            .collectLatest {
                viewState.value = EditOrderViewState.DataState(it)
            }
    }

    fun onNameChange(value: String) {
        name = InputValue(value)
    }

    fun onCompanyChange(value: String) {
        company = InputValue(value)
    }

    fun onAddressChange(value: String) {
        address = InputValue(value)
    }

    fun onCityChange(value: String) {
        city = InputValue(value)
    }

    fun onPostalCodeChange(value: String) {
        postalCode = InputValue(value)
    }

    fun onEmailChange(value: String) {
        email = InputValue(value)
    }

    fun onPhoneChange(value: String) {
        phone = InputValue(value)
    }

    sealed class EditOrderViewState {
        data class DataState(
            val editableAddress: EditableAddress
        ) : EditOrderViewState()
    }

    companion object {
        private const val DELAY_TIME_MILLIS = 500L
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.transformLatestWithDelay(
    delayMillis: Long,
    transformAfterIndex: Int = 0,
    transform: (T) -> T,
): Flow<T> = this.withIndex().transformLatest { (index, value) ->
    emit(value)
    if (index > transformAfterIndex - 1) {
        delay(delayMillis)
        emit(transform(value))
    }
}

data class EditableAddress(
    val name: InputValue = InputValue.EMPTY,
    val company: InputValue = InputValue.EMPTY,
    val country: String = "",
    val address: InputValue = InputValue.EMPTY,
    val city: InputValue = InputValue.EMPTY,
    val state: String = "",
    val postalCode: InputValue = InputValue.EMPTY,
    val email: InputValue = InputValue.EMPTY,
    val phone: InputValue = InputValue.EMPTY
)

data class InputValue(
    val value: String,
    val error: String? = null
) {
    companion object {
        val EMPTY = InputValue("")
    }
}
