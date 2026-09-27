# Notification Command

Android app that reads incoming notifications and, for contacts you configure, plays an
alert tone that bypasses silent mode — even though your phone stays silenced for everything
else.

## How it bypasses silent mode

Alert tones are played on the **ALARM** audio stream (`AudioAttributes.USAGE_ALARM`) instead of
the notification/ringer stream. The alarm stream ignores ringer mode (silent/vibrate), and by
default Android's Do Not Disturb configuration exempts alarms too — so this one mechanism covers
both cases you mentioned. If your DND config has been customized to also block alarms, grant the
app "Do Not Disturb access" from the in-app setup screen so it can request an exception.

## Alert behavior

- **First message from a matched sender**: plays immediately.
- **Debounce**: further messages from the same sender within 60 seconds do **not** replay the
  sound (so 5 messages in a row only alert once).
- **Reminders**: if you don't open/dismiss the conversation, the tone repeats every
  *N* minutes, up to *X* times (both configurable per rule). Opening or clearing the
  notification stops the remaining reminders.

## Project layout

```
app/src/main/java/com/ppimenta/notificationcommand/
  data/         Room entities, DAOs, database, repository
  matching/     Rule-matching logic
  notification/ NotificationListenerService + notification field extraction
  alerting/     Tone playback, AlarmManager scheduling, reminder + boot receivers
  ui/           Compose screens (permissions setup, rule list, rule editor), ViewModel
```

### Data flow

1. `NotificationCommandListenerService` gets every posted notification, extracts the sender
   name (via `Notification.EXTRA_TITLE`, falling back to the last `MessagingStyle` message's
   `Person` on API 28+), and checks it against your enabled rules.
2. A match records/updates a `ContactAlertStateEntity` (one per rule + sender) that tracks the
   debounce timestamp and the reminder chain's progress.
3. Reminders are (re)armed one at a time via `AlarmManager` exact alarms; `ReminderReceiver`
   fires the next tone and re-arms the following one until the configured count is reached, an
   alarm firing after the tone was acknowledged is a no-op.
4. `onNotificationRemoved` (user opened/cleared the notification) marks the state acknowledged
   and cancels any pending reminder for that conversation.
5. `BootReceiver` re-arms any reminders that were still pending, since `AlarmManager` alarms are
   cleared on reboot.

## Required setup (all done from the in-app permissions screen)

| Permission | Why |
|---|---|
| Notification access | Lets the app read notification content system-wide. |
| Post notifications | Shows the small status notification while a tone plays (required for the foreground service on Android 13+). |
| Alarms & reminders | Exact timing for reminders. The manifest also declares `USE_EXACT_ALARM`, which — for a sideloaded/personal build — grants this automatically without a settings trip. |
| Ignore battery optimization | Keeps the listener process alive in the background. |
| Do Not Disturb access (optional) | Only needed if you rely on DND instead of silent mode. |

## Building

Requires the Android SDK (compileSdk/targetSdk 35) and Android Studio or a local SDK install —
this repo includes a working Gradle wrapper (`./gradlew`), but the sandbox this was authored in
has no Android SDK and blocks network access to Google's Maven repo, so the build could not be
executed here. Open the project in Android Studio (which will fetch the SDK/dependencies), or run:

```
./gradlew assembleDebug
```

with `ANDROID_HOME` pointing at a valid SDK.

## Known limitations / next steps

- Sender matching is a simple case-insensitive substring match; no regex or contact-picker
  integration yet.
- Group chats: the sender extraction prefers the last message's individual sender when available
  (API 28+ `MessagingStyle`), but on older APIs or apps that don't use `MessagingStyle` it falls
  back to the notification title, which may be the group name.
- No instrumented/unit tests yet — recommended before relying on this for real alerts.
