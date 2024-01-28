package com.sr.libraryapp.utils


import android.Manifest

object Const {

    const val SHOW_POPUP = "Show Popup"
    const val LOCK_SCREEN = "Lock screen"
    const val LOCATION = "Location"
    const val CHANGE_ICON = "Change Icon"

    var InitialPermission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    var INITIAL_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    var LOCATION_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )


}