package net.iobb.koheinoapp.relativepositionestimator

import java.sql.Time

class PositionSamplingLogItem(
    acceleration: FloatArray,
    rotationMatrix: FloatArray
) {
    private val acceleration = Matrix(4, 1, floatArrayOf(acceleration[0], acceleration[1], acceleration[2], 1f))
    private val samplingTimeMillis: Long = System.currentTimeMillis()
    private val rotationMatrix = Matrix(4, 4, rotationMatrix)
    private val accelerationByGrand = this.rotationMatrix * this.acceleration

    override fun toString(): String {
        return "AccelLogItem{${Time(samplingTimeMillis)}, acceleration=$acceleration\naccelerationByGland=$accelerationByGrand\nrotationMatrix=$rotationMatrix}"
    }
}
