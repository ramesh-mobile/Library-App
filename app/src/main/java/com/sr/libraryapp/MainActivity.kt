package com.sr.libraryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.sr.libraryapp.databinding.ActivityMainBinding
import com.sr.libraryapp.utils.logPrint

class MainActivity : AppCompatActivity() {

    private val TAG = "LocationModule"

    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        GoogleLocationModule.init(this)
        //getLocation()
        binding.tvAppName.setOnClickListener {
            getLocation()
        }
    }

    fun getLocation(){
        /*LocationModule(this).getLocation{
            logPrint(TAG,it)
            binding.tvAppName.text = it
        }*/

        GoogleLocationModule().getLocation(isRepeated = true){
            logPrint(TAG,it)
            binding.tvAppName.text = it
        }
    }
}