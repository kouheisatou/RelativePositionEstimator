package net.iobb.koheinoapp.relativepositionestimator

import java.sql.Time

class SamplingItem(
    acceleration: FloatArray,
    rotationMatrix: FloatArray
) {
    private val acceleration = Matrix(4, 1, floatArrayOf(acceleration[0], acceleration[1], acceleration[2], 1f))
    private val samplingTimeMillis: Long = System.currentTimeMillis()
    private val rotationMatrix = Matrix(4, 4, rotationMatrix)
    private val accelerationByGrand = this.rotationMatrix * this.acceleration

    fun getAX(): Float = accelerationByGrand[0, 0]
    fun getAY(): Float = accelerationByGrand[0, 1]
    fun getAZ(): Float = accelerationByGrand[0, 2]

    override fun toString(): String {
        return "PositionSamplingLogItem{${Time(samplingTimeMillis)}, acceleration=$acceleration\naccelerationByGland=$accelerationByGrand\nrotationMatrix=$rotationMatrix}"
    }
}
