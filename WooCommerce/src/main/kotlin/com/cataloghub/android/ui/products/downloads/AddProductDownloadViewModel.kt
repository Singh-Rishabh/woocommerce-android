package com.cataloghub.android.ui.products.downloads

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.model.ProductFile
import com.cataloghub.android.ui.products.ProductNavigationTarget.ViewProductDownloadDetails
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddProductDownloadViewModel @Inject constructor(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    fun onMediaGalleryClicked() {
        triggerEvent(PickFileFromMedialLibrary)
    }

    fun onDeviceMediaFilesClicked() {
        triggerEvent(PickMediaFileFromDevice)
    }

    fun onDeviceDocumentsClicked() {
        triggerEvent(PickDocumentFromDevice)
    }

    fun onEnterURLClicked() {
        triggerEvent(
            ViewProductDownloadDetails(
                isEditing = false,
                file = ProductFile(id = null, url = "", name = "")
            )
        )
    }

    fun launchFileUpload(uri: Uri) {
        triggerEvent(AddFile(uri))
    }

    object PickFileFromMedialLibrary : Event()
    object PickMediaFileFromDevice : Event()
    object PickDocumentFromDevice : Event()
    data class AddFile(val uri: Uri) : Event()
}
