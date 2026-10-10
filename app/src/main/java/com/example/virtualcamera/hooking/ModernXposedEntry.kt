package com.example.virtualcamera.hooking

import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * Vector/libxposed API 102 entry point.
 *
 * This milestone installs non-invasive diagnostics around Camera1 and Camera2
 * open calls. It does not yet replace camera frames.
 */
class ModernXposedEntry : XposedModule() {
    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "MODULE_LOADED process=${param.processName} api=${getApiVersion()} framework=${getFrameworkName()} version=${getFrameworkVersion()}")
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        log(Log.INFO, TAG, "PACKAGE_LOADED package=${param.packageName} first=${param.isFirstPackage}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        val packageName = param.packageName
        val classLoader = param.classLoader
        log(Log.INFO, TAG, "PACKAGE_READY package=$packageName classLoader=${classLoader.javaClass.name}")
        installCamera1Diagnostics(classLoader, packageName)
        installCamera2Diagnostics(classLoader, packageName)
    }

    private fun installCamera1Diagnostics(classLoader: ClassLoader, packageName: String) {
        try {
            val cameraClass = Class.forName("android.hardware.Camera", false, classLoader)
            val methods = cameraClass.declaredMethods.filter { it.name == "open" }
            if (methods.isEmpty()) {
                log(Log.WARN, TAG, "CAMERA1_NO_OPEN_METHOD package=$packageName")
                return
            }
            methods.forEach { method ->
                hook(method).setId("vc-camera1-${method.toGenericString()}").intercept { chain ->
                    log(Log.INFO, TAG, "CAMERA1_OPEN package=$packageName method=${chain.executable.name} args=${chain.args.size}")
                    chain.proceed()
                }
            }
            log(Log.INFO, TAG, "CAMERA1_HOOKS_INSTALLED package=$packageName count=${methods.size}")
        } catch (t: Throwable) {
            log(Log.ERROR, TAG, "CAMERA1_HOOK_INSTALL_FAILED package=$packageName error=${t.javaClass.simpleName}: ${t.message}")
        }
    }

    private fun installCamera2Diagnostics(classLoader: ClassLoader, packageName: String) {
        try {
            val managerClass = Class.forName("android.hardware.camera2.CameraManager", false, classLoader)
            val methods = managerClass.declaredMethods.filter { it.name == "openCamera" }
            if (methods.isEmpty()) {
                log(Log.WARN, TAG, "CAMERA2_NO_OPEN_CAMERA_METHOD package=$packageName")
                return
            }
            methods.forEach { method ->
                hook(method).setId("vc-camera2-${method.toGenericString()}").intercept { chain ->
                    log(Log.INFO, TAG, "CAMERA2_OPEN package=$packageName method=${chain.executable.name} args=${chain.args.size}")
                    chain.proceed()
                }
            }
            log(Log.INFO, TAG, "CAMERA2_HOOKS_INSTALLED package=$packageName count=${methods.size}")
        } catch (t: Throwable) {
            log(Log.ERROR, TAG, "CAMERA2_HOOK_INSTALL_FAILED package=$packageName error=${t.javaClass.simpleName}: ${t.message}")
        }
    }

    private companion object {
        const val TAG = "VirtualCamera"
    }
}