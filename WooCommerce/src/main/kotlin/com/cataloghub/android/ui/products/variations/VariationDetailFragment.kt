package com.cataloghub.android.ui.products.variations

import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsEvent.PRODUCT_VARIATION_UPDATE_BUTTON_TAPPED
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentVariationDetailBinding
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.hide
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.parcelable
import com.cataloghub.android.extensions.show
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.model.Product.Image
import com.cataloghub.android.model.ProductVariation
import com.cataloghub.android.model.VariantOption
import com.cataloghub.android.ui.aztec.AztecEditorFragment
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.dialog.WooDialogFragment
import com.cataloghub.android.ui.dialog.WooDialogFragment.DialogInteractionListener
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.ui.products.BaseProductEditorFragment
import com.cataloghub.android.ui.products.ProductInventoryViewModel.InventoryData
import com.cataloghub.android.ui.products.adapters.ProductPropertyCardsAdapter
import com.cataloghub.android.ui.products.models.ProductPropertyCard
import com.cataloghub.android.ui.products.models.QuantityRules
import com.cataloghub.android.ui.products.price.ProductPricingViewModel.PricingData
import com.cataloghub.android.ui.products.shipping.ProductShippingViewModel.ShippingData
import com.cataloghub.android.ui.products.subscriptions.ProductSubscriptionExpirationFragment.Companion.KEY_SUBSCRIPTION_EXPIRATION_RESULT
import com.cataloghub.android.ui.products.subscriptions.ProductSubscriptionFreeTrialFragment.Companion.KEY_SUBSCRIPTION_FREE_TRIAL_RESULT
import com.cataloghub.android.ui.products.subscriptions.ProductSubscriptionFreeTrialViewModel.FreeTrialState
import com.cataloghub.android.ui.products.variations.VariationDetailViewModel.HideImageUploadErrorSnackbar
import com.cataloghub.android.ui.products.variations.attributes.edit.EditVariationAttributesFragment.Companion.KEY_VARIATION_ATTRIBUTES_RESULT
import com.cataloghub.android.util.Optional
import com.cataloghub.android.util.setupTabletSecondPaneToolbar
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowActionSnackbar
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialog
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialogFragment
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.widgets.CustomProgressDialog
import com.cataloghub.android.widgets.SkeletonView
import com.cataloghub.android.widgets.WCProductImageGalleryView.OnGalleryImageInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.util.ActivityUtils
import javax.inject.Inject

@AndroidEntryPoint
class VariationDetailFragment :
    BaseFragment(R.layout.fragment_variation_detail),
    BackPressListener,
    OnGalleryImageInteractionListener,
    DialogInteractionListener {
    companion object {
        private const val LIST_STATE_KEY = "list_state"
        const val KEY_VARIATION_DETAILS_RESULT = "key_variation_details_result"
    }

    override val activityAppBarStatus: AppBarStatus = AppBarStatus.Hidden

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    @Inject
    lateinit var navigator: VariationNavigator

    private var doneOrUpdateMenuItem: MenuItem? = null

    private var variationName = ""
        set(value) {
            field = value
            _binding?.toolbar?.title = value
        }

    private val skeletonView = SkeletonView()
    private var progressDialog: CustomProgressDialog? = null
    private var layoutManager: LayoutManager? = null
    private var imageUploadErrorsSnackbar: Snackbar? = null

    private val viewModel: VariationDetailViewModel by viewModels()

    private var _binding: FragmentVariationDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentVariationDetailBinding.bind(view)

        reattachDialogInteractionListener()

        initializeViews(savedInstanceState)
        initializeViewModel()
        setupTabletSecondPaneToolbar(
            title = variationName,
            onMenuItemSelected = ::onMenuItemSelected,
            onCreateMenu = { toolbar ->
                toolbar.setNavigationOnClickListener {
                    viewModel.onExit()
                }
                onCreateMenu(toolbar)
            }
        )
    }

    private fun reattachDialogInteractionListener() {
        val dialogFragment = parentFragmentManager.findFragmentByTag(WooDialogFragment.TAG) as? WooDialogFragment
        dialogFragment?.setDialogInteractionListener(this)
    }

    override fun onDestroyView() {
        skeletonView.hide()
        imageUploadErrorsSnackbar?.dismiss()
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onPause() {
        super.onPause()
        progressDialog?.dismiss()
    }

    private fun onCreateMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_variation_detail_fragment)
        doneOrUpdateMenuItem = toolbar.menu.findItem(R.id.menu_done)
        onPrepareMenu()
    }

    private fun onPrepareMenu() {
        doneOrUpdateMenuItem?.isVisible = viewModel.variationViewStateData.liveData.value?.isDoneButtonVisible ?: false
        doneOrUpdateMenuItem?.isEnabled = viewModel.variationViewStateData.liveData.value?.isDoneButtonEnabled ?: true
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_done -> {
                AnalyticsTracker.track(PRODUCT_VARIATION_UPDATE_BUTTON_TAPPED)
                ActivityUtils.hideKeyboard(activity)
                viewModel.onUpdateButtonClicked()
                true
            }

            R.id.menu_delete -> {
                viewModel.onDeleteVariationClicked()
                true
            }

            else -> false
        }
    }

    private fun initializeViews(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        this.layoutManager = layoutManager

        savedInstanceState?.parcelable<Parcelable>(LIST_STATE_KEY)?.let {
            layoutManager.onRestoreInstanceState(it)
        }
        binding.cardsRecyclerView.layoutManager = layoutManager
        binding.cardsRecyclerView.itemAnimator = null
    }

    private fun initializeViewModel() {
        setupObservers(viewModel)
        setupResultHandlers(viewModel)
    }

    private fun setupResultHandlers(viewModel: VariationDetailViewModel) {
        handleResult<PricingData>(BaseProductEditorFragment.KEY_PRICING_DIALOG_RESULT) {
            viewModel.onVariationChanged(
                regularPrice = it.regularPrice,
                salePrice = it.salePrice,
                isSaleScheduled = it.isSaleScheduled,
                saleStartDate = it.saleStartDate,
                saleEndDate = it.saleEndDate
            )

            if (it.isSubscription) {
                viewModel.onVariationSubscriptionChanged(
                    price = it.regularPrice,
                    period = it.subscriptionPeriod,
                    periodInterval = it.subscriptionInterval,
                    signUpFee = it.subscriptionSignUpFee,
                )
            }
        }
        handleResult<InventoryData>(BaseProductEditorFragment.KEY_INVENTORY_DIALOG_RESULT) {
            viewModel.onVariationChanged(
                sku = it.sku,
                globalUniqueId = it.globalUniqueId,
                stockStatus = it.stockStatus,
                stockQuantity = it.stockQuantity,
                backorderStatus = it.backorderStatus,
                isStockManaged = it.isStockManaged
            )
        }
        handleResult<ShippingData>(BaseProductEditorFragment.KEY_SHIPPING_DIALOG_RESULT) {
            viewModel.onVariationChanged(
                weight = it.weight,
                length = it.length,
                width = it.width,
                height = it.height,
                shippingClass = it.shippingClassSlug,
                shippingClassId = it.shippingClassId
            )
        }
        handleResult<List<Image>>(BaseProductEditorFragment.KEY_IMAGES_DIALOG_RESULT) { updatedImage ->
            viewModel.onVariationChanged(image = Optional(updatedImage.firstOrNull()))
        }
        handleResult<Bundle>(AztecEditorFragment.AZTEC_EDITOR_RESULT) { result ->
            if (result.getBoolean(AztecEditorFragment.ARG_AZTEC_HAS_CHANGES)) {
                viewModel.onVariationChanged(
                    description = result.getString(AztecEditorFragment.ARG_AZTEC_EDITOR_TEXT)
                )
            }
        }
        handleResult<Array<VariantOption>>(KEY_VARIATION_ATTRIBUTES_RESULT) {
            viewModel.onVariationChanged(attributes = it)
        }
        handleResult<Int>(KEY_SUBSCRIPTION_EXPIRATION_RESULT) { newExpiration ->
            viewModel.onSubscriptionExpirationChanged(newExpiration)
        }
        handleResult<FreeTrialState>(KEY_SUBSCRIPTION_FREE_TRIAL_RESULT) { freeTrial ->
            viewModel.onVariationSubscriptionChanged(trialLength = freeTrial.length, trialPeriod = freeTrial.period)
        }

        setupQuantityRulesHandleResult()
    }

    private fun setupQuantityRulesHandleResult() {
        handleResult<QuantityRules>(BaseProductEditorFragment.KEY_QUANTITY_RULES_DIALOG_RESULT) {
            viewModel.onVariationChanged(
                minAllowedQuantity = it.min,
                maxAllowedQuantity = it.max,
                groupOfQuantity = it.groupOf
            )
        }
    }

    @Suppress("LongMethod")
    private fun setupObservers(viewModel: VariationDetailViewModel) {
        viewModel.variationViewStateData.observe(viewLifecycleOwner) { old, new ->
            new.variation.takeIfNotEqualTo(old?.variation) { newVariation ->
                newVariation?.let {
                    variationName = it.getName(new.parentProduct)
                    showVariationDetails(it)
                }
            }
            new.parentProduct.takeIfNotEqualTo(old?.parentProduct) { product ->
                new.variation?.let {
                    variationName = it.getName(product)
                }
            }
            new.uploadingImageUri.takeIfNotEqualTo(old?.uploadingImageUri) {
                if (it != null) {
                    binding.imageGallery.clearImages()
                    binding.imageGallery.setPlaceholderImageUris(listOf(it))
                } else {
                    binding.imageGallery.clearPlaceholders()
                }
            }
            new.isSkeletonShown?.takeIfNotEqualTo(old?.isSkeletonShown) { showSkeleton(it) }
            new.isProgressDialogShown?.takeIfNotEqualTo(old?.isProgressDialogShown) {
                showProgressDialog(it, R.string.product_save_dialog_title)
            }
            new.isDoneButtonVisible?.takeIfNotEqualTo(old?.isDoneButtonVisible) {
                doneOrUpdateMenuItem?.isVisible = it
            }
            new.isDoneButtonEnabled?.takeIfNotEqualTo(old?.isDoneButtonEnabled) {
                doneOrUpdateMenuItem?.isEnabled = it
            }
            new.isDeleteDialogShown?.takeIfNotEqualTo(old?.isDeleteDialogShown) {
                showProgressDialog(it, R.string.product_delete_dialog_title)
            }
        }

        viewModel.variationDetailCards.distinctUntilChanged().observe(viewLifecycleOwner) {
            showVariationCards(it)
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ShowActionSnackbar -> displayProductImageUploadErrorSnackBar(
                    event.message,
                    event.actionText,
                    event.action
                )

                is HideImageUploadErrorSnackbar -> imageUploadErrorsSnackbar?.dismiss()
                is VariationNavigationTarget -> {
                    navigator.navigate(this, event)
                }

                is ExitWithResult<*> -> navigateBackWithResult(KEY_VARIATION_DETAILS_RESULT, event.data)
                is ShowDialog -> event.showDialog()
                is ShowDialogFragment -> event.showIn(parentFragmentManager, this)
                is VariationDetailViewModel.ShowUpdateVariationError -> showUpdateVariationError(event.message)
                is Exit -> findNavController().navigateUp()
                else -> event.isHandled = false
            }
        }
    }

    override fun onPositiveButtonClicked() {
        viewModel.onDeleteVariationConfirmed()
    }

    override fun onNegativeButtonClicked() {
        viewModel.onDeleteVariationCancelled()
    }

    override fun onNeutralButtonClicked() {
        // no-op
    }

    private fun showVariationDetails(variation: ProductVariation) {
        if (variation.image == null && !viewModel.isUploadingImages()) {
            binding.imageGallery.hide()
            binding.addImageContainer.show()
            binding.addImageContainer.setOnClickListener {
                AnalyticsTracker.track(AnalyticsEvent.PRODUCT_DETAIL_ADD_IMAGE_TAPPED)
                viewModel.onAddImageButtonClicked()
            }
        } else {
            binding.addImageContainer.hide()
            binding.imageGallery.show()
            variation.image?.let {
                binding.imageGallery.showProductImage(it, this)
            }
        }
    }

    private fun showUpdateVariationError(message: String) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.variation_detail_update_variation_error)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onGalleryImageClicked(image: Image) {
        viewModel.onImageClicked(image)
    }

    private fun showSkeleton(show: Boolean) {
        if (show) {
            skeletonView.show(binding.appBarLayout, R.layout.skeleton_variation_detail, delayed = true)
        } else {
            skeletonView.hide()
        }
    }

    private fun showProgressDialog(show: Boolean, @StringRes title: Int) {
        if (show) {
            hideProgressDialog()
            progressDialog = CustomProgressDialog.show(
                getString(title),
                getString(R.string.product_update_dialog_message)
            ).also { it.show(parentFragmentManager, CustomProgressDialog.TAG) }
            progressDialog?.isCancelable = false
        } else {
            hideProgressDialog()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun showVariationCards(cards: List<ProductPropertyCard>) {
        val adapter: ProductPropertyCardsAdapter
        if (binding.cardsRecyclerView.adapter == null) {
            adapter = ProductPropertyCardsAdapter()
            binding.cardsRecyclerView.adapter = adapter
        } else {
            adapter = binding.cardsRecyclerView.adapter as ProductPropertyCardsAdapter
        }

        val recyclerViewState = binding.cardsRecyclerView.layoutManager?.onSaveInstanceState()
        adapter.update(cards)
        binding.cardsRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun displayProductImageUploadErrorSnackBar(
        message: String,
        actionText: String,
        actionListener: View.OnClickListener
    ) {
        if (imageUploadErrorsSnackbar == null) {
            imageUploadErrorsSnackbar = uiMessageResolver.getIndefiniteActionSnack(
                message = message,
                actionText = actionText,
                actionListener = actionListener
            )
        } else {
            imageUploadErrorsSnackbar?.setText(message)
        }
        imageUploadErrorsSnackbar?.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        layoutManager?.let {
            outState.putParcelable(LIST_STATE_KEY, it.onSaveInstanceState())
        }
    }

    override fun onRequestAllowBackPress(): Boolean {
        viewModel.onExit()
        return false
    }
}
