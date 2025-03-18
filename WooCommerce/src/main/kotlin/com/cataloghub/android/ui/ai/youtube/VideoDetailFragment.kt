package com.cataloghub.android.ui.ai.youtube

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentVideoDetailBinding
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.ai.AINetworkLogger
import com.cataloghub.android.ui.base.UIMessageResolver
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VideoDetailFragment : Fragment(R.layout.fragment_video_detail) {
    
    @Inject lateinit var selectedSite: SelectedSite
    @Inject lateinit var uiMessageResolver: UIMessageResolver
    
    private val viewModel: VideoDetailViewModel by viewModels()
    private var _binding: FragmentVideoDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: VideoDetailFragmentArgs by navArgs()
    private lateinit var pagerAdapter: ProductsPagerAdapter
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVideoDetailBinding.bind(view)
        
        setupObservers()
        setupClickListeners()
        
        // Load video details
        viewModel.loadVideoDetails(selectedSite.get().url, args.videoId)
        
        // Check if products already exist for this video
        viewModel.checkExistingProducts(selectedSite.get().url, args.videoId)
    }
    
    private fun setupObservers() {
        viewModel.videoDetails.observe(viewLifecycleOwner) { video ->
            binding.videoTitle.text = video.title
            
            // Format stats text (views and publish date)
            val viewsText = video.viewCount?.let { formatViewCount(it) } ?: "No views"
            val dateText = formatPublishDate(video.publishedAt)
            binding.videoStats.text = "$viewsText • $dateText"
            
            // Load YouTube video in WebView
            setupYouTubePlayer(video.videoId)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.processButton.isEnabled = !isLoading
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                uiMessageResolver.showSnack(it)
                viewModel.errorMessageShown()
            }
        }
        
        viewModel.productsExist.observe(viewLifecycleOwner) { exist ->
            if (exist) {
                // Products already exist, setup tabs and load products
                setupViewPager()
                binding.processButton.text = getString(R.string.reprocess_video)
                binding.emptyView.visibility = View.GONE
            } else {
                // No products yet, show empty view
                binding.emptyView.visibility = View.VISIBLE
                binding.processButton.text = getString(R.string.process_video)
            }
        }
        
        viewModel.processingComplete.observe(viewLifecycleOwner) { complete ->
            if (complete) {
                // Processing completed, setup tabs and load products
                setupViewPager()
                binding.emptyView.visibility = View.GONE
                viewModel.processingCompleteHandled()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.processButton.setOnClickListener {
            viewModel.processVideo(selectedSite.get().url, args.videoId)
        }
    }
    
    private fun setupYouTubePlayer(videoId: String) {
        binding.youtubeWebView.apply {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.videoProgressBar.visibility = View.GONE
                }
            }
            
            val embedUrl = "https://www.youtube.com/embed/$videoId"
            loadUrl(embedUrl)
        }
    }
    
    private fun setupViewPager() {
        pagerAdapter = ProductsPagerAdapter(this)
        
        binding.viewPager.adapter = pagerAdapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.pending)
                1 -> getString(R.string.approved)
                2 -> getString(R.string.rejected)
                else -> ""
            }
        }.attach()
    }
    
    private fun formatViewCount(viewCount: Int): String {
        return when {
            viewCount < 1000 -> "$viewCount views"
            viewCount < 1000000 -> "${viewCount / 1000}K views"
            else -> "${viewCount / 1000000}M views"
        }
    }
    
    private fun formatPublishDate(publishDateStr: String): String {
        try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            val publishDate = format.parse(publishDateStr) ?: return "Unknown date"
            val now = java.util.Date()
            
            val diffInMillis = now.time - publishDate.time
            val diffInDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis)
            
            return when {
                diffInDays < 1 -> "Today"
                diffInDays < 2 -> "Yesterday"
                diffInDays < 7 -> "$diffInDays days ago"
                diffInDays < 30 -> "${diffInDays / 7} weeks ago"
                diffInDays < 365 -> "${diffInDays / 30} months ago"
                else -> "${diffInDays / 365} years ago"
            }
        } catch (e: Exception) {
            return "Unknown date"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        binding.youtubeWebView.loadUrl("about:blank")
        _binding = null
    }
} 