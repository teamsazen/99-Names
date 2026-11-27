package com.muzamil.asmaulhusna

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

/** Share, rate, other apps and website pages: plain intents, no extra libraries. */
object Actions {
    private const val PACKAGE = "com.muzamil.asmaulhusna"
    private const val SITE = "https://99.teamsazen.com"
    private const val SAZEN = "https://teamsazen.com"

    fun share(context: Context) {
        Analytics.log("share_app")
        val text = "99 Names - Asmaul Husna\nhttps://play.google.com/store/apps/details?id=$PACKAGE"
        start(context, Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
    }

    fun rate(context: Context) {
        Analytics.log("rate_app")
        openStore(context, PACKAGE, log = false)
    }

    fun openStore(context: Context, packageName: String, log: Boolean = true) {
        if (log) Analytics.log("open_other_app", "package" to packageName)
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    .setPackage("com.android.vending").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: ActivityNotFoundException) {
            start(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    /** Opens a page of the app's website in a Chrome Custom Tab in the app's ink colour. */
    fun openPage(context: Context, path: String) = openUrl(context, "$SITE$path")

    /** Opens Sazen's website (all our apps and games); [source] says which button was tapped. */
    fun openSazen(context: Context, source: String) {
        Analytics.log("open_sazen_website", "source" to source)
        openUrl(context, SAZEN)
    }

    private fun openUrl(context: Context, url: String) {
        val tab = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            putExtras(Bundle().apply { putBinder("android.support.customtabs.extra.SESSION", null) })
            putExtra("android.support.customtabs.extra.TOOLBAR_COLOR", 0xFF101820.toInt())
            putExtra("androidx.browser.customtabs.extra.SHARE_STATE", 2)
        }
        start(context, tab)
    }

    fun copy(context: Context, label: String, text: String) {
        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clip.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun start(context: Context, intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            Toast.show("Could not open this action. Please try again.")
        }
    }
}
