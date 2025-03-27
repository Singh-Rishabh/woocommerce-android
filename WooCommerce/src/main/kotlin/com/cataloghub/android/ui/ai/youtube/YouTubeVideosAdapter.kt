package com.cataloghub.android.ui.ai.youtube

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cataloghub.android.databinding.ItemYoutubeVideoBinding
import com.cataloghub.android.ui.ai.YouTubeVideo
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class YouTubeVideosAdapter(
    private val onVideoClick: (YouTubeVideo) -> Unit
) : ListAdapter<YouTubeVideo, YouTubeVideosAdapter.VideoViewHolder>(VideoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemYoutubeVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoViewHolder(
        private val binding: ItemYoutubeVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVideoClick(getItem(position))
                }
            }
        }

        fun bind(video: YouTubeVideo) {
            binding.apply {
                titleText.text = video.title
                channelText.text = video.channelTitle
                
                // Format stats text (views and publish date)
                val viewsText = video.viewCount?.let { formatViewCount(it) } ?: "No views"
                val dateText = formatPublishDate(video.publishedAt)
                statsText.text = "$viewsText • $dateText"
                
                // Set duration
                durationText.text = video.formattedDuration ?: ""
                
                // Load thumbnail
                val thumbnailUrl = video.thumbnails["medium"]?.url
                    ?: video.thumbnails["default"]?.url
                    ?: ""
                
                Glide.with(thumbnailImage)
                    .load(thumbnailUrl)
                    .centerCrop()
                    .into(thumbnailImage)
            }
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
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val publishDate = format.parse(publishDateStr) ?: return "Unknown date"
                val now = Date()
                
                val diffInMillis = now.time - publishDate.time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                
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
    }

    private class VideoDiffCallback : DiffUtil.ItemCallback<YouTubeVideo>() {
        override fun areItemsTheSame(oldItem: YouTubeVideo, newItem: YouTubeVideo): Boolean {
            return oldItem.videoId == newItem.videoId
        }

        override fun areContentsTheSame(oldItem: YouTubeVideo, newItem: YouTubeVideo): Boolean {
            return oldItem == newItem
        }
    }
} 