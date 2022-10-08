package net.iobb.koheinoapp.relativepositionestimator

import android.hardware.Sensor

class Estimator(val accelSensor: Sensor, val gyroSensor: Sensor) {

    private val log = mutableListOf<PositionSamplingLogItem>()


    fun addLogItem(){

    }

    fun setZeroPoint(){

    }
}