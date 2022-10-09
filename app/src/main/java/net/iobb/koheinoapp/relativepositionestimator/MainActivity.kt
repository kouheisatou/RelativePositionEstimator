package net.iobb.koheinoapp.relativepositionestimator

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import net.iobb.koheinoapp.relativepositionestimator.ui.theme.RelativePositionEstimatorTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var estimator: Estimator
    private var currentSamplingItem: MutableState<SamplingItem?> = mutableStateOf(null)

    private var sensorAcceleration: FloatArray? = null
    private var sensorRotationMatrix: FloatArray? = null

    private var samplingMode = mutableStateOf(SamplingMode.Paused)
    private var displayingGraphType = mutableStateOf(GraphType.Acceleration)

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
                        Text(
                            "a_x: ${currentSamplingItem.value?.getAX()}\n" +
                                    "a_y: ${currentSamplingItem.value?.getAY()}\n" +
                                    "a_z: ${currentSamplingItem.value?.getAZ()}"
                        )
                        Row {
                            Button(
                                onClick = {
                                    samplingMode.value = SamplingMode.Correcting
                                    estimator.resetCorrection()
                                },
                                enabled = samplingMode.value != SamplingMode.Correcting
                            ) {
                                if (samplingMode.value == SamplingMode.Correcting) {
                                    Text(text = "補正中")
                                } else {
                                    Text(text = "補正")
                                }
                            }
                            Button(onClick = {
                                estimator.resetCorrection()
                            }) {
                                Text("補正値をリセット")
                            }
                        }
                        Text(
                            "補正値a_x: ${estimator.correctionVector?.get(0)}\n" +
                                    "補正値a_y: ${estimator.correctionVector?.get(1)}\n" +
                                    "補正値a_z: ${estimator.correctionVector?.get(2)}"
                        )
                        var correctedAX = (currentSamplingItem.value?.getAX()
                            ?: 0f) + (estimator.correctionVector?.get(0) ?: 0f)
                        var correctedAY = (currentSamplingItem.value?.getAY()
                            ?: 0f) + (estimator.correctionVector?.get(1) ?: 0f)
                        var correctedAZ = (currentSamplingItem.value?.getAZ()
                            ?: 0f) + (estimator.correctionVector?.get(2) ?: 0f)
                        Text(
                            "補正後a_x: $correctedAX\n" +
                                    "補正後a_y: $correctedAY\n" +
                                    "補正後a_z: $correctedAZ"
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("位置推定")
                            Switch(
                                checked = samplingMode.value == SamplingMode.Sampling,
                                onCheckedChange = { checked ->
                                    samplingMode.value = if (checked) {
                                        SamplingMode.Sampling
                                    } else {
                                        SamplingMode.Paused
                                    }

                                },
                            )
                            Button(onClick = {
                                estimator.resetCurrentPosition()
                            }) {
                                Text(text = "リセット")
                            }
                        }

                        Row {
                            Button(
                                onClick = {
                                    displayingGraphType.value = GraphType.Acceleration
                                },
                                enabled = displayingGraphType.value != GraphType.Acceleration
                            ) {
                                Text("Acceleration")
                            }
                            Button(
                                onClick = {
                                    displayingGraphType.value = GraphType.Velocity
                                },
                                enabled = displayingGraphType.value != GraphType.Velocity
                            ) {
                                Text("Velocity")
                            }
                            Button(
                                onClick = {
                                    displayingGraphType.value = GraphType.Position
                                },
                                enabled = displayingGraphType.value != GraphType.Position
                            ) {
                                Text("Position")
                            }
                        }

                        when(displayingGraphType.value){
                            GraphType.Acceleration -> {
                                Graph(
                                    x = correctedAX,
                                    y = correctedAY,
                                    modifier = Modifier.fillMaxSize(),
                                    ratio = 1000f,
                                )
                            }
                            GraphType.Velocity -> {
                                Graph(
                                    x = estimator.currentVelocity[0],
                                    y = estimator.currentVelocity[1],
                                    modifier = Modifier.fillMaxSize(),
                                    ratio = 1000f,
                                )
                            }
                            GraphType.Position -> {
                                Graph(
                                    x = estimator.currentPosition[0],
                                    y = estimator.currentPosition[1],
                                    modifier = Modifier.fillMaxSize(),
                                    ratio = 1000f,
                                )
                            }
                        }
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

            when (samplingMode.value) {
                SamplingMode.Correcting -> {
                    estimator.addSamplingForCorrecting(
                        samplingItem = currentSamplingItem.value!!,
                        onSamplingFulled = {
                            estimator.calcCorrectionVector()
                            samplingMode.value = SamplingMode.Paused
                        },
                    )
                }
                SamplingMode.Sampling -> {
                    estimator.addSampling(currentSamplingItem.value!!)
                }
            }

            sensorAcceleration = null
            sensorRotationMatrix = null
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}

@Composable
fun Graph(
    x: Float,
    y: Float,
    modifier: Modifier,
    ratio: Float,
) {
    Canvas(modifier = modifier) {
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2)
        )
        drawLine(
            color = Color.Black,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height)
        )
        drawPoints(
            points = listOf(Offset(size.width / 2 + y * ratio, size.height / 2 + x * ratio)),
            pointMode = PointMode.Points,
            color = Color.Red,
            strokeWidth = 10f,
        )
    }
}

enum class SamplingMode {
    Correcting, Sampling, Paused
}

enum class GraphType {
    Acceleration, Velocity, Position
}