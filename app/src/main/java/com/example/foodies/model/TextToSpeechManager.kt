import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Función para reproducir el texto en voz alta con callback
    fun speakWithCallback(text: String, onDone: () -> Unit) {
        // Establecer el UtteranceProgressListener para manejar el callback
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Aquí puedes manejar el inicio de la reproducción si lo necesitas
            }

            override fun onDone(utteranceId: String?) {
                // Llama al callback cuando la reproducción termine
                onDone()
            }

            override fun onError(utteranceId: String?) {
                // Aquí puedes manejar los errores si ocurren
                Log.e("TextToSpeech", "Error en la reproducción de texto.")
            }
        })

        // Usar un identificador único para el utterance (esto es necesario para el callback)
        val utteranceId = UUID.randomUUID().toString()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
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
