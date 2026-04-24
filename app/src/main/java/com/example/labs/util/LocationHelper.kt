package com.example.labs.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCityLocation(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = suspendCoroutine<Location?> { continuation ->
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resume(null)
                    }
                }
            }

            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    address.locality ?: address.adminArea ?: address.countryName ?: "Неизвестно"
                } else {
                    "Неизвестно"
                }
            } else {
                "Неизвестно"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Неизвестно"
        }
    }
}
