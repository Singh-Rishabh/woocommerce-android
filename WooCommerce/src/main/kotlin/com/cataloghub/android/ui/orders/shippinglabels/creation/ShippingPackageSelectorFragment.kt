package com.cataloghub.android.ui.orders.shippinglabels.creation

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentShippingPackagesSelectorBinding
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.orders.shippinglabels.creation.ShippingPackageSelectorViewModel.ShowCreatePackageScreen
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShippingPackageSelectorFragment : BaseFragment(R.layout.fragment_shipping_packages_selector) {
    companion object {
        const val SELECTED_PACKAGE_RESULT = "selected-package"
    }

    @Inject lateinit var uiMessageResolver: UIMessageResolver
    val viewModel: ShippingPackageSelectorViewModel by viewModels()

    private val packagesAdapter by lazy {
        ShippingPackagesAdapter(
            viewModel.dimensionUnit,
            viewModel::onPackageSelected
        )
    }

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentShippingPackagesSelectorBinding.bind(view)
        setupToolbar(binding)
        with(binding.packagesList) {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = packagesAdapter
        }

        binding.packagesCreateNewButton.setOnClickListener {
            viewModel.onCreateNewPackageButtonClicked()
        }
        setupObservers(binding)
    }

    private fun setupToolbar(binding: FragmentShippingPackagesSelectorBinding) {
        binding.toolbar.title = getString(R.string.shipping_label_package_selector_title)
        binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireActivity(),
            R.drawable.ic_back_24dp
        )
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers(binding: FragmentShippingPackagesSelectorBinding) {
        viewModel.viewStateData.observe(viewLifecycleOwner) { old, new ->
            new.packagesList.takeIfNotEqualTo(old?.packagesList) { list ->
                packagesAdapter.updatePackages(list)
            }
            new.isLoading.takeIfNotEqualTo(old?.isLoading) { isLoading ->
                binding.loadingProgress.isVisible = isLoading
                binding.packagesList.isVisible = !isLoading
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ExitWithResult<*> -> navigateBackWithResult(SELECTED_PACKAGE_RESULT, event.data)
                is Exit -> findNavController().navigateUp()
                is ShowCreatePackageScreen -> {
                    val action = ShippingPackageSelectorFragmentDirections
                        .actionShippingPackageSelectorFragmentToShippingLabelCreatePackageFragment(
                            event.position
                        )
                    findNavController().navigateSafely(action)
                }
                else -> event.isHandled = false
            }
        }
    }

    override fun getFragmentTitle() = getString(R.string.shipping_label_package_selector_title)
}
