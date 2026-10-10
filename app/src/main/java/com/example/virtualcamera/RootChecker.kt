package com.example.virtualcamera

import java.io.BufferedReader
import java.io.InputStreamReader

object RootChecker {
    fun check(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val output = BufferedReader(
                InputStreamReader(process.inputStream)
            ).use { it.readText() }

            val exitCode = process.waitFor()
            exitCode == 0 && output.contains("uid=0")
        } catch (_: Exception) {
            false
        }
    }
}
