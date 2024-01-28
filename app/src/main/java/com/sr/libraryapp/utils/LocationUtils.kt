package com.sr.libraryapp.utils

import android.location.Location


fun Location.getLocationString() = "Latitude:${this.latitude}, Longitude:${this.longitude}"