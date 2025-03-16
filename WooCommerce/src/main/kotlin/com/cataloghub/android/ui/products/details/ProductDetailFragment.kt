package com.cataloghub.android.ui.products.details

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.automattic.android.tracks.crashlogging.CrashLogging
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.cataloghub.android.AppUrls
import com.cataloghub.android.NavGraphMainDirections
import com.cataloghub.android.R
import com.cataloghub.android.RequestCodes
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentProductDetailBinding
import com.cataloghub.android.extensions.fastStripHtml
import com.cataloghub.android.extensions.findNavController
import com.cataloghub.android.extensions.handleNotice
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.hide
import com.cataloghub.android.extensions.isTwoPanesShouldBeUsed
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.extensions.parcelable
import com.cataloghub.android.extensions.show
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.model.Product
import com.cataloghub.android.model.Product.Image
import com.cataloghub.android.model.UiString
import com.cataloghub.android.ui.aztec.AztecEditorFragment
import com.cataloghub.android.ui.aztec.AztecEditorFragment.Companion.ARG_AZTEC_EDITOR_TEXT
import com.cataloghub.android.ui.aztec.AztecEditorFragment.Companion.ARG_AZTEC_TITLE_FROM_AI_DESCRIPTION
import com.cataloghub.android.ui.blaze.BlazeUrlsHelper.BlazeFlowSource
import com.cataloghub.android.ui.blaze.creation.BlazeCampaignCreationDispatcher
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.dialog.WooDialog
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.MainNavigationRouter
import com.cataloghub.android.ui.products.BaseProductEditorFragment
import com.cataloghub.android.ui.products.BaseProductFragment
import com.cataloghub.android.ui.products.ProductInventoryViewModel.InventoryData
import com.cataloghub.android.ui.products.ProductNavigationTarget.ViewProductDetailBottomSheet
import com.cataloghub.android.ui.products.ProductsCommunicationViewModel
import com.cataloghub.android.ui.products.adapters.ProductPropertyCardsAdapter
import com.cataloghub.android.ui.products.ai.description.AIProductDescriptionBottomSheetFragment.Companion.KEY_AI_GENERATED_DESCRIPTION_RESULT
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.HideImageUploadErrorSnackbar
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.OpenProductDetails
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ProductDetailViewState.AuxiliaryState.Error
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ProductDetailViewState.AuxiliaryState.Loading
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ProductDetailViewState.AuxiliaryState.None
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ProductUpdated
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.RefreshMenu
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ShowAIProductDescriptionBottomSheet
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ShowAiProductCreationSurveyBottomSheet
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ShowBlazeCreationScreen
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ShowDuplicateProductError
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ShowDuplicateProductInProgress
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.ShowLinkedProductPromoBanner
import com.cataloghub.android.ui.products.details.ProductDetailViewModel.TrashProduct
import com.cataloghub.android.ui.products.grouped.GroupedProductListType
import com.cataloghub.android.ui.products.models.ProductPropertyCard
import com.cataloghub.android.ui.products.models.QuantityRules
import com.cataloghub.android.ui.products.price.ProductPricingViewModel.PricingData
import com.cataloghub.android.ui.products.reviews.ProductReviewsFragment
import com.cataloghub.android.ui.products.shipping.ProductShippingViewModel.ShippingData
import com.cataloghub.android.ui.products.subscriptions.ProductSubscriptionExpirationFragment.Companion.KEY_SUBSCRIPTION_EXPIRATION_RESULT
import com.cataloghub.android.ui.products.subscriptions.ProductSubscriptionFreeTrialFragment.Companion.KEY_SUBSCRIPTION_FREE_TRIAL_RESULT
import com.cataloghub.android.ui.products.subscriptions.ProductSubscriptionFreeTrialViewModel.FreeTrialState
import com.cataloghub.android.ui.products.typesbottomsheet.ProductTypesBottomSheetFragment
import com.cataloghub.android.ui.products.typesbottomsheet.ProductTypesBottomSheetViewModel.ProductTypesBottomSheetUiItem
import com.cataloghub.android.ui.products.variations.VariationListFragment
import com.cataloghub.android.ui.products.variations.VariationListViewModel.VariationListData
import com.cataloghub.android.ui.promobanner.PromoBanner
import com.cataloghub.android.ui.promobanner.PromoBannerType
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.util.UiHelpers.getTextOfUiString
import com.cataloghub.android.util.WooAnimUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.LaunchUrlInChromeTab
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowUiStringSnackbar
import com.cataloghub.android.widgets.CustomProgressDialog
import com.cataloghub.android.widgets.SkeletonView
import com.cataloghub.android.widgets.WCProductImageGalleryView.OnGalleryImageInteractionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class ProductDetailFragment :
    BaseProductFragment(R.layout.fragment_product_detail),
    OnGalleryImageInteractionListener {
    companion object {
        private const val LIST_STATE_KEY = "list_state"
    }

    private var productName = ""
        set(value) {
            field = value
            toolbarHelper.updateTitle(value)
        }

    private var productId: Long = ProductDetailViewModel.DEFAULT_ADD_NEW_PRODUCT_ID

    @Inject
    lateinit var blazeCampaignCreationDispatcher: BlazeCampaignCreationDispatcher

    @Inject
    lateinit var toolbarHelper: ProductDetailsToolbarHelper

    private val skeletonView = SkeletonView()

    private var progressDialog: CustomProgressDialog? = null
    private var layoutManager: LayoutManager? = null
    private var imageUploadErrorsSnackbar: Snackbar? = null

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    @Inject lateinit var crashLogging: CrashLogging

    private val productsCommunicationViewModel: ProductsCommunicationViewModel by activityViewModels()

    private val navArgs: ProductDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transitionDuration = resources.getInteger(R.integer.default_fragment_transition).toLong()
        val backgroundColor = ContextCompat.getColor(requireContext(), R.color.default_window_background)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.snack_root
            duration = transitionDuration
            scrimColor = Color.TRANSPARENT
            startContainerColor = backgroundColor
            endContainerColor = backgroundColor
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        blazeCampaignCreationDispatcher.attachFragment(this, BlazeFlowSource.PRODUCT_DETAIL_PROMOTE_BUTTON)

        _binding = FragmentProductDetailBinding.bind(view)

        toolbarHelper.onViewCreated(this, viewModel, binding)

        ViewCompat.setTransitionName(
            binding.root,
            getString(R.string.product_card_detail_transition_name)
        )
        initializeViews(savedInstanceState)
        initializeViewModel()
        handleOnePaneToTwoPaneConversion()
    }

    /**
     * When product details open in one pane mode and the screen size changes to two pane mode,
     * we need to handle the back stack and navigate to the product list fragment
     * to show selected product in the right pane.
     */
    private fun handleOnePaneToTwoPaneConversion() {
        val isScreenLargerThanCompact = requireContext().isTwoPanesShouldBeUsed
        val isProductListFragmentUpInBackStack =
            findNavController().previousBackStackEntry?.destination?.id == R.id.products
        if (isScreenLargerThanCompact && isProductListFragmentUpInBackStack) {
            val mode = navArgs.mode
            when (mode) {
                is Mode.ShowProduct -> {
                    findNavController().popBackStack()
                    productsCommunicationViewModel.pushEvent(
                        ProductsCommunicationViewModel.CommunicationEvent.ProductSelected(mode.remoteProductId)
                    )
                }

                is Mode.Loading, is Mode.Empty -> {
                    findNavController().popBackStack()
                }

                is Mode.AddNewProduct -> {
                }
            }
        }
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

    override fun onStop() {
        super.onStop()
        WooDialog.onCleared()
    }

    private fun initializeViews(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        this.layoutManager = layoutManager

        savedInstanceState?.parcelable<Parcelable>(LIST_STATE_KEY)?.let {
            layoutManager.onRestoreInstanceState(it)
        }
        binding.cardsRecyclerView.layoutManager = layoutManager
        binding.cardsRecyclerView.itemAnimator = null

        binding.openUploadScreenButton.setOnClickListener {
            viewModel.openUploadScreen()
        }
    }

    private fun initializeViewModel() {
        setupObservers(viewModel)
        setupResultHandlers(viewModel)
    }

    @Suppress("LongMethod")
    private fun setupResultHandlers(viewModel: ProductDetailViewModel) {
        handleResult<ProductTypesBottomSheetUiItem>(ProductTypesBottomSheetFragment.KEY_PRODUCT_TYPE_RESULT) {
            viewModel.onProductTypeChanged(productType = it.type, isVirtual = it.isVirtual)
        }
        handleResult<List<Long>>(GroupedProductListType.GROUPED.resultKey) {
            viewModel.updateProductDraft(groupedProductIds = it)
        }
        handleResult<PricingData>(BaseProductEditorFragment.KEY_PRICING_DIALOG_RESULT) {
            viewModel.updateProductDraft(
                regularPrice = it.regularPrice,
                salePrice = it.salePrice,
                saleStartDate = it.saleStartDate,
                saleEndDate = it.saleEndDate,
                isSaleScheduled = it.isSaleScheduled,
                taxClass = it.taxClass,
                taxStatus = it.taxStatus
            )
            if (it.isSubscription) {
                viewModel.updateProductSubscription(
                    price = it.regularPrice,
                    period = it.subscriptionPeriod,
                    periodInterval = it.subscriptionInterval,
                    signUpFee = it.subscriptionSignUpFee,
                )
            }
        }
        handleResult<InventoryData>(BaseProductEditorFragment.KEY_INVENTORY_DIALOG_RESULT) {
            viewModel.updateProductDraft(
                sku = it.sku,
                globalUniqueId = it.globalUniqueId,
                soldIndividually = it.isSoldIndividually,
                stockStatus = it.stockStatus,
                stockQuantity = it.stockQuantity,
                backorderStatus = it.backorderStatus,
                manageStock = it.isStockManaged
            )
        }
        handleResult<ShippingData>(BaseProductEditorFragment.KEY_SHIPPING_DIALOG_RESULT) {
            viewModel.updateProductDraft(
                weight = it.weight,
                length = it.length,
                width = it.width,
                height = it.height,
                shippingClass = it.shippingClassSlug,
                shippingClassId = it.shippingClassId
            )
            if (it.subscriptionShippingData != null) {
                viewModel.updateProductSubscription(
                    oneTimeShipping = it.subscriptionShippingData.oneTimeShipping
                )
            }
        }
        handleResult<QuantityRules>(BaseProductEditorFragment.KEY_QUANTITY_RULES_DIALOG_RESULT) {
            viewModel.updateProductDraft(
                minAllowedQuantity = it.min,
                maxAllowedQuantity = it.max,
                groupOfQuantity = it.groupOf
            )
        }
        handleResult<List<Image>>(BaseProductEditorFragment.KEY_IMAGES_DIALOG_RESULT) {
            viewModel.updateProductDraft(images = it)
        }

        handleResult<Bundle>(AztecEditorFragment.AZTEC_EDITOR_RESULT) { result ->
            if (!result.getBoolean(AztecEditorFragment.ARG_AZTEC_HAS_CHANGES)) return@handleResult
            when (result.getInt(AztecEditorFragment.ARG_AZTEC_REQUEST_CODE)) {
                RequestCodes.AZTEC_EDITOR_PRODUCT_DESCRIPTION -> {
                    viewModel.updateProductDraft(description = result.getString(ARG_AZTEC_EDITOR_TEXT))
                }

                RequestCodes.AZTEC_EDITOR_PRODUCT_SHORT_DESCRIPTION -> {
                    viewModel.updateProductDraft(shortDescription = result.getString(ARG_AZTEC_EDITOR_TEXT))
                }
            }

            if (result.containsKey(ARG_AZTEC_TITLE_FROM_AI_DESCRIPTION)) {
                viewModel.updateProductDraft(
                    title = result.getString(ARG_AZTEC_TITLE_FROM_AI_DESCRIPTION)
                )
            }
        }

        handleResult<VariationListData>(VariationListFragment.KEY_VARIATION_LIST_RESULT) { data ->
            data.currentVariationAmount?.let { viewModel.onVariationAmountReceived(it) }
        }

        handleNotice(ProductReviewsFragment.PRODUCT_REVIEWS_MODIFIED) {
            viewModel.refreshProduct()
        }

        handleResult<Pair<String, String>>(KEY_AI_GENERATED_DESCRIPTION_RESULT) { resultPair ->
            viewModel.updateProductDraft(description = resultPair.first, title = resultPair.second)
        }

        handleResult<Int>(KEY_SUBSCRIPTION_EXPIRATION_RESULT) { newExpiration ->
            viewModel.onSubscriptionExpirationChanged(newExpiration)
        }

        handleResult<FreeTrialState>(KEY_SUBSCRIPTION_FREE_TRIAL_RESULT) { freeTrial ->
            viewModel.updateProductSubscription(trialLength = freeTrial.length, trialPeriod = freeTrial.period)
        }
    }

    private fun setupObservers(viewModel: ProductDetailViewModel) {
        viewModel.productDetailViewStateData.observe(viewLifecycleOwner) { old, new ->
            new.productDraft?.takeIfNotEqualTo(old?.productDraft) { showProductDetails(it, new.areImagesAvailable) }
            new.auxiliaryState.takeIfNotEqualTo(old?.auxiliaryState) { showAuxiliaryState(it) }
            new.isProgressDialogShown?.takeIfNotEqualTo(old?.isProgressDialogShown) {
                if (it) {
                    showProgressDialog(R.string.product_save_dialog_title, R.string.product_update_dialog_message)
                } else {
                    hideProgressDialog()
                }
            }
            new.uploadingImageUris?.takeIfNotEqualTo(old?.uploadingImageUris) {
                binding.imageGallery.setPlaceholderImageUris(it)
            }
            new.showBottomSheetButton?.takeIfNotEqualTo(old?.showBottomSheetButton) { isVisible ->
                binding.productDetailAddMoreContainer.isVisible = isVisible
            }
            new.isUploadingDownloadableFile?.takeIfNotEqualTo(old?.isUploadingDownloadableFile) {
                if (it) {
                    showProgressDialog(
                        title = R.string.product_downloadable_files_upload_dialog_title,
                        message = R.string.product_downloadable_files_upload_dialog_message
                    )
                } else {
                    hideProgressDialog()
                }
            }
            new.hasUploadErrors?.takeIfNotEqualTo(old?.hasUploadErrors) { hasErrors ->
                binding.openUploadScreenButton.visibility = if (hasErrors) View.VISIBLE else View.GONE
            }
        }

        viewModel.productDetailCards.observe(viewLifecycleOwner) {
            showProductCards(it)
        }

        viewModel.hasChanges.observe(viewLifecycleOwner) { hasChanges ->
            productsCommunicationViewModel.pushEvent(
                ProductsCommunicationViewModel.CommunicationEvent.ProductChanges(hasChanges)
            )
        }

        observeEvents(viewModel)
    }

    @Suppress("ComplexMethod")
    private fun observeEvents(viewModel: ProductDetailViewModel) {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is LaunchUrlInChromeTab -> ChromeCustomTabUtils.launchUrl(requireContext(), event.url)
                is Event.LaunchUrlInAuthenticatedWebView -> findNavController(R.id.nav_host_fragment_main)
                    .navigateSafely(NavGraphMainDirections.actionGlobalAuthenticatedWebViewFragment(event.url))
                is RefreshMenu -> toolbarHelper.setupToolbar()

                is TrashProduct -> {
                    if (findNavController().previousBackStackEntry != null) {
                        findNavController().popBackStack()
                    }
                    productsCommunicationViewModel.pushEvent(
                        ProductsCommunicationViewModel.CommunicationEvent.ProductTrashed(event.productId)
                    )
                }

                is ShowUiStringSnackbar -> displayProductImageUploadErrorSnackBar(event.message)

                is HideImageUploadErrorSnackbar -> imageUploadErrorsSnackbar?.dismiss()
                is ShowLinkedProductPromoBanner -> showLinkedProductPromoBanner()
                is OpenProductDetails -> openProductDetails(event.productRemoteId)
                is ShowDuplicateProductError -> showDuplicateProductError()
                is ShowBlazeCreationScreen -> openBlazeCreationFlow(event.productId)
                is ShowDuplicateProductInProgress -> showProgressDialog(
                    R.string.product_duplicate_progress_title,
                    R.string.product_duplicate_progress_body
                )

                is ShowAIProductDescriptionBottomSheet -> showAIProductDescriptionBottomSheet(
                    event.productTitle,
                    event.productDescription
                )

                is ShowAiProductCreationSurveyBottomSheet -> openAIProductCreationSurveyBottomSheet()
                is ProductUpdated -> productsCommunicationViewModel.pushEvent(
                    ProductsCommunicationViewModel.CommunicationEvent.ProductUpdated
                )

                is ProductDetailViewModel.ShowUpdateProductError -> showUpdateProductError(event.message)
                else -> event.isHandled = false
            }
        }
    }

    private fun openAIProductCreationSurveyBottomSheet() {
        findNavController().navigateSafely(
            ProductDetailFragmentDirections.actionProductDetailFragmentToAIProductCreationSurveyBottomSheet()
        )
    }

    private fun showAIProductDescriptionBottomSheet(title: String, description: String?) {
        findNavController().navigateSafely(
            ProductDetailFragmentDirections.actionProductDetailFragmentToAIProductDescriptionBottomSheetFragment(
                title,
                description?.fastStripHtml()
            )
        )
    }

    private fun openBlazeCreationFlow(productId: Long) {
        lifecycleScope.launch {
            blazeCampaignCreationDispatcher.startCampaignCreation(
                source = BlazeFlowSource.PRODUCT_DETAIL_PROMOTE_BUTTON,
                productId = productId
            )
        }
    }

    private fun showDuplicateProductError() {
        hideProgressDialog()
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.error_generic)
            .setMessage(R.string.product_duplicate_error)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showUpdateProductError(message: String) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.product_detail_update_product_error)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun openProductDetails(productRemoteId: Long) {
        hideProgressDialog()
        (activity as? MainNavigationRouter)?.showProductDetail(
            remoteProductId = productRemoteId,
            enableTrash = true,
            popUpToProductList = true
        )
    }

    /**
     *  Triggered when the view modal updates or creates an order that doesn't already have linked products
     */
    private fun showProductDetails(product: Product, isImageUploadAvailable: Boolean) {
        binding.productErrorStateContainer.isVisible = false
        binding.productDetailRoot.isVisible = true
        binding.productDetailAddMoreContainer.isVisible = true

        productName = updateProductNameFromDetails(product)
        productId = product.remoteId

        if (isImageUploadAvailable) {
            if (product.images.isEmpty() && !viewModel.isUploadingImages()) {
                binding.imageGallery.hide()
                startAddImageContainer()
            } else {
                binding.addImageContainer.hide()
                binding.imageGallery.show()
                binding.imageGallery.showProductImages(product.images, this)
            }
            binding.imagesUnavailableNotice.hide()
        } else {
            binding.imageGallery.hide()
            binding.addImageContainer.hide()
            binding.imagesUnavailableNotice.show()
            binding.imagesUnavailableNotice.setOnClickListener {
                ChromeCustomTabUtils.launchUrl(requireContext(), AppUrls.WORDPRESS_PRIVACY_SETTINGS)
            }
        }

        binding.productDetailAddMoreButton.setOnClickListener {
            // TODO: add tracking events here
            viewModel.onEditProductCardClicked(
                ViewProductDetailBottomSheet(product.productType)
            )
        }

        toolbarHelper.setupToolbar()
    }

    private fun updateProductNameFromDetails(product: Product): String {
        return if (viewModel.isProductUnderCreation && product.name.isEmpty()) {
            getString(R.string.product_add_tool_bar_title)
        } else {
            product.name.fastStripHtml()
        }
    }

    private fun displayProductImageUploadErrorSnackBar(uiString: UiString) {
        if (imageUploadErrorsSnackbar == null) {
            imageUploadErrorsSnackbar = uiMessageResolver.getUiStringSnack(message = uiString)
        } else {
            imageUploadErrorsSnackbar?.setText(getTextOfUiString(requireContext(), uiString))
        }
        imageUploadErrorsSnackbar?.show()
    }

    private fun startAddImageContainer() {
        binding.addImageContainer.show()
        binding.addImageContainer.setOnClickListener {
            AnalyticsTracker.track(AnalyticsEvent.PRODUCT_DETAIL_ADD_IMAGE_TAPPED)
            viewModel.onAddImageButtonClicked()
        }
    }

    private fun showAuxiliaryState(auxiliaryState: ProductDetailViewModel.ProductDetailViewState.AuxiliaryState) {
        if (auxiliaryState == Loading) {
            skeletonView.show(binding.appBarLayout, R.layout.skeleton_product_detail, delayed = true)
        } else {
            skeletonView.hide()
            when (auxiliaryState) {
                Loading, None -> {
                    binding.productErrorStateContainer.isVisible = false
                }

                is Error -> {
                    binding.productErrorStateContainer.isVisible = true
                    binding.productDetailRoot.isVisible = false
                    binding.productDetailAddMoreContainer.isVisible = false

                    binding.productDetailsErrorImage.contentDescription = getString(auxiliaryState.message)
                    binding.productDetailsErrorText.text = getString(auxiliaryState.message)
                }
            }
        }
    }

    private fun showProgressDialog(@StringRes title: Int, @StringRes message: Int) {
        hideProgressDialog()
        progressDialog = CustomProgressDialog.show(
            getString(title),
            getString(message)
        ).also { it.show(parentFragmentManager, CustomProgressDialog.TAG) }
        progressDialog?.isCancelable = false
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun showProductCards(cards: List<ProductPropertyCard>) {
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

    private fun showLinkedProductPromoBanner() {
        if (binding.promoComposableContainer.isVisible.not()) {
            if (binding.promoComposable.hasComposition.not()) {
                binding.promoComposable.setContent {
                    WooThemeWithBackground {
                        PromoBanner(
                            bannerType = PromoBannerType.LINKED_PRODUCTS,
                            onCtaClick = {
                                WooAnimUtils.scaleOut(binding.promoComposableContainer)
                                viewModel.onLinkedProductPromoClicked()
                            },
                            onDismissClick = {
                                WooAnimUtils.scaleOut(binding.promoComposableContainer)
                                viewModel.onLinkedProductPromoDismissed()
                            }
                        )
                    }
                }
            }
            WooAnimUtils.scaleIn(binding.promoComposableContainer, WooAnimUtils.Duration.MEDIUM)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        layoutManager?.let {
            outState.putParcelable(LIST_STATE_KEY, it.onSaveInstanceState())
        }
    }

    override fun onRequestAllowBackPress(): Boolean {
        return viewModel.onBackButtonClickedProductDetail()
    }

    override fun onGalleryImageClicked(image: Image) {
        viewModel.onImageClicked()
    }

    override fun onGalleryAddImageClicked() {
        AnalyticsTracker.track(AnalyticsEvent.PRODUCT_DETAIL_ADD_IMAGE_TAPPED)
        viewModel.onAddImageButtonClicked()
    }

    override fun getFragmentTitle(): String = productName

    @Parcelize
    sealed class Mode : Parcelable {
        @Parcelize
        data object Loading : Mode()

        @Parcelize
        data object Empty : Mode()

        @Parcelize
        data class ShowProduct(
            val remoteProductId: Long,
            val afterGeneratedWithAi: Boolean = false,
        ) : Mode()

        @Parcelize
        data object AddNewProduct : Mode()
    }
}
