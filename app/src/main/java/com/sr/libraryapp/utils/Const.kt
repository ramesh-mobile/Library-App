package com.sr.libraryapp.utils


import android.Manifest

object Const {

    var BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    var LOCATION_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )


}