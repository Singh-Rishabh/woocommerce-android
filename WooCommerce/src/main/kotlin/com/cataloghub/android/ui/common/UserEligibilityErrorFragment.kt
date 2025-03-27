package com.cataloghub.android.ui.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.AppUrls
import com.cataloghub.android.R
import com.cataloghub.android.R.layout
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentUserEligibilityErrorBinding
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.model.User
import com.cataloghub.android.support.help.HelpActivity
import com.cataloghub.android.support.help.HelpOrigin.USER_ELIGIBILITY_ERROR
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.login.LoginActivity
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Logout
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.widgets.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.login.LoginMode
import org.wordpress.android.util.DisplayUtils
import javax.inject.Inject

@AndroidEntryPoint
class UserEligibilityErrorFragment : BaseFragment(layout.fragment_user_eligibility_error), BackPressListener {
    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    private val viewModel: UserEligibilityErrorViewModel by viewModels()

    private var _binding: FragmentUserEligibilityErrorBinding? = null
    private val binding get() = _binding!!

    private var progressDialog: CustomProgressDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserEligibilityErrorBinding.bind(view)

        setupMenu()
        setupView()
        setupObservers(viewModel)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(org.wordpress.android.login.R.menu.menu_login, menu)
                }

                override fun onMenuItemSelected(item: MenuItem): Boolean {
                    if (item.itemId == org.wordpress.android.login.R.id.help) {
                        startActivity(
                            HelpActivity.createIntent(
                                requireActivity(),
                                USER_ELIGIBILITY_ERROR,
                                arrayListOf(binding.textUserRoles.text.toString())
                            )
                        )
                        return true
                    }

                    return false
                }
            },
            viewLifecycleOwner,
            State.RESUMED
        )
    }

    private fun setupView() {
        // hide images in landscape unless this device is a tablet
        val isLandscape = DisplayUtils.isLandscape(context)
        val hideImages = isLandscape &&
            !DisplayUtils.isTablet(context) &&
            !DisplayUtils.isXLargeTablet(context)
        binding.imageView2.isVisible = !hideImages

        val btnBinding = binding.epilogueButtonBar
        with(btnBinding.buttonPrimary) {
            visibility = View.VISIBLE
            text = getString(R.string.retry)
            setOnClickListener { viewModel.onRetryButtonClicked() }
        }

        with(btnBinding.buttonSecondary) {
            visibility = View.VISIBLE
            text = getString(R.string.login_try_another_account)
            setOnClickListener { viewModel.onLogoutButtonClicked() }
        }

        binding.btnSecondaryAction.setOnClickListener {
            ChromeCustomTabUtils.launchUrl(requireContext(), AppUrls.WOOCOMMERCE_USER_ROLES)
        }
    }

    private fun setupObservers(viewModel: UserEligibilityErrorViewModel) {
        viewModel.viewStateData.observe(viewLifecycleOwner) { old, new ->
            new.user?.takeIfNotEqualTo(old?.user) { showView(it) }
            new.isProgressDialogShown?.takeIfNotEqualTo(old?.isProgressDialogShown) { showProgressDialog(it) }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSnackbar -> {
                    uiMessageResolver.showSnack(event.message)
                }
                is Exit -> {
                    findNavController().navigateUp()
                }
                is Logout -> {
                    requireActivity().apply {
                        setResult(Activity.RESULT_CANCELED)
                        val intent = Intent(activity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        LoginMode.WOO_LOGIN_MODE.putInto(intent)
                        startActivity(intent)
                        finish()
                    }
                }
                else -> event.isHandled = false
            }
        }
    }

    private fun showView(user: User) {
        binding.textDisplayname.text = user.getUserNameForDisplay()
        binding.textUserRoles.text = user.roles.joinToString(", ") { it.value }
    }

    private fun showProgressDialog(show: Boolean) {
        if (show) {
            hideProgressDialog()
            progressDialog = CustomProgressDialog.show(
                getString(R.string.user_access_verifying),
                getString(R.string.web_view_loading_message)
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

    override fun getFragmentTitle() = ""

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestAllowBackPress(): Boolean {
        activity?.finish()
        return false
    }
}
