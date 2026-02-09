package com.chesspuzzles.woodpecker.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.chesspuzzles.woodpecker.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) {
    private val soundPool: SoundPool
    private var moveSoundId: Int = 0
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var loaded = false

    companion object {
        private const val SAMPLE_RATE = 44100
    }

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrs)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = true
        }

        generateAndLoadSounds()
    }

    private fun generateAndLoadSounds() {
        val cacheDir = context.cacheDir

        val moveFile = File(cacheDir, "snd_move.wav")
        val correctFile = File(cacheDir, "snd_correct.wav")
        val wrongFile = File(cacheDir, "snd_wrong.wav")

        writeWav(moveFile, generateMoveSound())
        writeWav(correctFile, generateCorrectSound())
        writeWav(wrongFile, generateWrongSound())

        moveSoundId = soundPool.load(moveFile.absolutePath, 1)
        correctSoundId = soundPool.load(correctFile.absolutePath, 1)
        wrongSoundId = soundPool.load(wrongFile.absolutePath, 1)
    }

    private fun generateMoveSound(): ShortArray {
        val durationMs = 50
        val numSamples = SAMPLE_RATE * durationMs / 1000
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-t * 80.0) // Sharp decay
            val wave = sin(2.0 * PI * 1000.0 * t) + 0.5 * sin(2.0 * PI * 2000.0 * t)
            samples[i] = (wave * envelope * 16000).toInt().coerceIn(-32768, 32767).toShort()
        }

        return samples
    }

    private fun generateCorrectSound(): ShortArray {
        // C5→E5→G5 arpeggio, each note ~120ms
        val noteFreqs = doubleArrayOf(523.25, 659.25, 783.99) // C5, E5, G5
        val noteDurationMs = 120
        val samplesPerNote = SAMPLE_RATE * noteDurationMs / 1000
        val totalSamples = samplesPerNote * noteFreqs.size
        val samples = ShortArray(totalSamples)

        for (n in noteFreqs.indices) {
            val freq = noteFreqs[n]
            val offset = n * samplesPerNote
            for (i in 0 until samplesPerNote) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = exp(-t * 6.0) // Gentle decay per note
                val wave = sin(2.0 * PI * freq * t)
                val value = (wave * envelope * 20000).toInt().coerceIn(-32768, 32767).toShort()
                samples[offset + i] = value
            }
        }

        return samples
    }

    private fun generateWrongSound(): ShortArray {
        // Descending frequency sweep 350Hz→180Hz, 250ms, with overtones
        val durationMs = 250
        val numSamples = SAMPLE_RATE * durationMs / 1000
        val samples = ShortArray(numSamples)

        val startFreq = 350.0
        val endFreq = 180.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val freq = startFreq + (endFreq - startFreq) * progress
            val envelope = exp(-t * 4.0)
            val wave = sin(2.0 * PI * freq * t) +
                    0.4 * sin(2.0 * PI * freq * 2.0 * t) +
                    0.2 * sin(2.0 * PI * freq * 3.0 * t)
            samples[i] = (wave * envelope * 14000).toInt().coerceIn(-32768, 32767).toShort()
        }

        return samples
    }

    private fun writeWav(file: File, samples: ShortArray) {
        val dataSize = samples.size * 2
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)

        // WAV header
        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)           // Subchunk1Size (PCM)
        buffer.putShort(1)          // AudioFormat (PCM)
        buffer.putShort(1)          // NumChannels (mono)
        buffer.putInt(SAMPLE_RATE)  // SampleRate
        buffer.putInt(SAMPLE_RATE * 2) // ByteRate
        buffer.putShort(2)          // BlockAlign
        buffer.putShort(16)         // BitsPerSample
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)

        for (sample in samples) {
            buffer.putShort(sample)
        }

        FileOutputStream(file).use { it.write(buffer.array()) }
    }

    fun playMove() {
        if (!appPreferences.isSoundEnabled()) return
        if (moveSoundId != 0) soundPool.play(moveSoundId, 0.7f, 0.7f, 1, 0, 1f)
    }

    fun playCorrect() {
        if (!appPreferences.isSoundEnabled()) return
        if (correctSoundId != 0) soundPool.play(correctSoundId, 0.8f, 0.8f, 1, 0, 1f)
    }

    fun playWrong() {
        if (!appPreferences.isSoundEnabled()) return
        if (wrongSoundId != 0) soundPool.play(wrongSoundId, 0.8f, 0.8f, 1, 0, 1f)
    }
}
