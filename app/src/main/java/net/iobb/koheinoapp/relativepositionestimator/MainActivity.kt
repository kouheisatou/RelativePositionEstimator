package net.iobb.koheinoapp.relativepositionestimator

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import net.iobb.koheinoapp.relativepositionestimator.ui.theme.RelativePositionEstimatorTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var estimator: Estimator
    private var currentLogItem: MutableState<AccelLogItem?> = mutableStateOf(null)

    private var acceleration: FloatArray? = null
    private var rotationMatrix: FloatArray? = null

    override fun onResume() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if(accelSensor == null || gyroSensor == null) {
            throw Exception("このデバイスは計測に必要なセンサを搭載していません")
        }

        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL)

        estimator = Estimator(accelSensor, gyroSensor)

        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelativePositionEstimatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Text(currentLogItem.value.toString())
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> acceleration = event.values
            Sensor.TYPE_ROTATION_VECTOR -> {
                rotationMatrix = FloatArray(16)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            }
        }

        if(acceleration != null && rotationMatrix != null){
            currentLogItem.value = AccelLogItem(acceleration!!, rotationMatrix!!)

            acceleration = null
            rotationMatrix = null
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}