import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import java.util.*

object LocationManager {
    var latitude: Double? = null
        private set
    var longitude: Double? = null
        private set

    // LiveData para la dirección
    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    // Actualiza la ubicación y la dirección
    fun updateLocation(context: Context, onComplete: (Boolean) -> Unit) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            }
            onComplete(false) // Indica que no se pudo obtener la ubicación
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Actualizamos latitud y longitud
                latitude = location.latitude
                longitude = location.longitude

                Log.d("LocationManager", "Latitud: $latitude, Longitud: $longitude")

                // Obtenemos la dirección con el Geocoder
                val geocoder = Geocoder(context, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0).split(",")[0]
                        _address.postValue(address)
                    } else {
                        Log.d("LocationManager", "No se encontró ninguna dirección")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("LocationManager", "Error al obtener la dirección: ${e.message}")
                }
                onComplete(true) // Indica que la actualización fue exitosa
            } else {
                _address.postValue("Ubicación no disponible")
                onComplete(false) // Indica que no se pudo obtener la ubicación
            }
        }.addOnFailureListener {
            Log.e("LocationManager", "Error al obtener la ubicación")
            _address.postValue("Error al obtener la ubicación")
            onComplete(false) // Indica que hubo un error
        }
    }
}
