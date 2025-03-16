package com.cataloghub.android.ui.coupons.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.model.Coupon
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.coupons.edit.CouponRestrictionsViewModel.OpenAllowedEmailsEditor
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.ui.products.categories.selector.ProductCategorySelectorFragment
import com.cataloghub.android.ui.products.selector.ProductSelectorFragment
import com.cataloghub.android.ui.products.selector.ProductSelectorViewModel.SelectedItem
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CouponRestrictionsFragment : BaseFragment(), BackPressListener {
    companion object {
        const val RESTRICTIONS_RESULT = "restrictions-result"
    }

    private val viewModel: CouponRestrictionsViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Visible(
            navigationIcon = R.drawable.ic_gridicons_cross_24dp
        )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WooThemeWithBackground {
                    CouponRestrictionsScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        handleResults()
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is EditCouponNavigationTarget -> EditCouponNavigator.navigate(this, event)
                is OpenAllowedEmailsEditor -> {
                    findNavController().navigateSafely(
                        CouponRestrictionsFragmentDirections.actionCouponRestrictionsFragmentToEmailRestrictionFragment(
                            event.allowedEmails
                        )
                    )
                }
                is ExitWithResult<*> -> {
                    navigateBackWithResult(RESTRICTIONS_RESULT, event.data as Coupon.CouponRestrictions)
                }
                is Exit -> findNavController().navigateUp()
            }
        }
    }

    private fun handleResults() {
        handleResult<String>(EmailRestrictionFragment.ALLOWED_EMAILS) {
            viewModel.onAllowedEmailsUpdated(it)
        }
        handleResult<Collection<SelectedItem>>(ProductSelectorFragment.PRODUCT_SELECTOR_RESULT) {
            viewModel.onExcludedProductChanged(it)
        }
        handleResult<Set<Long>>(ProductCategorySelectorFragment.PRODUCT_CATEGORY_SELECTOR_RESULT) {
            viewModel.onExcludedProductCategoriesChanged(it)
        }
    }

    override fun getFragmentTitle() = getString(R.string.coupon_edit_usage_restrictions)

    override fun onRequestAllowBackPress(): Boolean {
        viewModel.onBackPressed()
        return false
    }
}
