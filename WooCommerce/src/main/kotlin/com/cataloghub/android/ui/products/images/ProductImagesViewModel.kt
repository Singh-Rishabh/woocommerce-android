package com.cataloghub.android.ui.products.images

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.R
import com.cataloghub.android.RequestCodes
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.extensions.areSameImagesAs
import com.cataloghub.android.model.Product
import com.cataloghub.android.model.UiString.UiStringText
import com.cataloghub.android.model.toAppModel
import com.cataloghub.android.tools.NetworkStatus
import com.cataloghub.android.ui.media.MediaFileUploadHandler
import com.cataloghub.android.ui.media.getMediaUploadErrorMessage
import com.cataloghub.android.ui.products.ProductNavigationTarget
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ProductImagesState.Browsing
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ProductImagesState.Dragging
import com.cataloghub.android.util.swap
import com.cataloghub.android.viewmodel.LiveDataDelegate
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ResourceProvider
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class ProductImagesViewModel @Inject constructor(
    private val networkStatus: NetworkStatus,
    private val mediaFileUploadHandler: MediaFileUploadHandler,
    private val resourceProvider: ResourceProvider,
    private val analyticsTracker: AnalyticsTrackerWrapper,
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    private val navArgs: ProductImagesFragmentArgs by savedState.navArgs()
    private val originalImages = navArgs.images.toList()

    private val isMultiSelectionAllowed = navArgs.requestCode == RequestCodes.PRODUCT_DETAIL_IMAGES

    /**
     * Saving more data than necessary into the SavedState has associated risks which were not known at the time this
     * field was implemented - after we ensure we don't save unnecessary data, we can replace @Suppress("OPT_IN_USAGE")
     * with @OptIn(LiveDelegateSavedStateAPI::class).
     */
    @Suppress("OPT_IN_USAGE")
    val viewStateData = LiveDataDelegate(
        savedState,
        ViewState(
            showSourceChooser = navArgs.showChooser,
            uploadingImageUris = emptyList(),
            isImageDeletingAllowed = true,
            images = navArgs.images.toList(),
            isWarningVisible = !isMultiSelectionAllowed,
            isDragDropDescriptionVisible = isMultiSelectionAllowed
        )
    ) { old, new ->
        if (old != new) {
            updateButtonStates()
            updateDragAndDropDescriptionStates()
        }
    }
    private var viewState by viewStateData

    val images
        get() = viewState.images ?: emptyList()

    val isImageDeletingAllowed
        get() = viewState.isImageDeletingAllowed ?: true

    init {
        if (viewState.showSourceChooser == true) {
            viewState = viewState.copy(showSourceChooser = false)
            triggerEvent(ShowImageSourceDialog)
        } else if (navArgs.selectedImage != null) {
            triggerEvent(ShowImageDetail(navArgs.selectedImage!!))
        }

        observeImageUploadEvents()
    }

    fun uploadProductImages(remoteProductId: Long, localUriList: List<Uri>) {
        if (!networkStatus.isConnected()) {
            triggerEvent(MultiLiveEvent.Event.ShowSnackbar(R.string.offline_error))
            return
        }

        mediaFileUploadHandler.enqueueUpload(remoteProductId, localUriList.map { it.toString() })

        if (!isMultiSelectionAllowed) {
            viewState = viewState.copy(images = emptyList())
        }
    }

    fun onShowStorageChooserButtonClicked() {
        AnalyticsTracker.track(
            AnalyticsEvent.PRODUCT_IMAGE_SETTINGS_ADD_IMAGES_SOURCE_TAPPED,
            mapOf(AnalyticsTracker.KEY_IMAGE_SOURCE to AnalyticsTracker.IMAGE_SOURCE_DEVICE)
        )
        triggerEvent(ShowStorageChooser)
    }

    fun onShowCameraButtonClicked() {
        AnalyticsTracker.track(
            AnalyticsEvent.PRODUCT_IMAGE_SETTINGS_ADD_IMAGES_SOURCE_TAPPED,
            mapOf(AnalyticsTracker.KEY_IMAGE_SOURCE to AnalyticsTracker.IMAGE_SOURCE_CAMERA)
        )
        triggerEvent(ShowCamera)
    }

    fun onShowWPMediaPickerButtonClicked() {
        AnalyticsTracker.track(
            AnalyticsEvent.PRODUCT_IMAGE_SETTINGS_ADD_IMAGES_SOURCE_TAPPED,
            mapOf(AnalyticsTracker.KEY_IMAGE_SOURCE to AnalyticsTracker.IMAGE_SOURCE_WPMEDIA)
        )
        triggerEvent(ShowWPMediaPicker)
    }

    fun onImageRemoved(imageId: Long) {
        viewState = viewState.copy(images = images.filter { it.id != imageId })
    }

    fun onMediaLibraryImagesAdded(newImages: List<Product.Image>) {
        viewState = if (isMultiSelectionAllowed) {
            viewState.copy(images = images + newImages)
        } else {
            viewState.copy(images = newImages)
        }
    }

    fun onImageSourceButtonClicked() {
        clearImageUploadErrors()
        AnalyticsTracker.track(AnalyticsEvent.PRODUCT_IMAGE_SETTINGS_ADD_IMAGES_BUTTON_TAPPED)
        triggerEvent(ShowImageSourceDialog)
    }

    fun onGalleryImageClicked(image: Product.Image) {
        AnalyticsTracker.track(AnalyticsEvent.PRODUCT_DETAIL_IMAGE_TAPPED)
        triggerEvent(ShowImageDetail(image))
    }

    fun onValidateButtonClicked() {
        viewState = viewState.copy(
            images = images.updateProductCoverImageToFirstItem(),
            productImagesState = Browsing
        )
    }

    fun onNavigateBackButtonClicked() {
        when (val productImagesState = viewState.productImagesState) {
            is Dragging -> {
                viewState = viewState.copy(
                    productImagesState = Browsing,
                    images = productImagesState.initialState
                )
            }

            Browsing -> {
                val hasChange = !images.areSameImagesAs(originalImages)
                analyticsTracker.track(
                    AnalyticsEvent.PRODUCT_IMAGE_SETTINGS_DONE_BUTTON_TAPPED,
                    mapOf(AnalyticsTracker.KEY_HAS_CHANGED_DATA to hasChange)
                )
                if (hasChange) {
                    triggerEvent(MultiLiveEvent.Event.ExitWithResult(images))
                } else {
                    triggerEvent(MultiLiveEvent.Event.Exit)
                }
            }
        }
    }

    private fun updateButtonStates() {
        val numImages = (viewState.images?.size ?: 0) + (viewState.uploadingImageUris?.size ?: 0)
        viewState = viewState.copy(
            chooserButtonButtonTitleRes = when {
                isMultiSelectionAllowed -> R.string.product_add_photos
                numImages > 0 -> R.string.product_replace_photo
                else -> R.string.product_add_photo
            }
        )
    }

    private fun updateDragAndDropDescriptionStates() {
        viewState = viewState.copy(
            isDragDropDescriptionVisible = viewState.productImagesState is Dragging || images.size > 1
        )
    }

    private fun clearImageUploadErrors() {
        // clear existing image upload errors from the backlog
        mediaFileUploadHandler.clearImageErrors(navArgs.remoteId)
    }

    private fun observeImageUploadEvents() {
        val remoteProductId = navArgs.remoteId
        mediaFileUploadHandler.observeCurrentUploads(remoteProductId)
            .map { list -> list.map { it.toUri() } }
            .onEach { viewState = viewState.copy(uploadingImageUris = it) }
            .launchIn(this)

        mediaFileUploadHandler.observeSuccessfulUploads(remoteProductId)
            .onEach { media ->
                viewState = if (isMultiSelectionAllowed) {
                    viewState.copy(images = images + media.toAppModel())
                } else {
                    viewState.copy(images = listOf(media.toAppModel()))
                }
            }
            .launchIn(this)

        mediaFileUploadHandler.observeCurrentUploadErrors(remoteProductId)
            .onEach { errorList ->
                if (errorList.isEmpty()) {
                    viewState = viewState.copy(hasUploadErrors = false)
                } else {
                    viewState = viewState.copy(hasUploadErrors = true)
                    triggerEvent(
                        MultiLiveEvent.Event.ShowUiStringSnackbar(
                            message = UiStringText(
                                resourceProvider.getMediaUploadErrorMessage(errorList.size)
                            ),
                        )
                    )
                }
            }
            .launchIn(this)
    }

    fun onGalleryImageDragStarted() {
        when (viewState.productImagesState) {
            is Dragging -> { /* no-op*/ }

            Browsing -> viewState = viewState.copy(
                images = images.uncheckProductCoverImage(),
                productImagesState = Dragging(images)
            )
        }
    }

    fun onGalleryImageDeleteIconClicked(image: Product.Image) {
        triggerEvent(ShowDeleteImageConfirmation(image))
    }

    fun onDeleteImageConfirmed(image: Product.Image) {
        viewState = viewState.copy(images = images - image)
    }

    fun onGalleryImageMoved(from: Int, to: Int) {
        val canSwap = from >= 0 && from < images.size && to >= 0 && to < images.size
        if (canSwap) {
            val reorderedImages = images.swap(from, to)
            viewState = viewState.copy(images = reorderedImages)
        }
    }

    fun openUploadScreen() {
        triggerEvent(ProductNavigationTarget.ViewMediaUploadErrors(navArgs.remoteId))
    }

    private fun List<Product.Image>.updateProductCoverImageToFirstItem() =
        this.mapIndexed { index, image -> image.copy(isCoverImage = index == 0) }

    private fun List<Product.Image>.uncheckProductCoverImage() = this.map { it.copy(isCoverImage = false) }

    @Parcelize
    data class ViewState(
        val showSourceChooser: Boolean? = null,
        val uploadingImageUris: List<Uri>? = null,
        val isImageDeletingAllowed: Boolean? = null,
        val images: List<Product.Image>? = null,
        val chooserButtonButtonTitleRes: Int? = null,
        val isWarningVisible: Boolean? = null,
        val hasUploadErrors: Boolean? = null,
        val isDragDropDescriptionVisible: Boolean? = null,
        val productImagesState: ProductImagesState = Browsing
    ) : Parcelable

    object ShowImageSourceDialog : MultiLiveEvent.Event()
    object ShowStorageChooser : MultiLiveEvent.Event()
    object ShowCamera : MultiLiveEvent.Event()
    object ShowWPMediaPicker : MultiLiveEvent.Event()
    data class ShowDeleteImageConfirmation(val image: Product.Image) : MultiLiveEvent.Event()
    data class ShowImageDetail(val image: Product.Image) : MultiLiveEvent.Event()

    sealed class ProductImagesState : Parcelable {
        @Parcelize
        data class Dragging(val initialState: List<Product.Image>) : ProductImagesState()

        @Parcelize
        object Browsing : ProductImagesState()
    }
}
