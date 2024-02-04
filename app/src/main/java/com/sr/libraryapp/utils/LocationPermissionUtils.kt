package com.sr.libraryapp.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient

object LocationPermissionUtils {

    private val TAG = "LocationPermissionUtils"

    private lateinit var activity : AppCompatActivity

    private lateinit var gpsContract : ActivityResultLauncher<IntentSenderRequest>

    private lateinit var onPermissionContract : ActivityResultLauncher<Array<String>>

    private lateinit var locationPermissionContract : ActivityResultLauncher<String>

    lateinit var locationManager : LocationManager

    private lateinit var settingClient: SettingsClient

    private lateinit var locationRequest: LocationRequest

    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    private val gadgetQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private lateinit var onLocationFoundOperation : (Location)->Unit

    private lateinit var getLocationRequestCallBack : ()->Unit
    fun init(
        activity: AppCompatActivity,
        getLocationRequestCallBack: () -> Unit,
        onLocationFoundOperation: (Location) -> Unit
    ){
        this.activity = activity
        registerAllLauncher()
        this.getLocationRequestCallBack = getLocationRequestCallBack
        this.onLocationFoundOperation = onLocationFoundOperation
    }

    private fun registerAllLauncher(){
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
                if(checkSelfPermission(Const.BACKGROUND_LOCATION))
                    locationPermissionContract.launch(Const.BACKGROUND_LOCATION)
                else
                    checkGpsStatus()
            }else{
                checkGpsStatus()
            }
        }

        gpsContract = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()){
            if(it?.resultCode == Activity.RESULT_OK){
                getLocationRequestCallBack()
            }else{
                activity.showToast("Location is disable please enable it")
            }
        }
    }

    private fun unregisterAllLauncher() {
        gpsContract.unregister()
        onPermissionContract.unregister()
        locationPermissionContract.unregister()
    }

    fun checkSelfPermission(permission:String) =
        ActivityCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED

    fun checkPermission(locationRequest:LocationRequest?) {
        locationRequest?.let { initializeVariables(it) }

        var askPermission = arrayOf<String>()
        Const.LOCATION_PERMISSION.forEach {
            if (ActivityCompat.checkSelfPermission(activity,it) != PackageManager.PERMISSION_GRANTED)
                askPermission += (it)
        }

        if (askPermission.isNotEmpty())
            onPermissionContract.launch(askPermission)
        else {
            checkGpsStatus()
        }
    }

    private fun initializeVariables(locationRequest: LocationRequest) {
        this.locationRequest = locationRequest
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        settingClient = LocationServices.getSettingsClient(activity)

        LocationSettingsRequest.Builder().apply {
            addLocationRequest(locationRequest)
            setAlwaysShow(true)
            mLocationSettingsRequest = build()
        }
    }

    private fun checkGpsStatus(){
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            getLocationRequestCallBack()
        else{
            settingClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener {
                    getLocationRequestCallBack.invoke()
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
                            }catch (e: IntentSender.SendIntentException){
                                "PendingIntent unable to execute request.".let {
                                    activity.showToast(it)
                                }
                                e.printStackTrace()
                            }

                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE->{
                            "Location settings are inadequate, and cannot be fixed here. Fix in Settings.".let {
                                Log.e(TAG, it)
                                activity.showToast(it)
                            }
                        }
                        else->{
                            activity.showToast("Unknown error found")
                        }
                    }
                }
        }
    }
}