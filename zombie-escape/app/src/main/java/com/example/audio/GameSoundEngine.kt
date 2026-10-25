package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Feature 10: Sound Effects
 * High-performance, zero-asset audio synthesizer using Android AudioTrack.
 * Generates retro 8-bit / 16-bit arcade sound effects for zombie detection,
 * footstep audio, pickup jingles, player hurt bites, and level transitions.
 */
class GameSoundEngine(
    private val scope: CoroutineScope
) {
    var isMuted: Boolean = false

    private val sampleRate = 22050

    /**
     * Synthesizes and plays a short sound buffer asynchronously.
     */
    private fun playToneBuffer(samples: ShortArray) {
        if (isMuted) return
        scope.launch(Dispatchers.Default) {
            try {
                val bufferSize = samples.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                // Release after playback duration
                val playDurationMs = (samples.size * 1000L) / sampleRate + 50L
                kotlinx.coroutines.delay(playDurationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio device init errors gracefully
            }
        }
    }

    /**
     * Requirement 10: Audio cue when zombie detects player!
     * Low ominous growl followed by a sharp alert screech.
     */
    fun playZombieAlert() {
        if (isMuted) return
        val duration = 0.35f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            // Pitch rises from 110Hz (growl) to 480Hz (screech)
            val freq = 110.0 + (t / duration) * 370.0
            // Blend sawtooth/square with random noise for zombie gargle
            val wave = if ((i % (sampleRate / freq.toInt().coerceAtLeast(1))) < (sampleRate / freq.toInt().coerceAtLeast(1)) / 2) 1.0 else -1.0
            val noise = (Random.nextFloat() * 2f - 1f) * 0.4f
            val envelope = if (t < 0.05f) t / 0.05f else (1.0f - (t - 0.05f) / (duration - 0.05f))
            val sample = ((wave * 0.6f + noise) * envelope * Short.MAX_VALUE * 0.55f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Tense heartbeat pulse when zombie is in close chasing range.
     */
    fun playHeartbeatPulse() {
        if (isMuted) return
        val duration = 0.12f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val freq = 60.0 - (t / duration) * 20.0
            val wave = sin(2.0 * PI * freq * t)
            val envelope = (1.0f - t / duration)
            val sample = (wave * envelope * Short.MAX_VALUE * 0.45f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Subtle footstep tap.
     */
    fun playFootstep() {
        if (isMuted) return
        val duration = 0.04f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noise = (Random.nextFloat() * 2f - 1f)
            val envelope = (1.0f - t / duration)
            val sample = (noise * envelope * Short.MAX_VALUE * 0.15f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Triumphant golden key pickup jingle.
     */
    fun playPickupKey() {
        if (isMuted) return
        val duration = 0.30f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val noteFrequencies = listOf(587.33, 739.99, 880.0, 1174.66) // D5, F#5, A5, D6 arpeggio

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIndex = ((t / duration) * noteFrequencies.size).toInt().coerceIn(0, noteFrequencies.lastIndex)
            val freq = noteFrequencies[noteIndex]
            val wave = sin(2.0 * PI * freq * t)
            val envelope = 1.0f - (t % (duration / noteFrequencies.size)) / (duration / noteFrequencies.size)
            val sample = (wave * envelope * Short.MAX_VALUE * 0.35f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Medkit pickup healing chime.
     */
    fun playPickupMedkit() {
        if (isMuted) return
        val duration = 0.22f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val notes = listOf(523.25, 659.25, 783.99) // C5, E5, G5

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIndex = ((t / duration) * notes.size).toInt().coerceIn(0, notes.lastIndex)
            val freq = notes[noteIndex]
            val wave = sin(2.0 * PI * freq * t)
            val envelope = 1.0f - (t / duration)
            val sample = (wave * envelope * Short.MAX_VALUE * 0.35f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Player damage bite / claw strike sound.
     */
    fun playPlayerDamage() {
        if (isMuted) return
        val duration = 0.20f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val freq = 140.0 - (t / duration) * 80.0
            val wave = sin(2.0 * PI * freq * t)
            val noise = (Random.nextFloat() * 2f - 1f) * 0.5f
            val envelope = 1.0f - (t / duration)
            val sample = ((wave + noise) * envelope * Short.MAX_VALUE * 0.55f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Level victory exit chime.
     */
    fun playLevelComplete() {
        if (isMuted) return
        val duration = 0.55f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIdx = ((t / duration) * notes.size).toInt().coerceIn(0, notes.lastIndex)
            val freq = notes[noteIdx]
            val wave = sin(2.0 * PI * freq * t)
            val envelope = (1.0f - (t / duration) * 0.5f)
            val sample = (wave * envelope * Short.MAX_VALUE * 0.40f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Game Over death stinger.
     */
    fun playGameOver() {
        if (isMuted) return
        val duration = 0.60f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val notes = listOf(392.0, 369.99, 349.23, 311.13) // G4, F#4, F4, D#4

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIdx = ((t / duration) * notes.size).toInt().coerceIn(0, notes.lastIndex)
            val freq = notes[noteIdx]
            val wave = if ((i % (sampleRate / freq.toInt().coerceAtLeast(1))) < (sampleRate / freq.toInt().coerceAtLeast(1)) / 2) 1.0 else -1.0
            val envelope = (1.0f - t / duration)
            val sample = (wave * envelope * Short.MAX_VALUE * 0.40f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }

    /**
     * Distraction decoy throw / flare hiss.
     */
    fun playDecoyThrow() {
        if (isMuted) return
        val duration = 0.25f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noise = (Random.nextFloat() * 2f - 1f)
            val envelope = (1.0f - t / duration)
            val sample = (noise * envelope * Short.MAX_VALUE * 0.35f).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playToneBuffer(buffer)
    }
}
