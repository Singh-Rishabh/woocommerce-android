package com.cataloghub.android.ui.products.variations

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GenerateVariationBottomSheetViewModel @Inject constructor(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    fun onGenerateAllVariationsClicked() {
        triggerEvent(GenerateAllVariations)
    }

    fun onAddNewVariationClicked() {
        triggerEvent(AddNewVariation)
    }

    object AddNewVariation : Event()
    object GenerateAllVariations : Event()
}
