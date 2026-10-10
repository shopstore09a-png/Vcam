package com.example.virtualcamera.hooking

import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class ModernXposedEntry : XposedModule() {
    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "MODULE_LOADED process=${param.processName} api=${getApiVersion()} framework=${getFrameworkName()} version=${getFrameworkVersion()}")
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        log(Log.INFO, TAG, "PACKAGE_LOADED package=${param.packageName} first=${param.isFirstPackage}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        log(Log.INFO, TAG, "PACKAGE_READY package=${param.packageName} classLoader=${param.classLoader.javaClass.name}")
    }

    private companion object {
        const val TAG = "VirtualCamera"
    }
}
