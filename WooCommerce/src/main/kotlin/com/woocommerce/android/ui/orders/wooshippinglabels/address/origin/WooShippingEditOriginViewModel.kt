package com.woocommerce.android.ui.orders.wooshippinglabels.address.origin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.extensions.combine
import com.woocommerce.android.extensions.isNotNullOrEmpty
import com.woocommerce.android.model.AmbiguousLocation
import com.woocommerce.android.model.Location
import com.woocommerce.android.ui.orders.details.editing.address.LocationCode
import com.woocommerce.android.ui.orders.wooshippinglabels.address.AddressValidationHelper
import com.woocommerce.android.util.StringUtils.combineStrings
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
class WooShippingEditOriginViewModel @Inject constructor(
    private val addressValidator: AddressValidationHelper,
    private val getAcceptedOriginCountries: GetAcceptedOriginCountries,
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    private var name by mutableStateOf(InputValue(""))
    private var company by mutableStateOf(InputValue(""))
    private var address by mutableStateOf(InputValue(""))
    private var city by mutableStateOf(InputValue(""))
    private var state by mutableStateOf("")
    private var postalCode by mutableStateOf(InputValue(""))
    private var email by mutableStateOf(InputValue(""))
    private var phone by mutableStateOf(InputValue(""))

    private var country = MutableStateFlow(Location.EMPTY)
    private val countriesState = MutableStateFlow<CountriesState>(CountriesState.Loading)

    private val navArgs: WooShippingEditOriginAddressFragmentArgs by savedState.navArgs()

    private val nameValidatedFlow = snapshotFlow { name }
        .combine(snapshotFlow { company }) { name, company ->
            Pair(name, company)
        }.transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
        ) { inputValues ->
            inputValues.copy(
                first = inputValues.first.copy(
                    error = addressValidator.validateAtLeastOneOf(
                        inputValues.first.value,
                        inputValues.second.value
                    )
                )
            )
        }

    private val addressValidatedFlow = snapshotFlow { address }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateFieldRequired(inputValue.value))
        }

    private val cityValidatedFlow = snapshotFlow { city }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateFieldRequired(inputValue.value))
        }

    private val postalCodeValidatedFlow = snapshotFlow { postalCode }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateFieldRequired(inputValue.value))
        }

    private val emailValidatedFlow = snapshotFlow { email }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateFieldRequired(inputValue.value))
        }

    private val phoneValidatedFlow = snapshotFlow { phone }
        .transformLatestWithDelay(
            delayMillis = DELAY_TIME_MILLIS,
        ) { inputValue ->
            inputValue.copy(error = addressValidator.validateUSCustomsPhone(inputValue.value))
        }

    private val isCompanyExpanded = MutableStateFlow(false)

    private val editableAddress = combine(
        nameValidatedFlow,
        addressValidatedFlow,
        cityValidatedFlow,
        postalCodeValidatedFlow,
        emailValidatedFlow,
        phoneValidatedFlow,
        country
    ) { name, address, city, postalCode, email, phone, country ->
        EditableAddress(
            name = name.first,
            company = name.second,
            country = country.name,
            state = state,
            address = address,
            city = city,
            postalCode = postalCode,
            email = email,
            phone = phone
        )
    }

    val viewState: MutableStateFlow<EditAddressViewState> = MutableStateFlow(
        EditAddressViewState.DataState(
            isCompanyExpanded = false,
            editableAddress = EditableAddress(),
            shouldDisplayLoadingCountries = false,
            shouldDisplayLoadingCountriesError = false
        )
    )

    init {
        launch { observeChanges() }
        fillAddressForm()
        launch { loadCountries() }
    }

    private fun fillAddressForm() {
        val fullName = combineStrings(
            navArgs.originAddress.firstName.orEmpty(),
            navArgs.originAddress.lastName.orEmpty()
        )
        val fullAddress = combineStrings(
            navArgs.originAddress.address1.orEmpty(),
            navArgs.originAddress.address2.orEmpty()
        )
        name = InputValue(fullName)
        company = InputValue(navArgs.originAddress.company.orEmpty())
        country.value = findCountryByCountryCode(navArgs.originAddress.country)
        address = InputValue(fullAddress)
        city = InputValue(navArgs.originAddress.city.orEmpty())
        state = navArgs.originAddress.state.orEmpty()
        postalCode = InputValue(navArgs.originAddress.postcode)
        email = InputValue(navArgs.originAddress.email.orEmpty())
        phone = InputValue(navArgs.originAddress.phone.orEmpty())
        isCompanyExpanded.value = navArgs.originAddress.company.isNotNullOrEmpty()
    }

    private fun findCountryByCountryCode(countryCode: String): Location {
        val default = AmbiguousLocation.Raw(countryCode).asLocation()
        return when (val currentState = countriesState.value) {
            is CountriesState.Loaded -> {
                currentState.countries.firstOrNull { it.code == countryCode } ?: default
            }

            else -> default
        }
    }

    private suspend fun loadCountries() {
        getAcceptedOriginCountries().fold(
            onSuccess = {
                countriesState.value = CountriesState.Loaded(it)
                country.value = findCountryByCountryCode(country.value.code)
            },
            onFailure = { countriesState.value = CountriesState.Error }
        )
    }

    fun onExpandCompany() {
        isCompanyExpanded.value = true
    }

    private suspend fun observeChanges() {
        combine(
            editableAddress,
            isCompanyExpanded,
            countriesState
        ) { address, isExpanded, countriesState ->
            EditAddressViewState.DataState(
                isCompanyExpanded = isExpanded,
                editableAddress = address,
                shouldDisplayLoadingCountries = countriesState is CountriesState.DisplayLoading,
                shouldDisplayLoadingCountriesError = countriesState is CountriesState.Error
            )
        }
            .collectLatest {
                viewState.value = it
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

    fun onCountryChange() {
        when (val state = countriesState.value) {
            is CountriesState.Loaded -> {
                triggerEvent(ShowCountrySelector(state.countries))
            }

            is CountriesState.Loading -> {
                countriesState.value = CountriesState.DisplayLoading
            }

            else -> {}
        }
    }

    fun onRefreshCountries() {
        countriesState.value = CountriesState.DisplayLoading
        launch { loadCountries() }
    }

    fun onCountryChanged(code: LocationCode) {
        country.value = findCountryByCountryCode(code)
    }

    sealed class EditAddressViewState {
        data class DataState(
            val isCompanyExpanded: Boolean,
            val editableAddress: EditableAddress,
            val shouldDisplayLoadingCountries: Boolean,
            val shouldDisplayLoadingCountriesError: Boolean,
        ) : EditAddressViewState()
    }

    sealed class CountriesState {
        data object Loading : CountriesState()
        data object DisplayLoading : CountriesState()
        data object Error : CountriesState()
        data class Loaded(val countries: List<Location>) : CountriesState()
    }

    data class ShowCountrySelector(
        val countries: List<Location>
    ) : MultiLiveEvent.Event()

    companion object {
        private const val DELAY_TIME_MILLIS = 500L
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.transformLatestWithDelay(
    delayMillis: Long,
    transform: (T) -> T,
): Flow<T> = this.transformLatest { value ->
    emit(value)
    delay(delayMillis)
    emit(transform(value))
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
