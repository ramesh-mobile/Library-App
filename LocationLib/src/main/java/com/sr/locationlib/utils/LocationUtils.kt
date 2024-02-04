package com.sr.locationlib.utils

import android.location.Location


fun Location.getLocationString() = "Latitude:${this.latitude}, Longitude:${this.longitude}"

fun Location.isInRadius(otherLocation: Location, radius:Float) = radius>=distanceTo(otherLocation)