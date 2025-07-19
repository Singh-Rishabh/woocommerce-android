package com.cataloghub.android.ui.ai.process

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentAiProcessBinding
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.ai.AIViewModel
import com.cataloghub.android.ui.WebViewFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.net.Uri
import android.widget.Toast
import com.azure.storage.blob.BlobClientBuilder
import com.azure.storage.blob.specialized.BlockBlobClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.wordpress.android.mediapicker.model.MediaTypes
import com.cataloghub.android.mediapicker.MediaPickerHelper
import com.cataloghub.android.mediapicker.MediaPickerHelper.MediaPickerResultHandler
import java.io.InputStream
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

@AndroidEntryPoint
class AIProcessFragment : BaseFragment(R.layout.fragment_ai_process), MediaPickerResultHandler {
    companion object {
        private const val TAG = "AIProcessFragment"
        private const val DEFAULT_BUFFER_SIZE = 8192
    }

    @Inject lateinit var uiMessageResolver: UIMessageResolver
    @Inject lateinit var selectedSite: SelectedSite
    @Inject lateinit var mediaPickerHelper: MediaPickerHelper

    private val viewModel: AIProcessViewModel by viewModels()
    private val aiViewModel: AIViewModel by activityViewModels()

    private var _binding: FragmentAiProcessBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAiProcessBinding.bind(view)

        setupProcessObservers()
        setupYouTubeObservers()

        setupProcessClickListeners()
        setupYouTubeConnectButton()

        checkYouTubeStatus()
    }

    private fun checkYouTubeStatus() {
        selectedSite.get()?.let {
            aiViewModel.checkYouTubeConnectionStatus(it.url)
        } ?: Log.w(TAG, "Cannot check YouTube status: No site selected.")
    }

    private fun setupProcessObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.buttonProcess.isEnabled = !state.isLoading
            binding.switchAutoApprove.isEnabled = !state.isLoading
            binding.editTextUrl.isEnabled = !state.isLoading
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AIProcessViewModel.Event.ShowError -> {
                    uiMessageResolver.showSnack(event.message)
                }
                is AIProcessViewModel.Event.NavigateToReview -> {
                    findNavController().navigate(R.id.action_ai_to_review)
                }
            }
        }
    }

    private fun setupProcessClickListeners() {
        binding.buttonProcess.setOnClickListener {
            val url = binding.editTextUrl.text.toString()
            val autoApprove = binding.switchAutoApprove.isChecked
            viewModel.processVideo(url, autoApprove)
        }

        binding.buttonUploadVideo.setOnClickListener {
            // Launch file picker for videos
            mediaPickerHelper.showMediaPicker(
                org.wordpress.android.mediapicker.api.MediaPickerSetup.DataSource.DEVICE,
                allowMultiSelect = false,
                mediaTypes = org.wordpress.android.mediapicker.model.MediaTypes.IMAGES_AND_VIDEOS // fallback if VIDEOS not available
            )
        }
    }

    private fun setupYouTubeObservers() {
        aiViewModel.isYouTubeConnected.observe(viewLifecycleOwner) { isConnected ->
            isConnected?.let { updateYouTubeConnectionUI(it) }
        }

        aiViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.youtubeConnectButton.isEnabled = !(isLoading ?: false)
        }

        aiViewModel.event.observe(viewLifecycleOwner) { event ->
            handleAIEvent(event)
        }

        aiViewModel.authUrl.observe(viewLifecycleOwner) { url ->
            handleAuthUrl(url)
        }

        aiViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                uiMessageResolver.showSnack(errorMessage)
                aiViewModel.errorMessageShown()
            }
        }
    }

    private fun setupYouTubeConnectButton() {
        binding.youtubeConnectButton.setOnClickListener {
            val isConnected = aiViewModel.isYouTubeConnected.value ?: false
            if (isConnected) {
                try {
                    findNavController().navigate(R.id.youTubeVideosFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation to YouTube Videos failed", e)
                    uiMessageResolver.showSnack("Could not navigate to YouTube videos.")
                }
            } else {
                selectedSite.get()?.let {
                    aiViewModel.connectYouTube()
                } ?: uiMessageResolver.showSnack("Please select a store first.")
            }
        }
    }

    private fun updateYouTubeConnectionUI(isConnected: Boolean) {
        if (isConnected) {
            binding.youtubeConnectButton.text = "View Videos"
            binding.youtubeSubtitle.text = "YouTube connected"
        } else {
            binding.youtubeConnectButton.text = "Connect"
            binding.youtubeSubtitle.text = "Connect YouTube to generate products from videos"
        }
    }

    private fun handleAIEvent(event: AIViewModel.Event?) {
        when (event) {
            is AIViewModel.Event.ShowSnackbar -> {
                uiMessageResolver.showSnack(event.message)
            }
            else -> {}
        }
    }

    private fun handleAuthUrl(url: String?) {
        if (!url.isNullOrBlank()) {
            Log.d(TAG, "Received YouTube auth URL: $url")
            try {
                val directions = WebViewFragmentDirections.actionGlobalWebViewFragment(url)
                findNavController().navigate(directions)
                aiViewModel.authUrlOpened()
            } catch (e: Exception) {
                Log.e(TAG, "Navigation to WebView failed", e)
                uiMessageResolver.showSnack("Error opening authentication page.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // MediaPickerResultHandler implementation
    override fun onDeviceMediaSelected(imageUris: List<Uri>, source: String) {
        Log.d(TAG, "onDeviceMediaSelected: imageUris=$imageUris, source=$source")
        if (imageUris.isNotEmpty()) {
            val videoUri = imageUris.first()
            Log.d(TAG, "onDeviceMediaSelected: Selected videoUri=$videoUri")
            uploadVideoToAzure(videoUri)
        } else {
            Log.w(TAG, "onDeviceMediaSelected: No imageUris selected")
        }
    }
    override fun onWPMediaSelected(images: List<com.cataloghub.android.model.Product.Image>) {
        Log.d(TAG, "onWPMediaSelected: images=$images")
    }

    private fun uploadVideoToAzure(videoUri: Uri) {
        Log.d(TAG, "uploadVideoToAzure: ENTRY")
        val context = requireContext().applicationContext
        val site = selectedSite.getOrNull()
        Log.d(TAG, "uploadVideoToAzure: called with videoUri=$videoUri, site=$site")
        if (site == null) {
            uiMessageResolver.showSnack("No site selected.")
            Log.e(TAG, "uploadVideoToAzure: No site selected.")
            Log.d(TAG, "uploadVideoToAzure: EXIT (no site)")
            return
        }
        val storeName = site.getUrl()?.let { url ->
            Uri.parse(url).host ?: url.replace(Regex("https?://"), "").replace("/", "")
        } ?: "default"
        val fileName = "${UUID.randomUUID()}.mp4"
        val container = "productvideos/$storeName"
        Log.d(TAG, "uploadVideoToAzure: storeName=$storeName, fileName=$fileName, container=$container")

        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = false
        binding.progressBar.progress = 0
        Log.d(TAG, "uploadVideoToAzure: Progress bar set to visible and determinate")
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "uploadVideoToAzure: CoroutineScope(Dispatchers.IO) launched")
            try {
                Log.d(TAG, "uploadVideoToAzure: Requesting SAS upload URL from backend...")
                val uploadUrlResponse = viewModel.generateAzureUploadUrl(fileName, container)
                val uploadUrl = uploadUrlResponse.uploadUrl
                val blobName = uploadUrlResponse.blobName
                Log.d(TAG, "uploadVideoToAzure: Received uploadUrl=$uploadUrl, blobName=$blobName")

                // 2. Copy content to temp file
                Log.d(TAG, "uploadVideoToAzure: Opening inputStream for videoUri=$videoUri")
                val inputStream = context.contentResolver.openInputStream(videoUri)
                if (inputStream == null) {
                    Log.e(TAG, "uploadVideoToAzure: Failed to open video file for uri=$videoUri")
                    launch(Dispatchers.Main) {
                        Log.d(TAG, "uploadVideoToAzure: Switching to Main thread to show error UI")
                        binding.progressBar.visibility = View.GONE
                        uiMessageResolver.showSnack("Failed to open video file.")
                    }
                    Log.d(TAG, "uploadVideoToAzure: EXIT (failed to open video file)")
                    return@launch
                }
                val tempFile = File.createTempFile("upload", ".mp4", context.cacheDir)
                Log.d(TAG, "uploadVideoToAzure: Created tempFile at ${tempFile.absolutePath}")
                FileOutputStream(tempFile).use { output ->
                    Log.d(TAG, "uploadVideoToAzure: Copying inputStream to tempFile...")
                    inputStream.copyTo(output)
                }
                inputStream.close()
                Log.d(TAG, "uploadVideoToAzure: Copied video to tempFile=${tempFile.absolutePath}, size=${tempFile.length()} bytes")

                // 3. Upload to Azure using OkHttp with progress
                val totalBytes = tempFile.length()
                Log.d(TAG, "uploadVideoToAzure: Preparing progressRequestBody, totalBytes=$totalBytes")
                val progressRequestBody = object : RequestBody() {
                    override fun contentType() = "video/mp4".toMediaTypeOrNull()
                    override fun contentLength() = totalBytes
                    override fun writeTo(sink: BufferedSink) {
                        Log.d(TAG, "progressRequestBody.writeTo: ENTRY")
                        tempFile.inputStream().use { inputStream ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytesWritten = 0L
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                sink.write(buffer, 0, bytesRead)
                                bytesWritten += bytesRead
                                val progress = (bytesWritten * 100 / totalBytes).toInt()
                                requireActivity().runOnUiThread {
                                    binding.progressBar.progress = progress
                                    Log.d(TAG, "progressRequestBody.writeTo: UI progress updated to $progress% ($bytesWritten/$totalBytes bytes)")
                                }
                                if (bytesWritten % (1024 * 1024) == 0L) {
                                    Log.d(TAG, "progressRequestBody.writeTo: Uploaded $bytesWritten/$totalBytes bytes ($progress%)")
                                }
                            }
                        }
                        Log.d(TAG, "progressRequestBody.writeTo: EXIT")
                    }
                }
                val client = OkHttpClient.Builder()
                    .connectTimeout(2, java.util.concurrent.TimeUnit.MINUTES)
                    .writeTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
                    .readTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
                    .build()
                Log.d(TAG, "uploadVideoToAzure: OkHttpClient built")
                val request = Request.Builder()
                    .url(uploadUrl)
                    .put(progressRequestBody)
                    .addHeader("x-ms-blob-type", "BlockBlob")
                    .addHeader("Content-Type", "video/mp4")
                    .build()
                Log.d(TAG, "uploadVideoToAzure: OkHttp Request built, starting upload to Azure...")
                val response = client.newCall(request).execute()
                Log.d(TAG, "uploadVideoToAzure: Upload response code=${response.code}, message=${response.message}")
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e(TAG, "uploadVideoToAzure: Upload failed. Code=${response.code}, Message=${response.message}, Body=$errorBody")
                    launch(Dispatchers.Main) {
                        Log.d(TAG, "uploadVideoToAzure: Switching to Main thread to show upload error UI")
                        binding.progressBar.visibility = View.GONE
                        uiMessageResolver.showSnack("Upload failed: ${response.code}")
                    }
                    tempFile.delete()
                    Log.d(TAG, "uploadVideoToAzure: Temp file deleted after failed upload")
                    Log.d(TAG, "uploadVideoToAzure: EXIT (upload failed)")
                    return@launch
                }
                tempFile.delete()
                Log.d(TAG, "uploadVideoToAzure: Upload successful. Temp file deleted.")

                // 4. Construct blob URL
                val blobUrl = "https://cataloghubstorage.blob.core.windows.net/$container/$blobName"
                Log.d(TAG, "uploadVideoToAzure: Blob uploaded. blobUrl=$blobUrl. Now calling processAzureVideo...")

                launch(Dispatchers.Main) {
                    Log.d(TAG, "uploadVideoToAzure: Switching to Main thread to show success UI and process video")
                    binding.progressBar.visibility = View.GONE
                    uiMessageResolver.showSnack("Upload successful. Processing video...")
                    Log.d(TAG, "uploadVideoToAzure: Calling viewModel.processAzureVideo(blobUrl, true)")
                    viewModel.processAzureVideo(blobUrl, true)
                }
                Log.d(TAG, "uploadVideoToAzure: EXIT (success)")
            } catch (e: Exception) {
                Log.e(TAG, "uploadVideoToAzure: Exception during upload/process", e)
                launch(Dispatchers.Main) {
                    Log.d(TAG, "uploadVideoToAzure: Switching to Main thread to show exception UI")
                    binding.progressBar.visibility = View.GONE
                    uiMessageResolver.showSnack("Upload failed: ${e.message}")
                }
                Log.d(TAG, "uploadVideoToAzure: EXIT (exception)")
            }
        }
    }
}
