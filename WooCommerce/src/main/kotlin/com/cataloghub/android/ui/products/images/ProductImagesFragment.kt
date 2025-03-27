package com.cataloghub.android.ui.products.images

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.cataloghub.android.AppUrls
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentProductImagesBinding
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.mediapicker.MediaPickerHelper
import com.cataloghub.android.model.Product
import com.cataloghub.android.model.UiString
import com.cataloghub.android.ui.products.BaseProductEditorFragment
import com.cataloghub.android.ui.products.ConfirmRemoveProductImageDialog
import com.cataloghub.android.ui.products.ProductNavigationTarget
import com.cataloghub.android.ui.products.ProductNavigator
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ProductImagesState
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ShowCamera
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ShowDeleteImageConfirmation
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ShowImageDetail
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ShowImageSourceDialog
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ShowStorageChooser
import com.cataloghub.android.ui.products.images.ProductImagesViewModel.ShowWPMediaPicker
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.util.UiHelpers.getTextOfUiString
import com.cataloghub.android.util.setHomeIcon
import com.cataloghub.android.util.setupTabletSecondPaneToolbar
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialog
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowUiStringSnackbar
import com.cataloghub.android.viewmodel.fixedHiltNavGraphViewModels
import com.cataloghub.android.widgets.WCProductImageGalleryView
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.mediapicker.MediaPickerUtils
import org.wordpress.android.mediapicker.api.MediaPickerSetup.DataSource.CAMERA
import org.wordpress.android.mediapicker.api.MediaPickerSetup.DataSource.DEVICE
import org.wordpress.android.mediapicker.api.MediaPickerSetup.DataSource.WP_MEDIA_LIBRARY
import javax.inject.Inject

@AndroidEntryPoint
class ProductImagesFragment :
    BaseProductEditorFragment(R.layout.fragment_product_images),
    WCProductImageGalleryView.OnGalleryImageInteractionListener,
    MediaPickerHelper.MediaPickerResultHandler {
    private val navArgs: ProductImagesFragmentArgs by navArgs()
    private val viewModel: ProductImagesViewModel by fixedHiltNavGraphViewModels(R.id.nav_graph_image_gallery)

    @Inject
    lateinit var navigator: ProductNavigator

    @Inject
    lateinit var mediaPickerUtils: MediaPickerUtils

    @Inject
    lateinit var mediaPickerHelper: MediaPickerHelper

    private var _binding: FragmentProductImagesBinding? = null
    private val binding get() = _binding!!

    override val lastEvent: MultiLiveEvent.Event?
        get() = viewModel.event.value

    private var imageSourceDialog: AlertDialog? = null
    private var imageUploadErrorsSnackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProductImagesBinding.bind(view)

        setupObservers(viewModel)
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        imageUploadErrorsSnackbar?.dismiss()
    }

    override fun onPause() {
        super.onPause()
        imageSourceDialog?.dismiss()
    }

    private fun onCreateMenu(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener {
            onExit()
        }
        updateMenuState(toolbar)
    }

    private fun updateMenuState(toolbar: Toolbar) {
        toolbar.menu.clear()
        when (viewModel.viewStateData.liveData.value?.productImagesState) {
            is ProductImagesState.Dragging -> {
                toolbar.inflateMenu(R.menu.menu_dragging)
                setHomeIcon(R.drawable.ic_gridicons_cross_24dp)
            }

            ProductImagesState.Browsing -> {
                setHomeIcon(R.drawable.ic_back_24dp)
            }

            null -> Unit // Do nothing
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (viewModel.viewStateData.liveData.value?.productImagesState) {
            is ProductImagesState.Dragging -> {
                when (item.itemId) {
                    R.id.menu_validate -> {
                        viewModel.onValidateButtonClicked()
                        true
                    }

                    else -> false
                }
            }

            else -> false
        }
    }

    private fun setupViews() {
        binding.addImageButton.setOnClickListener {
            viewModel.onImageSourceButtonClicked()
        }
        with(binding.learnMoreButton) {
            text = HtmlCompat.fromHtml(
                context.getString(R.string.product_images_learn_more_button),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            movementMethod = LinkMovementMethod.getInstance()
            setOnClickListener {
                ChromeCustomTabUtils.launchUrl(it.context, AppUrls.PRODUCT_IMAGE_UPLOADS_TROUBLESHOOTING)
            }
        }

        binding.openUploadScreenButton.setOnClickListener {
            viewModel.openUploadScreen()
        }

        setupTabletSecondPaneToolbar(
            title = getString(R.string.product_images_title),
            onMenuItemSelected = ::onMenuItemSelected,
            onCreateMenu = ::onCreateMenu
        )
    }

    override fun onGalleryImageDeleteIconClicked(image: Product.Image) {
        viewModel.onGalleryImageDeleteIconClicked(image)
    }

    private fun setupObservers(viewModel: ProductImagesViewModel) {
        viewModel.viewStateData.observe(viewLifecycleOwner) { old, new ->
            new.uploadingImageUris.takeIfNotEqualTo(old?.uploadingImageUris) { uris ->
                updateImages(new.images ?: emptyList(), uris)
            }
            new.images.takeIfNotEqualTo(old?.images) { images ->
                updateImages(images ?: emptyList(), new.uploadingImageUris)
            }
            new.isWarningVisible?.takeIfNotEqualTo(old?.isWarningVisible) { isVisible ->
                binding.textWarning.isVisible = isVisible
            }
            new.chooserButtonButtonTitleRes?.takeIfNotEqualTo(old?.chooserButtonButtonTitleRes) { titleRes ->
                binding.addImageButton.setText(titleRes)
            }
            new.productImagesState.takeIfNotEqualTo(old?.productImagesState) {
                updateMenuState(binding.toolbar)

                when (new.productImagesState) {
                    ProductImagesState.Browsing -> {
                        binding.addImageButton.isEnabled = true
                        binding.imageGallery.setDraggingState(isDragging = false)
                    }

                    is ProductImagesState.Dragging -> {
                        binding.addImageButton.isEnabled = false
                        binding.imageGallery.setDraggingState(isDragging = true)
                    }
                }
            }
            new.hasUploadErrors?.takeIfNotEqualTo(old?.hasUploadErrors) { hasErrors ->
                binding.openUploadScreenButton.visibility = if (hasErrors) View.VISIBLE else View.GONE
            }
            new.isDragDropDescriptionVisible?.takeIfNotEqualTo(old?.isDragDropDescriptionVisible) { isVisible ->
                binding.dragAndDropDescription.isVisible = isVisible
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is Exit -> findNavController().navigateUp()
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ShowUiStringSnackbar -> displayProductImageUploadErrorSnackBar(event.message)
                is ProductNavigationTarget -> navigator.navigate(this, event)
                is ExitWithResult<*> -> navigateBackWithResult(KEY_IMAGES_DIALOG_RESULT, event.data)
                is ShowDialog -> event.showDialog()
                ShowImageSourceDialog -> showImageSourceDialog()
                is ShowImageDetail -> showImageDetail(event.image)
                ShowStorageChooser -> mediaPickerHelper.showMediaPicker(DEVICE, allowMultiSelect = true)
                ShowCamera -> mediaPickerHelper.showMediaPicker(CAMERA)
                ShowWPMediaPicker -> mediaPickerHelper.showMediaPicker(WP_MEDIA_LIBRARY, allowMultiSelect = true)
                is ShowDeleteImageConfirmation -> showConfirmationDialog(event.image)
                else -> event.isHandled = false
            }
        }
    }

    private fun showConfirmationDialog(image: Product.Image) {
        ConfirmRemoveProductImageDialog(
            requireActivity(),
            onPositiveButton = { viewModel.onDeleteImageConfirmed(image) },
            onNegativeButton = { /* no-op */ }
        ).show()
    }

    private fun displayProductImageUploadErrorSnackBar(uiString: UiString) {
        if (imageUploadErrorsSnackbar == null) {
            imageUploadErrorsSnackbar = uiMessageResolver.getUiStringSnack(message = uiString)
        } else {
            imageUploadErrorsSnackbar?.setText(getTextOfUiString(requireContext(), uiString))
        }
        imageUploadErrorsSnackbar?.show()
    }

    private fun updateImages(images: List<Product.Image>, uris: List<Uri>?) {
        binding.imageGallery.showProductImages(images, this)
        binding.imageGallery.setPlaceholderImageUris(uris)
    }

    override fun onGalleryImageClicked(image: Product.Image) {
        viewModel.onGalleryImageClicked(image)
    }

    override fun onGalleryImageDragStarted() {
        viewModel.onGalleryImageDragStarted()
    }

    override fun onGalleryImageMoved(from: Int, to: Int) {
        viewModel.onGalleryImageMoved(from, to)
    }

    private fun showImageDetail(image: Product.Image) {
        val action = ProductImageViewerFragmentDirections.actionGlobalProductImageViewerFragment(
            isDeletingAllowed = viewModel.isImageDeletingAllowed,
            mediaId = image.id,
            remoteId = navArgs.remoteId,
            requestCode = navArgs.requestCode,
            selectedImage = null,
            showChooser = false,
            images = viewModel.images.toTypedArray()
        )
        findNavController().navigateSafely(action)
    }

    private fun showImageSourceDialog() {
        val inflater = requireActivity().layoutInflater
        val contentView = inflater.inflate(R.layout.dialog_product_image_source, binding.imageGallery, false)
            .also {
                it.findViewById<View>(R.id.textChooser)?.setOnClickListener {
                    viewModel.onShowStorageChooserButtonClicked()
                }
                it.findViewById<View>(R.id.textCamera)?.apply {
                    isVisible = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                    setOnClickListener {
                        viewModel.onShowCameraButtonClicked()
                    }
                }
                it.findViewById<View>(R.id.textWPMediaLibrary)?.setOnClickListener {
                    viewModel.onShowWPMediaPickerButtonClicked()
                }
            }

        imageSourceDialog = MaterialAlertDialogBuilder(requireActivity())
            .setView(contentView)
            .show()
    }

    override fun onExit() {
        viewModel.onNavigateBackButtonClicked()
    }

    override fun onDeviceMediaSelected(imageUris: List<Uri>, source: String) {
        if (imageUris.isNotEmpty()) {
            AnalyticsTracker.track(
                AnalyticsEvent.PRODUCT_IMAGE_ADDED,
                mapOf(AnalyticsTracker.KEY_IMAGE_SOURCE to source)
            )
            viewModel.uploadProductImages(navArgs.remoteId, imageUris)
        }
    }

    override fun onWPMediaSelected(images: List<Product.Image>) {
        AnalyticsTracker.track(
            AnalyticsEvent.PRODUCT_IMAGE_ADDED,
            mapOf(AnalyticsTracker.KEY_IMAGE_SOURCE to AnalyticsTracker.IMAGE_SOURCE_WPMEDIA)
        )
        viewModel.onMediaLibraryImagesAdded(images)
    }
}
