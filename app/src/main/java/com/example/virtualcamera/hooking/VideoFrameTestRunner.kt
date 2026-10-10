package com.example.virtualcamera.hooking

import android.content.Context
import android.net.Uri

data class VideoFrameTestReport(
    val durationMs: Long,
    val framesDecoded: Int,
    val failedFrames: Int,
    val frameSizes: List<String>,
    val message: String
)

/**
 * Tests video frame decoding at several timestamps.
 * Does not intercept or replace camera output.
 */
object VideoFrameTestRunner {

    fun run(context: Context, uri: Uri): VideoFrameTestReport {
        var duration = 0L
        var decoded = 0
        var failed = 0
        val sizes = mutableListOf<String>()

        try {
            VideoFrameSource(context, uri).use { source ->
                duration = source.prepare()

                if (duration <= 0L) {
                    return VideoFrameTestReport(
                        duration, 0, 0, emptyList(),
                        "Could not read video duration"
                    )
                }

                val timestamps = listOf(
                    0L,
                    duration / 4,
                    duration / 2,
                    (duration * 3) / 4
                ).distinct()

                for (timestamp in timestamps) {
                    val bitmap = try {
                        source.frameAt(timestamp)
                    } catch (_: Exception) {
                        null
                    }

                    if (bitmap == null) {
                        failed++
                    } else {
                        decoded++
                        sizes.add("${bitmap.width}x${bitmap.height}")
                        bitmap.recycle()
                    }
                }
            }

            val message = when {
                decoded == 0 -> "No frames decoded"
                failed == 0 -> "All sampled frames decoded"
                else -> "Some sampled frames failed"
            }

            return VideoFrameTestReport(
                duration, decoded, failed, sizes, message
            )
        } catch (e: Exception) {
            return VideoFrameTestReport(
                duration, decoded, failed, sizes,
                "Test error: ${e.javaClass.simpleName}"
            )
        }
    }
}
