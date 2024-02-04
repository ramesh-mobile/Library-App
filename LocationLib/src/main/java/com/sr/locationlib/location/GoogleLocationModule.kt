package com.sr.locationlib.location

import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sr.libraryapp.utils.Const
import com.sr.libraryapp.utils.LocationPermissionUtils
import com.sr.libraryapp.utils.getLocationString
import com.sr.libraryapp.utils.logPrint
import java.util.concurrent.TimeUnit


object GoogleLocationModule {

    private val TAG = "GoogleLocationModule"

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest

    private lateinit var activity : AppCompatActivity

    private var isRepeated = false

    private var onLocationFoundOperation : (Location)->Unit={}

    private var getLocationRequestCallBack : ()->Unit = {
        getLocationRequest()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
            Log.d(TAG, "Location information isn't available not available")
        }

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.d(TAG, "onLocationResult: ${locationResult.locations.size}")
            locationResult.locations.forEach {
                onLocationFoundOperation.invoke(it)
            }
            if(!isRepeated)
                removeLocationUpdate()
        }
    }

    fun init(activity:AppCompatActivity){
        GoogleLocationModule.activity = activity
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        LocationPermissionUtils.init(activity, getLocationRequestCallBack, onLocationFoundOperation)
        removeLocationUpdate()
    }

    private fun getLocationRequest(){
        if(Const.LOCATION_PERMISSION.any { LocationPermissionUtils.checkSelfPermission(it)}){
            LocationPermissionUtils.checkPermission(locationRequest)
            return
        }
        //unregisterAllLauncher()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun removeLocationUpdate(){
        val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                logPrint(TAG, "Location Callback removed.")
            } else {
                logPrint(TAG, "Failed to remove Location Callback.")
            }
        }
    }
    fun getLocation(
        isRepeated : Boolean,
        timeIntervalInMilli : Long = TimeUnit.MINUTES.toMinutes(30),
        onLocationFoundOperation:(Location)->Unit
    ){
        if(!this::activity.isInitialized){
            logPrint(TAG, "initializeVariables: activity is not initialized")
            return
        }
        GoogleLocationModule.isRepeated =  isRepeated
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,timeIntervalInMilli).build()
        GoogleLocationModule.onLocationFoundOperation = onLocationFoundOperation
        LocationPermissionUtils.checkPermission(locationRequest)
    }
}