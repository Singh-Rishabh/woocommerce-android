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
        // Remove YouTube and URL related observers and setup
        setupUploadClickListener()
    }

    private fun setupProcessObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            binding.uploadCard.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.buttonUploadVideo.isEnabled = !state.isLoading
            binding.textInputLayoutCollectionName.isEnabled = !state.isLoading
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AIProcessViewModel.Event.ShowError -> {
                    uiMessageResolver.showSnack(event.message)
                }
                is AIProcessViewModel.Event.NavigateToReview -> {
                    // No-op for now
                }
                is AIProcessViewModel.Event.NavigateToCollection -> {
                    uiMessageResolver.showSnack(event.message)
                    // Navigate to ProductListFragment filtered by the new collection
                    val bundle = android.os.Bundle().apply {
                        putString("categoryId", event.collectionId)
                        putString("categoryName", event.collectionName)
                    }
                    findNavController().navigate(R.id.action_global_productsFragment, bundle)
                    Log.d(TAG, "NavigateToCollection: collectionId=${event.collectionId}, collectionName=${event.collectionName}")
                }
            }
        }
    }

    private fun setupUploadClickListener() {
        binding.buttonUploadVideo.setOnClickListener {
            val collectionName = binding.editTextCollectionName.text?.toString()?.trim()
            if (collectionName.isNullOrEmpty()) {
                uiMessageResolver.showSnack("Please enter a collection name.")
                return@setOnClickListener
            }
            // Launch file picker for videos
            mediaPickerHelper.showMediaPicker(
                org.wordpress.android.mediapicker.api.MediaPickerSetup.DataSource.DEVICE,
                allowMultiSelect = false,
                mediaTypes = org.wordpress.android.mediapicker.model.MediaTypes.IMAGES_AND_VIDEOS
            )
        }
    }

    // MediaPickerResultHandler implementation
    override fun onDeviceMediaSelected(imageUris: List<Uri>, source: String) {
        Log.d(TAG, "onDeviceMediaSelected: imageUris=$imageUris, source=$source")
        if (imageUris.isNotEmpty()) {
            val videoUri = imageUris.first()
            Log.d(TAG, "onDeviceMediaSelected: Selected videoUri=$videoUri")
            val collectionName = binding.editTextCollectionName.text?.toString()?.trim() ?: ""
            uploadVideoToAzure(videoUri, collectionName)
        } else {
            Log.w(TAG, "onDeviceMediaSelected: No imageUris selected")
        }
    }
    override fun onWPMediaSelected(images: List<com.cataloghub.android.model.Product.Image>) {
        Log.d(TAG, "onWPMediaSelected: images=$images")
    }

    // Update uploadVideoToAzure to accept collectionName and update UI to use uploadCard
    private var uploadCall: okhttp3.Call? = null
    private fun uploadVideoToAzure(videoUri: Uri, collectionName: String) {
        Log.d(TAG, "uploadVideoToAzure: ENTRY")
        val context = requireContext().applicationContext
        val site = selectedSite.getOrNull()
        Log.d(TAG, "uploadVideoToAzure: called with videoUri=$videoUri, site=$site, collectionName=$collectionName")
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

        // Show upload card
        requireActivity().runOnUiThread {
            binding.uploadCard.visibility = View.VISIBLE
            binding.textUploadStatus.text = "Preparing upload..."
            binding.textUploadFileName.text = fileName
            binding.progressBar.progress = 0
            binding.textUploadProgress.text = "0%"
            binding.buttonCancelUpload.isEnabled = true
        }
        CoroutineScope(Dispatchers.IO).launch {
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
                    launch(Dispatchers.Main) {
                        binding.uploadCard.visibility = View.GONE
                        uiMessageResolver.showSnack("Failed to open video file.")
                    }
                    Log.d(TAG, "uploadVideoToAzure: EXIT (failed to open video file)")
                    return@launch
                }
                val tempFile = File.createTempFile("upload", ".mp4", context.cacheDir)
                FileOutputStream(tempFile).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                Log.d(TAG, "uploadVideoToAzure: Copied video to tempFile=${tempFile.absolutePath}, size=${tempFile.length()} bytes")

                // 3. Upload to Azure using OkHttp with progress
                val totalBytes = tempFile.length()
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
                                    binding.textUploadProgress.text = "$progress%"
                                    binding.textUploadStatus.text = "Uploading... ($progress%)"
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
                uploadCall = client.newCall(request)
                requireActivity().runOnUiThread {
                    binding.textUploadStatus.text = "Uploading... (0%)"
                    binding.buttonCancelUpload.setOnClickListener {
                        uploadCall?.cancel()
                        binding.textUploadStatus.text = "Upload cancelled."
                        binding.buttonCancelUpload.isEnabled = false
                    }
                }
                val response = uploadCall!!.execute()
                Log.d(TAG, "uploadVideoToAzure: Upload response code=${response.code}, message=${response.message}")
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e(TAG, "uploadVideoToAzure: Upload failed. Code=${response.code}, Message=${response.message}, Body=$errorBody")
                    launch(Dispatchers.Main) {
                        binding.uploadCard.visibility = View.GONE
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
                    binding.textUploadStatus.text = "Processing video..."
                    binding.buttonCancelUpload.isEnabled = false
                }
                // Call processAzureVideo with collectionName
                viewModel.processAzureVideoWithCollection(blobUrl, collectionName, true)
                launch(Dispatchers.Main) {
                    binding.uploadCard.visibility = View.GONE
                }
                Log.d(TAG, "uploadVideoToAzure: EXIT (success)")
            } catch (e: Exception) {
                Log.e(TAG, "uploadVideoToAzure: Exception during upload/process", e)
                launch(Dispatchers.Main) {
                    binding.uploadCard.visibility = View.GONE
                    uiMessageResolver.showSnack("Upload failed: ${e.message}")
                }
                Log.d(TAG, "uploadVideoToAzure: EXIT (exception)")
            }
        }
    }
}
