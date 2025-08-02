package com.cataloghub.android.ui.live

import android.content.Context
import android.view.SurfaceView
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.view.ConnectCheckerRtmp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RtmpStreamer(
    private val context: Context,
    private val rtmpUrl: String,
    private val surfaceView: SurfaceView
) : ConnectCheckerRtmp {
    
    private var rtmpCamera: RtmpCamera1? = null
    
    private val _connectionState = MutableStateFlow(StreamState.DISCONNECTED)
    val connectionState: StateFlow<StreamState> = _connectionState
    
    private val _streamingState = MutableStateFlow(false)
    val streamingState: StateFlow<Boolean> = _streamingState

    init {
        rtmpCamera = RtmpCamera1(surfaceView, this)
    }

    fun startStream() {
        rtmpCamera?.let { camera ->
            if (camera.prepareVideo() && camera.prepareAudio()) {
                camera.startStream(rtmpUrl)
                _streamingState.value = true
            }
        }
    }

    fun stopStream() {
        rtmpCamera?.stopStream()
        _streamingState.value = false
        _connectionState.value = StreamState.DISCONNECTED
    }

    fun startPreview() {
        rtmpCamera?.startPreview()
    }

    fun stopPreview() {
        rtmpCamera?.stopPreview()
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
        _connectionState.value = StreamState.CONNECTING
    }

    override fun onConnectionSuccessRtmp() {
        _connectionState.value = StreamState.CONNECTED
    }

    override fun onConnectionFailedRtmp(reason: String) {
        _connectionState.value = StreamState.FAILED
        _streamingState.value = false
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        // Handle bitrate changes if needed
    }

    override fun onDisconnectRtmp() {
        _connectionState.value = StreamState.DISCONNECTED
        _streamingState.value = false
    }

    override fun onAuthErrorRtmp() {
        _connectionState.value = StreamState.AUTH_ERROR
        _streamingState.value = false
    }

    override fun onAuthSuccessRtmp() {
        _connectionState.value = StreamState.CONNECTED
    }

    enum class StreamState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        FAILED,
        AUTH_ERROR
    }
}
