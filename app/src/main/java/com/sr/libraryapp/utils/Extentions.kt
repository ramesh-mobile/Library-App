package com.sr.libraryapp.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Context.showToast(msg:String){
    Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
}
fun logPrint(TAG:String,msg:String){
    Log.d(TAG, "logPrint: $msg")
}