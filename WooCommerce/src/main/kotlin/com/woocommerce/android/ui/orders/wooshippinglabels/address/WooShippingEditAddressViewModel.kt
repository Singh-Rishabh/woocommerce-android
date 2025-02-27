package com.woocommerce.android.ui.orders.wooshippinglabels.address

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.R
import com.woocommerce.android.extensions.combine
import com.woocommerce.android.extensions.isNotNullOrEmpty
import com.woocommerce.android.model.Address
import com.woocommerce.android.model.AmbiguousLocation
import com.woocommerce.android.model.Location
import com.woocommerce.android.ui.orders.details.editing.address.LocationCode
import com.woocommerce.android.ui.orders.wooshippinglabels.address.origin.GetAcceptedOriginCountries
import com.woocommerce.android.ui.orders.wooshippinglabels.address.origin.UpdateOriginAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.models.AddressNormalizationModel
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.util.StringUtils.combineStrings
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event
import com.woocommerce.android.viewmodel.ResourceProvider
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
class WooShippingEditAddressViewModel @Inject constructor(
    private val addressValidator: AddressValidationHelper,
    private val getAcceptedOriginCountries: GetAcceptedOriginCountries,
    private val getStatesByCountryCode: GetStatesByCountryCode,
    private val normalizeAddress: NormalizeAddress,
    private val resourceProvider: ResourceProvider,
    private val updateOriginAddress: UpdateOriginAddress,
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    private var name by mutableStateOf(InputValue(""))
    private var company by mutableStateOf(InputValue(""))
    private var address by mutableStateOf(InputValue(""))
    private var city by mutableStateOf(InputValue(""))
    private var postalCode by mutableStateOf(InputValue(""))
    private var email by mutableStateOf(InputValue(""))
    private var phone by mutableStateOf(InputValue(""))

    private var country = MutableStateFlow(Location.EMPTY)

    private var rawState by mutableStateOf("")
    private val selectedState = MutableStateFlow(Location.EMPTY)

    private val state = combine(
        snapshotFlow { rawState },
        selectedState
    ) { rawState, selectedState ->
        if (selectedState != Location.EMPTY) selectedState else AmbiguousLocation.Raw(rawState).asLocation()
    }

    private val countriesState = MutableStateFlow<LocationState>(LocationState.Loading)
    private val statesState = MutableStateFlow<LocationState>(LocationState.Loading)

    private val addressValidationState =
        MutableStateFlow<AddressValidationState>(AddressValidationState.NotStarted)

    private val navArgs: WooShippingEditAddressFragmentArgs by savedState.navArgs()

    private val currentAddress = when (val currentFlow = navArgs.flow) {
        is EditAddressFlow.EditDestinationAddress -> currentFlow.address.address
        is EditAddressFlow.EditOriginAddress -> currentFlow.address.toAddress()
    }.let { MutableStateFlow(it) }

    private val isVerified = when (val currentFlow = navArgs.flow) {
        is EditAddressFlow.EditDestinationAddress -> currentFlow.address.isVerified
        is EditAddressFlow.EditOriginAddress -> currentFlow.address.isVerified
    }.let { MutableStateFlow(it) }

    private val addressId = (navArgs.flow as? EditAddressFlow.EditOriginAddress)?.address?.id

    val screenTitle = when (navArgs.flow) {
        is EditAddressFlow.EditDestinationAddress ->
            resourceProvider.getString(R.string.woo_shipping_edit_destination_address_title)
        is EditAddressFlow.EditOriginAddress ->
            resourceProvider.getString(R.string.woo_shipping_edit_origin_address_title)
    }

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
        country,
        state
    ) { name, address, city, postalCode, email, phone, country, state ->
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

    val viewState: MutableStateFlow<ViewState> = MutableStateFlow(
        ViewState(
            isCompanyExpanded = false,
            editableAddress = EditableAddress(),
            loading = LoadingState.Hidden,
            error = null,
            shouldUseStatesInput = false,
            addressStatus = AddressStatus.UNVERIFIED,
            addressValidationState = AddressValidationState.NotStarted
        )
    )

    init {
        launch { observeChanges() }
        fillAddressForm(currentAddress.value)
        launch {
            loadCountries()
            loadStates()
        }
    }

    private fun fillAddressForm(addressInformation: Address) {
        val fullName = combineStrings(
            addressInformation.firstName,
            addressInformation.lastName
        )
        val fullAddress = combineStrings(
            addressInformation.address1,
            addressInformation.address2
        )
        name = InputValue(fullName)
        company = InputValue(addressInformation.company)
        country.value = findLocationByCode(addressInformation.country.code, countriesState.value)
        address = InputValue(fullAddress)
        city = InputValue(addressInformation.city)
        selectedState.value = findLocationByCode(addressInformation.state.codeOrRaw, statesState.value)
        postalCode = InputValue(addressInformation.postcode)
        email = InputValue(addressInformation.email)
        phone = InputValue(addressInformation.phone)
        isCompanyExpanded.value = addressInformation.company.isNotNullOrEmpty()
    }

    private fun findLocationByCode(code: String, state: LocationState): Location {
        val default = AmbiguousLocation.Raw(code).asLocation()
        return when (val currentState = state) {
            is LocationState.Loaded -> {
                currentState.locations.firstOrNull { it.code == code } ?: default
            }

            else -> default
        }
    }

    private suspend fun loadCountries() {
        getAcceptedOriginCountries().fold(
            onSuccess = {
                countriesState.value = LocationState.Loaded(it)
                country.value = findLocationByCode(country.value.code, countriesState.value)
            },
            onFailure = { countriesState.value = LocationState.Error }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun loadStates() {
        country.mapLatest { country -> getStatesByCountryCode(country.code) }
            .collectLatest { states ->
                statesState.value = LocationState.Loaded(states)
                rawState = ""
                if (states.isNotEmpty()) {
                    findLocationByCode(selectedState.value.code, statesState.value)
                        .takeIf { it != Location.EMPTY }
                        ?.let { selectedState.value = it } ?: run {
                        selectedState.value = states.first()
                    }
                } else {
                    selectedState.value = Location.EMPTY
                }
            }
    }

    fun handleBackPress(): Boolean {
        if (allowBackNavigation()) { onNavigateBack() }
        return false
    }

    fun allowBackNavigation(): Boolean {
        return when (viewState.value.addressValidationState) {
            is AddressValidationState.AddressSelection,
            is AddressValidationState.NormalizedAddressUpdateFailed -> {
                onCloseAddressSelection()
                return false
            }

            else -> true
        }
    }

    fun onNavigateBack() {
        when (navArgs.flow) {
            is EditAddressFlow.EditDestinationAddress -> triggerEvent(
                Event.ExitWithResult(
                    DestinationShippingAddress(currentAddress.value, isVerified.value)
                )
            )

            is EditAddressFlow.EditOriginAddress -> triggerEvent(Event.Exit)
        }
    }

    fun onExpandCompany() {
        isCompanyExpanded.value = true
    }

    private suspend fun observeChanges() {
        combine(
            editableAddress,
            isCompanyExpanded,
            countriesState,
            statesState,
            addressValidationState,
            currentAddress
        ) { address, isExpanded, countriesState, statesState, addressSelection, currentAddress ->

            val loading = getLoadingState(countriesState, statesState, addressSelection)
            val error = getErrorState(countriesState, addressSelection, address)

            val addressStatus = when {
                hasIncorrectOrMissingData(address) -> AddressStatus.MISSING_INFO
                hasOnlyNoAddressChanges(address, currentAddress) -> AddressStatus.SAVE_CHANGES
                isSameAddress(address, currentAddress) && isVerified.value -> AddressStatus.VERIFIED
                else -> AddressStatus.UNVERIFIED
            }

            ViewState(
                isCompanyExpanded = isExpanded,
                editableAddress = address,
                loading = loading,
                error = error,
                shouldUseStatesInput = statesState is LocationState.Loaded && statesState.locations.isEmpty(),
                addressStatus = addressStatus,
                addressValidationState = addressSelection
            )
        }
            .collectLatest {
                viewState.value = it
            }
    }

    private fun getErrorState(
        countriesState: LocationState,
        addressSelection: AddressValidationState,
        editableAddress: EditableAddress
    ): EditAddressError? {
        return when {
            countriesState is LocationState.Error -> {
                EditAddressError(
                    resourceProvider.getString(R.string.woo_shipping_fetching_countries_and_states_failed)
                ) { onRefreshCountries() }
            }

            addressSelection is AddressValidationState.VerificationFailed -> {
                if (editableAddress == addressSelection.editableAddress) {
                    EditAddressError(
                        resourceProvider.getString(R.string.woo_shipping_verifying_address_failed)
                    ) {
                        onNormalizeAddress(addressSelection.editableAddress)
                    }
                } else {
                    addressValidationState.value = AddressValidationState.NotStarted
                    null
                }
            }

            addressSelection is AddressValidationState.AddressUpdateFailed -> {
                if (editableAddress == addressSelection.editableAddress) {
                    EditAddressError(
                        resourceProvider.getString(R.string.woo_shipping_updating_address_failed)
                    ) {
                        onUpdateAddress(addressSelection.editableAddress)
                    }
                } else {
                    addressValidationState.value = AddressValidationState.NotStarted
                    null
                }
            }

            addressSelection is AddressValidationState.NormalizedAddressUpdateFailed -> {
                EditAddressError(
                    resourceProvider.getString(R.string.woo_shipping_updating_address_failed)
                ) {
                    onUpdateNormalizedOriginAddress(addressSelection.selection)
                }
            }

            else -> null
        }
    }

    private fun getLoadingState(
        countriesState: LocationState,
        statesState: LocationState,
        addressSelection: AddressValidationState
    ): LoadingState {
        return when {
            countriesState is LocationState.DisplayLoading || statesState is LocationState.DisplayLoading -> {
                LoadingState.DisplayLoading(
                    resourceProvider.getString(R.string.loading),
                    resourceProvider.getString(R.string.woo_shipping_fetching_countries_and_states)
                )
            }

            addressSelection is AddressValidationState.VerifyingAddress -> {
                LoadingState.DisplayLoading(
                    resourceProvider.getString(R.string.woo_shipping_address_validate_title),
                    resourceProvider.getString(R.string.woo_shipping_address_validate_message)
                )
            }

            addressSelection is AddressValidationState.UpdatingAddress -> {
                LoadingState.DisplayLoading(
                    resourceProvider.getString(R.string.woo_shipping_address_update_title),
                    resourceProvider.getString(R.string.woo_shipping_address_update_message)
                )
            }

            else -> LoadingState.Hidden
        }
    }

    private fun isSameAddress(newAddress: EditableAddress, currentAddress: Address): Boolean {
        val originalFullAddress = combineStrings(
            currentAddress.address1,
            currentAddress.address2
        )

        val isSameAddress = originalFullAddress == newAddress.address.value
        val isSameCity = currentAddress.city == newAddress.city.value
        val isSameState = currentAddress.state.codeOrRaw == newAddress.state.code
        val isSameCountry = currentAddress.country.code == newAddress.country.code
        val isSamePostalCode = currentAddress.postcode == newAddress.postalCode.value

        return isSameAddress && isSameCity && isSameState && isSameCountry && isSamePostalCode
    }

    private fun hasOnlyNoAddressChanges(newAddress: EditableAddress, currentAddress: Address): Boolean {
        val originalFullName = combineStrings(
            currentAddress.firstName,
            currentAddress.lastName
        )
        val isDifferentName = newAddress.name.value != originalFullName
        val isDifferentCompany = newAddress.company.value != currentAddress.company
        val isDifferentEmail = newAddress.email.value != currentAddress.email
        val isDifferentPhone = newAddress.phone.value != currentAddress.phone
        val isSameAddress = isSameAddress(newAddress, currentAddress)
        val isVerified = isVerified.value
        val hasNoAddressChanges = isDifferentName || isDifferentCompany || isDifferentEmail || isDifferentPhone
        return hasNoAddressChanges && isSameAddress && isVerified
    }

    private fun hasIncorrectOrMissingData(editableAddress: EditableAddress): Boolean {
        return editableAddress.address.error.isNotNullOrEmpty() ||
            editableAddress.city.error.isNotNullOrEmpty() ||
            editableAddress.postalCode.error.isNotNullOrEmpty() ||
            editableAddress.email.error.isNotNullOrEmpty() ||
            editableAddress.phone.error.isNotNullOrEmpty() ||
            editableAddress.name.error.isNotNullOrEmpty() ||
            editableAddress.company.error.isNotNullOrEmpty()
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

    fun onRawStateChange(value: String) {
        rawState = value
    }

    fun onCountryChange() {
        when (val state = countriesState.value) {
            is LocationState.Loaded -> {
                triggerEvent(ShowCountrySelector(state.locations))
            }

            is LocationState.Loading -> {
                countriesState.value = LocationState.DisplayLoading
            }

            else -> {}
        }
    }

    fun onStateChange() {
        when (val state = statesState.value) {
            is LocationState.Loaded -> {
                triggerEvent(ShowStateSelector(state.locations))
            }

            is LocationState.Loading -> {
                countriesState.value = LocationState.DisplayLoading
            }

            else -> {}
        }
    }

    fun onRefreshCountries() {
        countriesState.value = LocationState.DisplayLoading
        launch { loadCountries() }
    }

    fun onCountryChanged(code: LocationCode) {
        country.value = findLocationByCode(code, countriesState.value)
    }

    fun onStateChanged(code: LocationCode) {
        selectedState.value = findLocationByCode(code, statesState.value)
    }

    fun onNormalizeAddress(editableAddress: EditableAddress) {
        addressValidationState.value = AddressValidationState.VerifyingAddress
        launch {
            val address = editableAddress.toAddress()
            normalizeAddress(address).fold(
                onSuccess = {
                    addressValidationState.value =
                        AddressValidationState.AddressSelection(it, it.normalizedAddress)
                },
                onFailure = {
                    addressValidationState.value = AddressValidationState.VerificationFailed(editableAddress)
                }
            )
        }
    }

    fun onAddressSelectionChange(addressSelection: AddressValidationState.AddressSelection) {
        addressValidationState.value = addressSelection
    }

    fun onCloseAddressSelection() {
        addressValidationState.value = AddressValidationState.NotStarted
    }

    fun onUpdateNormalizedOriginAddress(selection: AddressValidationState.AddressSelection) {
        addressValidationState.value = AddressValidationState.UpdatingAddress
        launch {
            updateOriginAddress(selection.selectedAddress, addressId).fold(
                onSuccess = {
                    val address = it.toAddress()
                    fillAddressForm(address)
                    addressValidationState.value = AddressValidationState.NotStarted
                    currentAddress.value = address
                    isVerified.value = it.isVerified
                },
                onFailure = {
                    addressValidationState.value = AddressValidationState.NormalizedAddressUpdateFailed(selection)
                }
            )
        }
    }

    fun onUpdateAddress(editableAddress: EditableAddress) {
        when (navArgs.flow) {
            is EditAddressFlow.EditDestinationAddress -> onUpdateDestinationAddress(editableAddress)
            is EditAddressFlow.EditOriginAddress -> onUpdateOriginAddress(editableAddress)
        }
    }

    @Suppress("UnusedParameter")
    private fun onUpdateDestinationAddress(editableAddress: EditableAddress) {
        // To be created
    }

    private fun onUpdateOriginAddress(editableAddress: EditableAddress) {
        addressValidationState.value = AddressValidationState.UpdatingAddress
        launch {
            val address = editableAddress.toAddress()
            updateOriginAddress(address, addressId).fold(
                onSuccess = {
                    val address = it.toAddress()
                    fillAddressForm(address)
                    addressValidationState.value = AddressValidationState.NotStarted
                    currentAddress.value = address
                    isVerified.value = it.isVerified
                },
                onFailure = {
                    addressValidationState.value = AddressValidationState.AddressUpdateFailed(editableAddress)
                }
            )
        }
    }

    data class ViewState(
        val isCompanyExpanded: Boolean,
        val editableAddress: EditableAddress,
        val loading: LoadingState,
        val error: EditAddressError?,
        val shouldUseStatesInput: Boolean,
        val addressStatus: AddressStatus,
        val addressValidationState: AddressValidationState
    )

    sealed class LoadingState {
        data object Hidden : LoadingState()
        data class DisplayLoading(
            val title: String,
            val message: String
        ) : LoadingState()
    }

    data class EditAddressError(
        val message: String,
        val onRetry: () -> Unit
    )

    sealed class LocationState {
        data object Loading : LocationState()
        data object DisplayLoading : LocationState()
        data object Error : LocationState()
        data class Loaded(val locations: List<Location>) : LocationState()
    }

    data class ShowCountrySelector(
        val countries: List<Location>
    ) : Event()

    data class ShowStateSelector(
        val states: List<Location>
    ) : Event()

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
    val country: Location = Location.EMPTY,
    val address: InputValue = InputValue.EMPTY,
    val city: InputValue = InputValue.EMPTY,
    val state: Location = Location.EMPTY,
    val postalCode: InputValue = InputValue.EMPTY,
    val email: InputValue = InputValue.EMPTY,
    val phone: InputValue = InputValue.EMPTY
)

fun EditableAddress.toAddress(): Address {
    return Address(
        firstName = name.value,
        lastName = "",
        company = company.value,
        address1 = address.value,
        address2 = "",
        city = city.value,
        state = AmbiguousLocation.Defined(state),
        postcode = postalCode.value,
        country = country,
        email = email.value,
        phone = phone.value
    )
}

fun OriginShippingAddress.toAddress(): Address {
    return Address(
        firstName = firstName.orEmpty(),
        lastName = lastName.orEmpty(),
        company = company.orEmpty(),
        address1 = address1.orEmpty(),
        address2 = address2.orEmpty(),
        city = city.orEmpty(),
        state = AmbiguousLocation.Raw(state.orEmpty()),
        postcode = postcode,
        country = AmbiguousLocation.Raw(country).asLocation(),
        email = email.orEmpty(),
        phone = phone.orEmpty()
    )
}

sealed class AddressValidationState {
    data object NotStarted : AddressValidationState()
    data object VerifyingAddress : AddressValidationState()
    data class VerificationFailed(
        val editableAddress: EditableAddress
    ) : AddressValidationState()

    data class AddressSelection(
        val addressNormalization: AddressNormalizationModel,
        val selectedAddress: Address
    ) : AddressValidationState()
    data object UpdatingAddress : AddressValidationState()
    data class AddressUpdateFailed(
        val editableAddress: EditableAddress
    ) : AddressValidationState()

    data class NormalizedAddressUpdateFailed(
        val selection: AddressSelection,
    ) : AddressValidationState()
}

@Parcelize
sealed class EditAddressFlow : Parcelable {
    data class EditOriginAddress(val address: OriginShippingAddress) : EditAddressFlow()
    data class EditDestinationAddress(
        val address: DestinationShippingAddress,
        val orderId: Long
    ) : EditAddressFlow()
}

enum class AddressStatus {
    VERIFIED,
    UNVERIFIED,
    MISSING_INFO,
    SAVE_CHANGES
}

data class InputValue(
    val value: String,
    val error: String? = null
) {
    companion object {
        val EMPTY = InputValue("")
    }
}

@Parcelize
data class DestinationShippingAddress(
    val address: Address,
    val isVerified: Boolean
) : Parcelable
