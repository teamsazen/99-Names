package com.muzamil.asmaulhusna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.muzamil.asmaulhusna.data.Settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        Settings.load(this)
        Analytics.init(this)
        setContent { AppRoot() }
        checkForUpdate()
    }

    /**
     * Asks Google Play whether a newer release exists, downloads it in the background
     * (flexible update) and installs once ready. Fails silently when offline,
     * sideloaded or in debug builds.
     */
    private fun checkForUpdate() {
        val manager = runCatching { AppUpdateManagerFactory.create(this) }.getOrNull() ?: return
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE ||
                !info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) return@addOnSuccessListener
            lateinit var listener: InstallStateUpdatedListener
            listener = InstallStateUpdatedListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    manager.unregisterListener(listener)
                    manager.completeUpdate()
                }
            }
            manager.registerListener(listener)
            runCatching {
                manager.startUpdateFlow(info, this, AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build())
            }
        }
    }
}
