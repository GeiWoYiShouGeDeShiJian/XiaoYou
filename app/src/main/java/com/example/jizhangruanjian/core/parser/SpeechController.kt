package com.example.jizhangruanjian.core.parser
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale
// P12 语音识别：封装 SpeechRecognizer，转文本后复用 TextParser
class SpeechController(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (Int) -> Unit
) {
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINA.toString())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }
    init {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onResult)
            }
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onResult)
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onError(error: Int) { onError(error) }
        })
    }
    fun start() { recognizer.startListening(intent) }
    fun cancel() { try { recognizer.cancel() } catch (_: Exception) {} }
    fun destroy() { try { recognizer.destroy() } catch (_: Exception) {} }
}