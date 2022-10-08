package net.iobb.koheinoapp.relativepositionestimator

import android.hardware.Sensor
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Estimator(val accelSensor: Sensor, val gyroSensor: Sensor) {

    private val log = mutableListOf<AccelLogItem>()


    fun addLogItem(){

    }

    fun setZeroPoint(){

    }
}