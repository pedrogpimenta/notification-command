package com.ppimenta.notificationcommand.alerting

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import com.ppimenta.notificationcommand.data.AlertPreferences

/**
 * Plays a short tone on the ALARM audio stream, which is exempt from ringer mode (silent/vibrate)
 * and, by default, from Do Not Disturb's "alarms" exception. This is the mechanism that lets the
 * app sound off even when the phone itself is silenced.
 *
 * The tone itself is whatever the user picked in Settings ([AlertPreferences]), falling back to
 * the device's default alarm sound.
 */
object AlertTonePlayer {
    private const val TAG = "AlertTonePlayer"

    fun play(context: Context, onFinished: (() -> Unit)? = null) {
        try {
            val uri = AlertPreferences.getToneUri(context)
                ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            player.setDataSource(context, uri)
            player.setOnCompletionListener {
                it.release()
                onFinished?.invoke()
            }
            player.setOnErrorListener { mp, _, _ ->
                mp.release()
                onFinished?.invoke()
                true
            }
            player.isLooping = false
            player.prepare()
            player.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alert tone", e)
            onFinished?.invoke()
        }
    }
}
