package com.example.virtualcamera.hooking

/**
 * Reports hooking-framework integration separately from root access.
 * Root access alone does not activate camera hooks.
 */
enum class HookEngineStatus {
    NOT_CONFIGURED,
    FRAMEWORK_NOT_ACTIVE,
    INITIALIZING,
    ACTIVE,
    ERROR
}

data class HookEngineInfo(
    val framework: String = "Not configured",
    val status: HookEngineStatus = HookEngineStatus.NOT_CONFIGURED,
    val message: String = "Camera hooks are not installed or initialized."
)
