package com.cataloghub.android.ui.live

import android.Manifest
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.cataloghub.android.R
import com.cataloghub.android.model.Product
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveStreamScreen(
    viewModel: LiveStreamViewModel = hiltViewModel()
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedProducts by viewModel.selectedProducts.collectAsState()

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
        if (!recordAudioPermissionState.isGranted) {
            recordAudioPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.isGranted && recordAudioPermissionState.isGranted) {
        var surfaceView by remember { mutableStateOf<SurfaceView?>(null) }
        var rtmpStreamer by remember { mutableStateOf<RtmpStreamer?>(null) }

        LaunchedEffect(surfaceView) {
            surfaceView?.let { surface ->
                val streamer = RtmpStreamer(
                    context = context,
                    rtmpUrl = "rtmp://a.rtmp.youtube.com/live2/${viewModel.streamKey}",
                    surfaceView = surface
                )
                rtmpStreamer = streamer
                viewModel.setRtmpStreamer(streamer)
                viewModel.onPreviewStarted()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Camera Preview
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        surfaceView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Product showcase overlay
            if (selectedProducts.isNotEmpty()) {
                ProductShowcase(
                    products = selectedProducts,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // Stream controls
            StreamControls(
                isStreaming = uiState.isStreaming,
                onStreamToggle = {
                    if (uiState.isStreaming) {
                        viewModel.onStreamStopped()
                    } else {
                        rtmpStreamer?.let { viewModel.onStreamStarted(it) }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )

            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    backgroundColor = MaterialTheme.colors.error,
                    elevation = 4.dp
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colors.onError,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    } else {
        PermissionRequired(
            cameraGranted = cameraPermissionState.isGranted,
            audioGranted = recordAudioPermissionState.isGranted,
            onRequestPermissions = {
                if (!cameraPermissionState.isGranted) {
                    cameraPermissionState.launchPermissionRequest()
                }
                if (!recordAudioPermissionState.isGranted) {
                    recordAudioPermissionState.launchPermissionRequest()
                }
            }
        )
    }
}

@Composable
fun ProductShowcase(
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 8.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.featured_products),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductCard(product = product)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(120.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(product.firstImageUrl),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = product.name,
                style = MaterialTheme.typography.caption,
                maxLines = 2,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = product.priceWithCurrency ?: "",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StreamControls(
    isStreaming: Boolean,
    onStreamToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onStreamToggle,
        modifier = modifier,
        backgroundColor = if (isStreaming) Color.Red else MaterialTheme.colors.primary
    ) {
        Icon(
            imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (isStreaming) "Stop streaming" else "Start streaming",
            tint = Color.White
        )
    }
}

@Composable
fun PermissionRequired(
    cameraGranted: Boolean,
    audioGranted: Boolean,
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.permissions_required_for_streaming),
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (!cameraGranted) {
            Text(
                text = stringResource(R.string.camera_permission_required),
                style = MaterialTheme.typography.body1
            )
        }
        
        if (!audioGranted) {
            Text(
                text = stringResource(R.string.microphone_permission_required),
                style = MaterialTheme.typography.body1
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRequestPermissions) {
            Text(text = stringResource(R.string.grant_permissions))
        }
    }
}