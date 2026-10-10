package com.example.virtualcamera

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object RootChecker {
    fun check(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "id")
            )

            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return false
            }

            val output = BufferedReader(
                InputStreamReader(process.inputStream)
            ).use { it.readText() }

            process.exitValue() == 0 &&
                output.contains("uid=0") &&
                output.contains("gid=0")
        } catch (_: Exception) {
            false
        }
    }
}
