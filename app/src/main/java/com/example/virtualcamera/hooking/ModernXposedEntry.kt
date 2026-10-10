package com.example.virtualcamera.hooking

import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.lang.reflect.Method
import java.util.Collections

/**
 * VirtualCamera hook entry point for libxposed API 102.
 *
 * Current stage:
 * 1. Confirm module loading.
 * 2. Detect target package process loading.
 * 3. Install Camera1 and Camera2 diagnostic hooks.
 * 4. Log camera open calls while preserving the original behavior.
 *
 * This stage does not replace camera frames.
 */
class ModernXposedEntry : XposedModule() {

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(
            Log.INFO,
            TAG,
            "MODULE_LOADED process=${param.processName} " +
                "api=${getApiVersion()} " +
                "framework=${getFrameworkName()} " +
                "version=${getFrameworkVersion()}"
        )
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        log(
            Log.INFO,
            TAG,
            "PACKAGE_LOADED package=${param.packageName} " +
                "first=${param.isFirstPackage}"
        )
    }

    override fun onPackageReady(param: PackageReadyParam) {
        val packageName = param.packageName
        val classLoader = param.classLoader

        log(
            Log.INFO,
            TAG,
            "PACKAGE_READY package=$packageName " +
                "classLoader=${classLoader.javaClass.name}"
        )

        installCamera1Hooks(classLoader, packageName)
        installCamera2Hooks(classLoader, packageName)
    }

    private fun installCamera1Hooks(
        classLoader: ClassLoader,
        packageName: String
    ) {
        try {
            val cameraClass = Class.forName(
                "android.hardware.Camera",
                false,
                classLoader
            )

            val methods = cameraClass.declaredMethods
                .filter { it.name == "open" }
                .sortedBy { it.toGenericString() }

            if (methods.isEmpty()) {
                log(
                    Log.WARN,
                    TAG,
                    "CAMERA1_NO_OPEN_METHOD package=$packageName"
                )
                return
            }

            var installedCount = 0

            methods.forEachIndexed { index, method ->
                try {
                    installCamera1Method(method, packageName, index)
                    installedCount++
                } catch (t: Throwable) {
                    log(
                        Log.ERROR,
                        TAG,
                        "CAMERA1_METHOD_HOOK_FAILED " +
                            "package=$packageName " +
                            "method=${method.toGenericString()} " +
                            "error=${t.javaClass.simpleName}: ${t.message}"
                    )
                }
            }

            log(
                Log.INFO,
                TAG,
                "CAMERA1_HOOKS_INSTALLED package=$packageName " +
                    "installed=$installedCount total=${methods.size}"
            )
        } catch (t: Throwable) {
            log(
                Log.ERROR,
                TAG,
                "CAMERA1_HOOK_INSTALL_FAILED package=$packageName " +
                    "error=${t.javaClass.simpleName}: ${t.message}"
            )
        }
    }

    private fun installCamera1Method(
        method: Method,
        packageName: String,
        index: Int
    ) {
        val hookId = "vc-camera1-${method.name}-$index"

        hook(method)
            .setId(hookId)
            .intercept { chain ->
                log(
                    Log.INFO,
                    TAG,
                    "CAMERA1_OPEN package=$packageName " +
                        "method=${chain.executable.toGenericString()} " +
                        "args=${chain.args.size}"
                )

                chain.proceed()
            }
    }

    private fun installCamera2Hooks(
        classLoader: ClassLoader,
        packageName: String
    ) {
        try {
            val managerClass = Class.forName(
                "android.hardware.camera2.CameraManager",
                false,
                classLoader
            )

            val methods = managerClass.declaredMethods
                .filter { it.name == "openCamera" }
                .sortedBy { it.toGenericString() }

            if (methods.isEmpty()) {
                log(
                    Log.WARN,
                    TAG,
                    "CAMERA2_NO_OPEN_CAMERA_METHOD package=$packageName"
                )
                return
            }

            var installedCount = 0

            methods.forEachIndexed { index, method ->
                try {
                    installCamera2Method(method, packageName, index)
                    installedCount++
                } catch (t: Throwable) {
                    log(
                        Log.ERROR,
                        TAG,
                        "CAMERA2_METHOD_HOOK_FAILED " +
                            "package=$packageName " +
                            "method=${method.toGenericString()} " +
                            "error=${t.javaClass.simpleName}: ${t.message}"
                    )
                }
            }

            log(
                Log.INFO,
                TAG,
                "CAMERA2_HOOKS_INSTALLED package=$packageName " +
                    "installed=$installedCount total=${methods.size}"
            )
        } catch (t: Throwable) {
            log(
                Log.ERROR,
                TAG,
                "CAMERA2_HOOK_INSTALL_FAILED package=$packageName " +
                    "error=${t.javaClass.simpleName}: ${t.message}"
            )
        }
    }

    private fun installCamera2Method(
        method: Method,
        packageName: String,
        index: Int
    ) {
        val hookId = "vc-camera2-openCamera-$index"

        hook(method)
            .setId(hookId)
            .intercept { chain ->
                log(
                    Log.INFO,
                    TAG,
                    "CAMERA2_OPEN package=$packageName " +
                        "method=${chain.executable.toGenericString()} " +
                        "args=${chain.args.size}"
                )

                chain.proceed()
            }
    }

    private companion object {
        const val TAG = "VirtualCamera"
    }
}
