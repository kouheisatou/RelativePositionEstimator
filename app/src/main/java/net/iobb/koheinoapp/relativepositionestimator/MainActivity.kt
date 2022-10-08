package net.iobb.koheinoapp.relativepositionestimator

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
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
    private var currentSamplingItem: MutableState<SamplingItem?> = mutableStateOf(null)

    private var sensorAcceleration: FloatArray? = null
    private var sensorRotationMatrix: FloatArray? = null

    private var samplingMode = mutableStateOf(SamplingMode.Paused)

    override fun onResume() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (accelSensor == null || gyroSensor == null) {
            throw Exception("このデバイスは計測に必要なセンサを搭載していません")
        }

        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL)

        estimator = Estimator()

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
                    Column {
                        Text("a_x: ${currentSamplingItem.value?.getAX()}\n" +
                                "a_y: ${currentSamplingItem.value?.getAY()}\n" +
                                "a_z: ${currentSamplingItem.value?.getAZ()}")
                        Button(
                            onClick = {
                                samplingMode.value = SamplingMode.Correcting
                                estimator.samplingsForCorrecting.clear()
                            },
                        ) {
                            if (samplingMode.value == SamplingMode.Correcting) {
                                Text(text = "補正中")
                            } else {
                                Text(text = "補正")
                            }
                        }
                        Text("補正値a_x: ${estimator.correction?.get(0)}\n" +
                                "補正値a_y: ${estimator.correction?.get(1)}\n" +
                                "補正値a_z: ${estimator.correction?.get(2)}")
                        Text("補正後a_x: ${(currentSamplingItem.value?.getAX() ?: 0f) + (estimator.correction?.get(0) ?: 0f)}\n" +
                                "補正後a_y: ${(currentSamplingItem.value?.getAY() ?: 0f) + (estimator.correction?.get(1) ?: 0f)}\n" +
                                "補正後a_z: ${(currentSamplingItem.value?.getAZ() ?: 0f) + (estimator.correction?.get(2) ?: 0f)}")
                    }
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> sensorAcceleration = event.values
            Sensor.TYPE_ROTATION_VECTOR -> {
                sensorRotationMatrix = FloatArray(16)
                SensorManager.getRotationMatrixFromVector(sensorRotationMatrix, event.values)
            }
        }

        if (sensorAcceleration != null && sensorRotationMatrix != null) {
            currentSamplingItem.value = SamplingItem(sensorAcceleration!!, sensorRotationMatrix!!)

            when(samplingMode.value){
                SamplingMode.Correcting -> {
                    estimator.samplingsForCorrecting.add(currentSamplingItem.value!!)
                    if(estimator.samplingsForCorrecting.size >= 1000){
                        estimator.calcCorrectionVector()
                        samplingMode.value = SamplingMode.Paused
                    }
                }
            }

            sensorAcceleration = null
            sensorRotationMatrix = null
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}

enum class SamplingMode{
    Correcting, Sampling, Paused
}