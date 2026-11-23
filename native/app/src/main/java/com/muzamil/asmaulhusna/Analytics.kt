package com.muzamil.asmaulhusna

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase Analytics. Collection is switched on only in release builds by the
 * `firebase_analytics_collection_enabled` manifest flag (analyticsEnabled in
 * app/build.gradle.kts); in debug builds every call is dropped by the SDK.
 */
object Analytics {
    private var fa: FirebaseAnalytics? = null

    fun init(context: Context) {
        fa = runCatching { FirebaseAnalytics.getInstance(context) }.getOrNull()
    }

    fun log(name: String, vararg params: Pair<String, Any>) {
        val bundle = Bundle().apply {
            for ((k, v) in params) when (v) {
                is Int -> putLong(k, v.toLong())
                is Long -> putLong(k, v)
                else -> putString(k, v.toString())
            }
        }
        fa?.logEvent(name, bundle)
    }

    fun screen(route: Route) {
        val name = when (route) {
            Route.Home -> "home"
            is Route.Name -> "name"
            Route.Settings -> "settings"
            Route.Licences -> "licences"
        }
        log(FirebaseAnalytics.Event.SCREEN_VIEW, FirebaseAnalytics.Param.SCREEN_NAME to name)
    }

    fun openName(number: Int, source: String) = log("open_name", "number" to number, "source" to source)

    fun favourite(number: Int, added: Boolean) =
        log(if (added) "favourite_add" else "favourite_remove", "number" to number)
}
