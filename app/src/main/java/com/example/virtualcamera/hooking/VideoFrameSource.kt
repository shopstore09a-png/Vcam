package com.example.virtualcamera.hooking

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.content.Context

/**
 * Decodes video frames for a controlled camera-pipeline test.
 * This class does not hook or replace the Android camera.
 */
class VideoFrameSource(
    private val context: Context,
    private val videoUri: Uri
) : AutoCloseable {

    private val retriever = MediaMetadataRetriever()
    private var prepared = false

    fun prepare(): Long {
        retriever.setDataSource(context, videoUri)
        prepared = true

        return retriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_DURATION
        )?.toLongOrNull() ?: 0L
    }

    fun frameAt(timeMs: Long): Bitmap? {
        check(prepared) { "Call prepare() first" }

        return retriever.getFrameAtTime(
            timeMs * 1000,
            MediaMetadataRetriever.OPTION_CLOSEST
        )
    }

    override fun close() {
        prepared = false
        retriever.release()
    }
}
