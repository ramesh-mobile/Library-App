package com.sr.libraryapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.datatypes.units.Power
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.sr.libraryapp.utils.Const
import com.sr.libraryapp.utils.getLocationString
import com.sr.libraryapp.utils.logPrint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LocationModule(var activity: AppCompatActivity) :  LocationListener{

    private val TAG = "LocationModule"
    lateinit var locationManager : LocationManager
    private val gadgetQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    var fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    var coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
    var backgroundLocation = Manifest.permission.ACCESS_BACKGROUND_LOCATION



    fun checkSelfPermission(permission:String) = ActivityCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED

    //private lateinit var activity : ComponentActivity
    private var onLocationFoundOperation : (String)->Unit={

    }
    private fun checkProvider(){

        if(Const.LOCATION_PERMISSION.any { checkSelfPermission(it) }){
            checkPermission()
            return
        }

        //startWakeLock()
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, this@LocationModule)
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,0f,this@LocationModule)

        }
        var locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        var locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)


        var gpsLocationTime : Long = 0
        locationGPS?.let {
            gpsLocationTime = it.time
        }
        var networkLocationTime : Long = 0
        locationNetwork?.let {
            networkLocationTime = it.time
        }

        var latestLoc = if(gpsLocationTime>networkLocationTime) locationGPS else locationNetwork
        Log.d(TAG, "checkProvider: ${latestLoc?.getLocationString()}")
        onLocationFoundOperation.invoke(latestLoc?.getLocationString()?:"Location not found")
        //getLastBestLocation()

        //stopWakeLock()

    }

    fun fetchLocationData(){

        //locationManager.removeUpdates(this)

    }

    fun getLocation(onLocationFoundOperation:(String)->Unit){
        //this.activity = activity
        this.onLocationFoundOperation = onLocationFoundOperation
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkPermission()
    }

    private fun checkPermission(){
        var askPermission = arrayOf<String>()
        Const.INITIAL_PERMISSION.forEach {
            if(ActivityCompat.checkSelfPermission(activity, it)!= PackageManager.PERMISSION_GRANTED)
                askPermission+=(it)
        }

        if(askPermission.isNotEmpty())
            onPermissionContract.launch(askPermission)
        else{
            checkProvider()
        }
    }

    private val onPermissionContract = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        Toast.makeText(activity, "on activity contracts", Toast.LENGTH_SHORT).show()
        if (gadgetQ) {
            if(checkSelfPermission(backgroundLocation))
                locationPermissionContract.launch(backgroundLocation)
            else
                checkProvider()
        }else{
            checkProvider()
        }
    }

    private val locationPermissionContract = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){
            checkProvider()
        }
    }


    override fun onLocationChanged(location: Location) {
        logPrint(TAG,"Location is :${location.getLocationString()}")
    }

    private val WAKE_LOCK_TAG = "LibraryApp:WAKE_LOCK_TAG";

    var wakeLock : PowerManager.WakeLock? = null;
    private fun startWakeLock(){
        if(wakeLock==null) {
            var powerManager: PowerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,WAKE_LOCK_TAG) as PowerManager.WakeLock
        }
        wakeLock?.acquire()
    }

    private fun stopWakeLock(){
        if(wakeLock?.isHeld==true)
            wakeLock?.release()
    }

}