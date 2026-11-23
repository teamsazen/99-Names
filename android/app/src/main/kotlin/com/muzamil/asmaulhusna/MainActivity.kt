package com.muzamil.asmaulhusna

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.muzamil.asmaulhusna/app_actions")
            .setMethodCallHandler { call, result ->
                val url = "https://play.google.com/store/apps/details?id=com.muzamil.asmaulhusna"
                try {
                    when (call.method) {
                        "share" -> startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "99 Names - Asmaul Husna\n$url")
                        }, null))
                        "rate", "openStore" -> {
                            val target = if (call.method == "rate") packageName else call.argument<String>("packageName")
                            if (target == null || !target.matches(Regex("[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+"))) {
                                result.error("invalid_argument", "Invalid package name", null)
                                return@setMethodCallHandler
                            }
                            val storeUrl = "https://play.google.com/store/apps/details?id=$target"
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$target")).setPackage("com.android.vending"))
                            } catch (_: ActivityNotFoundException) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)))
                            }
                        }
                        "openUrl" -> {
                            // Only the app's own website (privacy policy, terms).
                            val link = call.argument<String>("url")
                            val uri = link?.let { Uri.parse(it) }
                            if (uri == null || uri.scheme != "https" || uri.host != "99.teamsazen.com") {
                                result.error("invalid_argument", "Unsupported link", null)
                                return@setMethodCallHandler
                            }
                            // A Chrome Custom Tab in the app's ink colour, through the
                            // standard Custom Tabs extras (no extra library); browsers
                            // without Custom Tabs simply open the page.
                            val tab = Intent(Intent.ACTION_VIEW, uri).apply {
                                putExtras(Bundle().apply { putBinder("android.support.customtabs.extra.SESSION", null) })
                                putExtra("android.support.customtabs.extra.TOOLBAR_COLOR", 0xFF101820.toInt())
                                putExtra("androidx.browser.customtabs.extra.SHARE_STATE", 2)
                            }
                            startActivity(tab)
                        }
                        else -> { result.notImplemented(); return@setMethodCallHandler }
                    }
                    result.success(null)
                } catch (_: Exception) {
                    result.error("unavailable", "Could not open the requested app", null)
                }
            }
    }
}
