package com.nuha.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nuha.app.R
import java.util.Locale

/**
 * Arabic voice prompts through the device's text-to-speech engine. A stand-in until recorded voice assets exist:
 * if no Arabic voice is installed it stays silent, and every screen must still work without it.
 */
class Speech(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null

    init { tts = TextToSpeech(context.applicationContext, this) }

    override fun onInit(status: Int) {
        val engine = tts ?: return
        ready = status == TextToSpeech.SUCCESS && engine.setLanguage(Locale.forLanguageTag("ar")) >= TextToSpeech.LANG_AVAILABLE
        if (ready) pending?.let(::say)
        pending = null
    }

    fun say(text: String) {
        if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nuha-prompt") else pending = text
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

/** Voice prompts for this composition, or null when the voice channel is off. */
@Composable
fun rememberSpeech(enabled: Boolean): Speech? {
    val context = LocalContext.current
    val speech = remember(enabled) { if (enabled) Speech(context) else null }
    DisposableEffect(speech) { onDispose { speech?.release() } }
    return speech
}

/** Short interface sounds on the effects channel; soft-attack and quiet, never a buzzer (ui-style.md, Sound). */
class Effects(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
        .build()
    private val pop = pool.load(context, R.raw.pop, 1)

    fun pop() { pool.play(pop, 1f, 1f, 1, 0, 1f) }
    fun release() = pool.release()
}

/** Interface sounds for this composition, or null when the effects channel is off. */
@Composable
fun rememberEffects(enabled: Boolean): Effects? {
    val context = LocalContext.current
    val effects = remember(enabled) { if (enabled) Effects(context) else null }
    DisposableEffect(effects) { onDispose { effects?.release() } }
    return effects
}
