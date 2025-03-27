package com.cataloghub.android.ui.ai.youtube

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentYoutubeVideosBinding
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.ai.AINetworkLogger
import com.cataloghub.android.ui.base.UIMessageResolver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class YouTubeVideosFragment : Fragment(R.layout.fragment_youtube_videos) {
    
    @Inject lateinit var selectedSite: SelectedSite
    @Inject lateinit var uiMessageResolver: UIMessageResolver
    
    private val viewModel: YouTubeVideosViewModel by viewModels()
    private var _binding: FragmentYoutubeVideosBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var videosAdapter: YouTubeVideosAdapter
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentYoutubeVideosBinding.bind(view)
        
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        setupObservers()
        
        // Load videos on start
        viewModel.loadVideos(selectedSite.get().url)
    }
    
    private fun setupRecyclerView() {
        videosAdapter = YouTubeVideosAdapter { video ->
            // Navigate to video detail screen
            val action = YouTubeVideosFragmentDirections.actionYoutubeVideosToVideoDetail(video.videoId)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewVideos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videosAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.sortButton.setOnClickListener { view ->
            showSortMenu(view)
        }
    }
    
    private fun setupSearchListener() {
        binding.searchEditText.doAfterTextChanged { text ->
            if (text.isNullOrBlank()) {
                // If search is cleared, load all videos
                viewModel.loadVideos(selectedSite.get().url)
            } else if (text.length > 2) {
                // Search after typing at least 3 characters
                viewModel.searchVideos(selectedSite.get().url, text.toString())
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            videosAdapter.submitList(videos)
            
            // Show empty view if no videos
            binding.emptyView.visibility = if (videos.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewVideos.visibility = if (videos.isEmpty()) View.GONE else View.VISIBLE
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                uiMessageResolver.showSnack(it)
                viewModel.errorMessageShown()
            }
        }
    }
    
    private fun showSortMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_youtube_sort, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort_date_desc -> {
                    viewModel.setSortOrder("date", "desc")
                    true
                }
                R.id.sort_date_asc -> {
                    viewModel.setSortOrder("date", "asc")
                    true
                }
                R.id.sort_views_desc -> {
                    viewModel.setSortOrder("viewCount", "desc")
                    true
                }
                R.id.sort_title_asc -> {
                    viewModel.setSortOrder("title", "asc")
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 