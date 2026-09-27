package com.ppimenta.notificationcommand.data

import android.content.Context
import android.net.Uri
import androidx.core.content.edit

/** Stores the single, app-wide alert tone the user picked (null = follow the system default). */
object AlertPreferences {
    private const val PREFS_NAME = "alert_prefs"
    private const val KEY_TONE_URI = "tone_uri"

    fun getToneUri(context: Context): Uri? {
        val stored = prefs(context).getString(KEY_TONE_URI, null) ?: return null
        return Uri.parse(stored)
    }

    fun setToneUri(context: Context, uri: Uri?) {
        prefs(context).edit {
            if (uri == null) remove(KEY_TONE_URI) else putString(KEY_TONE_URI, uri.toString())
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
