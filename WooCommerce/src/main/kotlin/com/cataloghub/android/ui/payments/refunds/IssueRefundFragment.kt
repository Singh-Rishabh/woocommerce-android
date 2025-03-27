@file:Suppress("DEPRECATION")

package com.cataloghub.android.ui.payments.refunds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentIssueRefundBinding
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.payments.refunds.IssueRefundViewModel.IssueRefundEvent.ShowRefundSummary
import com.cataloghub.android.viewmodel.fixedHiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IssueRefundFragment : BaseFragment() {
    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private var _binding: FragmentIssueRefundBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IssueRefundViewModel by fixedHiltNavGraphViewModels(R.id.nav_graph_refunds)

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentIssueRefundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager
            .beginTransaction()
            .replace(R.id.issueRefund_frame, RefundByItemsFragment())
            .commit()

        setupToolbar()

        setupObservers(viewModel)
    }

    private fun setupToolbar() {
        binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireActivity(),
            R.drawable.ic_back_24dp
        )
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers(viewModel: IssueRefundViewModel) {
        viewModel.commonStateLiveData.observe(viewLifecycleOwner) { old, new ->
            new.screenTitle?.takeIfNotEqualTo(old?.screenTitle) {
                binding.toolbar.title = it
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowRefundSummary -> {
                    val action = IssueRefundFragmentDirections.actionIssueRefundFragmentToRefundSummaryFragment()
                    findNavController().navigateSafely(action)
                }

                else -> event.isHandled = false
            }
        }
    }
}
