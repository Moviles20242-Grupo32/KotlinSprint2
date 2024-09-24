package com.example.foodies.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationRequest
import androidx.compose.ui.util.fastCbrt
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import com.example.foodies.model.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class LocationLiveData(context: Context): LiveData<Location>() {
    
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override fun onActive() {
        super.onActive()
        startLocationUpdates()
    }

    private fun startLocationUpdates(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, null)
        
    }
    
    private val locationCallBack = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null) {
                super.onLocationResult(locationResult)
                
                for(location in locationResult.locations){
                    setLocationData(location)
                }
            }
            
            
        }
        
    }

    private fun setLocationData(location: Location?) {

    }

    override fun onInactive() {
        super.onInactive()
    }

    companion object{
        
        val locationRequest: com.google.android.gms.location.LocationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 60000
            fastestInterval = 15000
            priority = LocationRequest.QUALITY_HIGH_ACCURACY
        }

    }
    }
