import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TextToSpeechManager(context: Context) {

    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("es", "ES")// Idioma por defecto
            }
        }
    }

    // Función para reproducir el texto en voz alta
    fun speak(text: String) {
        if (textToSpeech == null) {
            Log.d("TextToSpeech", "textToSpeechManager es nulo.")
        } else {
            Log.d("TextToSpeech", "textToSpeechManager está inicializado.")
        }

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Función para detener el TTS si está en ejecución
    fun stop() {
        textToSpeech?.stop()
    }

    // Función para liberar los recursos de TextToSpeech
    fun shutdown() {
        textToSpeech?.shutdown()
    }
}
