package com.sr.libraryapp

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sr.libraryapp.databinding.ActivityMainBinding
import com.sr.libraryapp.location.LocationModule
import com.sr.libraryapp.utils.getLocationString
import com.sr.libraryapp.utils.isInRadius
import com.sr.libraryapp.utils.logPrint

class MainActivity : AppCompatActivity() {

    private val TAG = "LocationModule"

    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //GoogleLocationModule.init(this)
        LocationModule.init(this)
        binding.tvAppName.setOnClickListener {
            getLocation()
        }
    }

    fun getLocation(){
        var timeInterval : Long = 10000
        LocationModule.getLocation(true, timeInterval){
            onLocationFoundOperation(it)
        }
        /*GoogleLocationModule.getLocation(isRepeated = true, timeInterval){
            onLocationFoundOperation()
        }*/
    }

    fun onLocationFoundOperation(it:Location){
        var apiLocation = Location(LocationManager.NETWORK_PROVIDER)
        apiLocation.latitude =18.969063515099037
        apiLocation.longitude = 72.81220707268889

        Log.d(TAG, "onLocationChanged: ${it.getLocationString()}")
        binding.tvAppName.text = it.getLocationString()
        logPrint(TAG, "getLocation: "+apiLocation.isInRadius(it,9.9f))
    }
}