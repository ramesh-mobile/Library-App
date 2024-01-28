package com.sr.libraryapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.sr.libraryapp.utils.Const
import com.sr.libraryapp.utils.getLocationString
import java.util.concurrent.TimeUnit


class GoogleLocationModule() {

    private val TAG = "GoogleLocationModule"

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    lateinit var locationManager : LocationManager

    private lateinit var locationRequest: LocationRequest

    companion object{
        private lateinit var activity : AppCompatActivity
        fun init(activity:AppCompatActivity){
            this.activity = activity
            GoogleLocationModule().registerAllLauncher()
        }

    }

    fun init(){
        //this.activity = activity
    }

    // This will store current location info
    private var currentLocation: Location? = null

    private fun initializeVariables(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,TimeUnit.MILLISECONDS.toMillis(30000)).build()
        settingClient = LocationServices.getSettingsClient(activity)
        removeLocationUpdate()

        LocationSettingsRequest.Builder().apply {
            addLocationRequest(locationRequest)
            setAlwaysShow(true)
            mLocationSettingsRequest = build()
        }
    }
    var i = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
            Log.d(TAG, "Location information isn't available not available")
        }

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.locations.forEach {
                currentLocation = it
                i++
                onLocationFoundOperation.invoke("iter: ${i}, "+it?.getLocationString()?:"Location not found")
            }
            /*locationResult.lastLocation.let {
                currentLocation = it
                onLocationFoundOperation.invoke(it?.getLocationString()?:"Location not found")
            }*/
            if(!isRepeated)
                removeLocationUpdate()
        }
    }

    var fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    var coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
    var backgroundLocation = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    private val gadgetQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private lateinit var settingClient: SettingsClient
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    private fun checkSelfPermission(permission:String) = ActivityCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED

    private fun checkGpsStatus(){
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            getLocationRequest()
        else{
            settingClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener {
                    getLocationRequest()
                }
                .addOnFailureListener {
                    when((it as ApiException).statusCode){
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED->{
                            try {
                                (it as ResolvableApiException).let {
                                    IntentSenderRequest.Builder(it.resolution).build().let {isReq->
                                        gpsContract.launch(isReq)
                                    }
                                }
                            }catch (e: SendIntentException){
                                "PendingIntent unable to execute request.".let {
                                    Toast.makeText(activity,it,Toast.LENGTH_LONG).show()
                                }
                                e.printStackTrace()
                            }

                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE->{
                            "Location settings are inadequate, and cannot be fixed here. Fix in Settings.".let {
                                Log.e(TAG, it)
                                Toast.makeText(activity,it,Toast.LENGTH_LONG).show()
                            }
                        }
                        else->{
                            Toast.makeText(activity,"Unknown error found",Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }

    private fun getLocationRequest(){
        if(Const.LOCATION_PERMISSION.any { checkSelfPermission(it) }){
            checkPermission()
            return
        }
        //unregisterAllLauncher()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    fun registerAllLauncher(){
        locationPermissionContract = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
            if(it){
                checkGpsStatus()
            }
        }

        onPermissionContract = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            Toast.makeText(activity, "on activity contracts", Toast.LENGTH_SHORT).show()
            if (gadgetQ) {
                if(checkSelfPermission(backgroundLocation))
                    locationPermissionContract.launch(backgroundLocation)
                else
                    checkGpsStatus()
            }else{
                checkGpsStatus()
            }
        }

        gpsContract = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()){
            if(it?.resultCode == Activity.RESULT_OK){
                getLocationRequest()
            }else{
                onLocationFoundOperation.invoke("Location is disable please enable it")
            }
        }
    }

    private fun unregisterAllLauncher() {
        gpsContract.unregister()
        onPermissionContract.unregister()
        locationPermissionContract.unregister()
    }

    fun removeLocationUpdate(){
        val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Location Callback removed.")
            } else {
                Log.d(TAG, "Failed to remove Location Callback.")
            }
        }
    }


    var isRepeated = false
    fun getLocation(isRepeated : Boolean,onLocationFoundOperation:(String)->Unit){
        this.isRepeated =  isRepeated
        this.onLocationFoundOperation = onLocationFoundOperation
        initializeVariables()
        checkPermission()
    }

    //private lateinit var activity : ComponentActivity
    private lateinit var onLocationFoundOperation : (String)->Unit//={}

    private fun checkPermission(){
        var askPermission = arrayOf<String>()
        Const.INITIAL_PERMISSION.forEach {
            if(ActivityCompat.checkSelfPermission(activity, it)!= PackageManager.PERMISSION_GRANTED)
                askPermission+=(it)
        }

        if(askPermission.isNotEmpty())
            onPermissionContract.launch(askPermission)
        else{
            checkGpsStatus()
        }
    }

    private lateinit var gpsContract : ActivityResultLauncher<IntentSenderRequest>

    private lateinit var onPermissionContract : ActivityResultLauncher<Array<String>>

    private lateinit var locationPermissionContract : ActivityResultLauncher<String>
}