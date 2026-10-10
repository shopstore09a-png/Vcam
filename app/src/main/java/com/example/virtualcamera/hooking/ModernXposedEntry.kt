package com.example.virtualcamera.hooking

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import android.util.Log

/**
 * Modern Xposed API 102 entry point.
 * This confirms framework loading only; camera hooks are not active yet.
 */
class ModernXposedEntry : XposedModule() {

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(
            Log.INFO,
            "VirtualCamera",
            "Module loaded in process: ${param.processName}"
        )
    }
}
