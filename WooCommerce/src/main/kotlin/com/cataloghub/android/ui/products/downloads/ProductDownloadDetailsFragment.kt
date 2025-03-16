package com.cataloghub.android.ui.products.downloads

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTracker.Companion.DownloadableFileAction
import com.cataloghub.android.analytics.AnalyticsTracker.Companion.KEY_DOWNLOADABLE_FILE_ACTION
import com.cataloghub.android.databinding.FragmentProductDownloadDetailsBinding
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.ui.products.details.ProductDetailViewModel
import com.cataloghub.android.ui.products.downloads.ProductDownloadDetailsViewModel.ProductDownloadDetailsEvent.AddFileAndExitEvent
import com.cataloghub.android.ui.products.downloads.ProductDownloadDetailsViewModel.ProductDownloadDetailsEvent.DeleteFileEvent
import com.cataloghub.android.ui.products.downloads.ProductDownloadDetailsViewModel.ProductDownloadDetailsEvent.UpdateFileAndExitEvent
import com.cataloghub.android.util.setupTabletSecondPaneToolbar
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialog
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.fixedHiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.util.ActivityUtils
import javax.inject.Inject

@AndroidEntryPoint
class ProductDownloadDetailsFragment :
    BaseFragment(R.layout.fragment_product_download_details), BackPressListener {
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    private val viewModel: ProductDownloadDetailsViewModel by viewModels()
    private val parentViewModel: ProductDetailViewModel by fixedHiltNavGraphViewModels(R.id.nav_graph_products)
    private val navArgs by navArgs<ProductDownloadDetailsFragmentArgs>()
    private lateinit var doneOrUpdateMenuItem: MenuItem

    private var _binding: FragmentProductDownloadDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProductDownloadDetailsBinding.bind(view)

        setupObservers(viewModel)

        setupTabletSecondPaneToolbar(
            title = viewModel.screenTitle,
            onMenuItemSelected = ::onMenuItemSelected,
            onCreateMenu = { toolbar ->
                toolbar.setNavigationOnClickListener {
                    if (viewModel.onBackButtonClicked()) {
                        findNavController().navigateUp()
                    }
                }
                onCreateMenu(toolbar)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onCreateMenu(toolbar: Toolbar) {
        toolbar.menu.clear()
        if (navArgs.isEditing) {
            toolbar.inflateMenu(R.menu.menu_product_download_details)
        } else {
            toolbar.inflateMenu(R.menu.menu_done)
        }

        doneOrUpdateMenuItem = toolbar.menu.findItem(R.id.menu_done)
        doneOrUpdateMenuItem.isVisible = viewModel.showDoneButton
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_done -> {
                viewModel.onDoneOrUpdateClicked()

                val action = if (navArgs.isEditing) DownloadableFileAction.UPDATED else DownloadableFileAction.ADDED
                AnalyticsTracker.track(
                    AnalyticsEvent.PRODUCTS_DOWNLOADABLE_FILE,
                    mapOf(KEY_DOWNLOADABLE_FILE_ACTION to action.value)
                )

                true
            }
            R.id.menu_delete -> {
                viewModel.onDeleteButtonClicked()
                true
            }
            else -> false
        }
    }

    private fun setupObservers(viewModel: ProductDownloadDetailsViewModel) {
        viewModel.productDownloadDetailsViewStateData.observe(viewLifecycleOwner) { old, new ->
            new.fileDraft.url.takeIfNotEqualTo(binding.productDownloadUrl.text) {
                binding.productDownloadUrl.text = it
            }
            new.fileDraft.name.takeIfNotEqualTo(binding.productDownloadName.text) {
                binding.productDownloadName.text = it
            }
            new.showDoneButton.takeIfNotEqualTo(old?.showDoneButton) {
                showDoneMenuItem(it)
            }
            if (new.urlErrorMessage != old?.urlErrorMessage || new.nameErrorMessage != old?.nameErrorMessage) {
                updateErrorMessages(new.urlErrorMessage, new.nameErrorMessage)
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is Exit -> {
                    ActivityUtils.hideKeyboard(requireActivity())
                    findNavController().navigateUp()
                }
                is ShowDialog -> event.showDialog()
                is UpdateFileAndExitEvent -> {
                    ActivityUtils.hideKeyboard(requireActivity())
                    parentViewModel.updateDownloadableFileInDraft(event.updatedFile)
                    findNavController().navigateUp()
                }
                is AddFileAndExitEvent -> {
                    ActivityUtils.hideKeyboard(requireActivity())
                    parentViewModel.addDownloadableFileToDraft(event.file)
                    findNavController().navigateUp()
                }
                is DeleteFileEvent -> {
                    parentViewModel.deleteDownloadableFile(event.file)

                    AnalyticsTracker.track(
                        AnalyticsEvent.PRODUCTS_DOWNLOADABLE_FILE,
                        mapOf(KEY_DOWNLOADABLE_FILE_ACTION to DownloadableFileAction.DELETED.value)
                    )

                    findNavController().navigateUp()
                }
            }
        }

        initListeners()
    }

    private fun initListeners() {
        binding.productDownloadUrl.setOnTextChangedListener {
            viewModel.onFileUrlChanged(it.toString())
        }
        binding.productDownloadName.setOnTextChangedListener {
            viewModel.onFileNameChanged(it.toString())
        }
    }

    private fun updateErrorMessages(urlErrorMessage: Int?, nameErrorMessage: Int?) {
        binding.productDownloadUrl.error = if (urlErrorMessage != null) getString(urlErrorMessage) else null
        binding.productDownloadName.error = if (nameErrorMessage != null) getString(nameErrorMessage) else null
        enableDoneButton(urlErrorMessage == null && nameErrorMessage == null)
    }

    private fun enableDoneButton(enable: Boolean) {
        if (::doneOrUpdateMenuItem.isInitialized) {
            doneOrUpdateMenuItem.isEnabled = enable
        }
    }

    private fun showDoneMenuItem(show: Boolean) {
        if (::doneOrUpdateMenuItem.isInitialized) {
            doneOrUpdateMenuItem.isVisible = show
        }
    }

    override fun onRequestAllowBackPress(): Boolean {
        return viewModel.onBackButtonClicked()
    }
}
