package com.example.breeze

import android.app.Activity
import androidx.lifecycle.Lifecycle
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal

class InternetChecker {
    fun checkInternet(thisView : Activity, lifecycleView : Lifecycle){
        NoInternetDialogSignal.Builder(
            thisView,
            lifecycleView
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }
                cancelable = false
                noInternetConnectionTitle = "No Internet"
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi"
                mobileDataOnButtonText = "Mobile data"

                onAirplaneModeTitle = "No Internet"
                onAirplaneModeMessage = "You have turned on the airplane mode." //optional
                pleaseTurnOffText = "Please turn off"
                airplaneModeOffButtonText = "Airplane mode"
                showAirplaneModeOffButtons = true
            }
        }.build()

    }
}


