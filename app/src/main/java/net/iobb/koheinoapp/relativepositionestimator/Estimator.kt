package net.iobb.koheinoapp.relativepositionestimator

private const val OUTLIER_THRESHOLD = 0.1
private const val NUMBER_OF_CORRECTION_SAMPLING = 1000

private const val NUMBER_OF_RETAINING_SAMPLINGS = 100

class Estimator {

    private val samplings = mutableListOf<SamplingItem>()
    private val samplingsForCorrecting = mutableListOf<SamplingItem>()

    var correctionVector: FloatArray? = null
    val currentAcceleration = floatArrayOf(0f, 0f, 0f)
    val currentVelocity = floatArrayOf(0f, 0f, 0f)
    val currentPosition = floatArrayOf(0f, 0f, 0f)

    fun addSampling(samplingItem: SamplingItem) {
        val dt: Float =
            if (samplings.isNotEmpty()) {
                (samplings.last().samplingTimeMillis - samplingItem.samplingTimeMillis).toFloat() / 1000f
            } else {
                0f
            }

        currentAcceleration[0] = samplingItem.getAX() + (correctionVector?.get(0) ?: 0f)
        currentAcceleration[1] = samplingItem.getAX() + (correctionVector?.get(1) ?: 0f)
        currentAcceleration[2] = samplingItem.getAX() + (correctionVector?.get(2) ?: 0f)

        val aX: Float = currentAcceleration[0]
        val aY: Float = currentAcceleration[1]
        val aZ: Float = currentAcceleration[2]

        val vX0: Float = currentVelocity[0]
        val vY0: Float = currentVelocity[1]
        val vZ0: Float = currentVelocity[2]

        val x0: Float = currentPosition[0]
        val y0: Float = currentPosition[0]
        val z0: Float = currentPosition[0]

        currentVelocity[0] = aX * dt + vX0
        currentVelocity[1] = aX * dt + vY0
        currentVelocity[2] = aX * dt + vZ0

        currentPosition[0] = 0.5f * aX * dt * dt + vX0 * dt + x0
        currentPosition[1] = 0.5f * aY * dt * dt + vY0 * dt + y0
        currentPosition[2] = 0.5f * aZ * dt * dt + vZ0 * dt + z0

        println("$aX, $vX0, $x0, $dt")

        samplings.add(samplingItem)

        if (samplings.size > NUMBER_OF_RETAINING_SAMPLINGS) {
            samplings.removeAt(0)
        }
    }

    fun resetCurrentPosition() {
        currentVelocity[0] = 0f
        currentVelocity[1] = 0f
        currentVelocity[2] = 0f
        currentPosition[0] = 0f
        currentPosition[1] = 0f
        currentPosition[2] = 0f
    }

    fun addSamplingForCorrecting(samplingItem: SamplingItem, onSamplingFulled: () -> Unit) {
        if (samplingItem.getAX() > OUTLIER_THRESHOLD || samplingItem.getAY() > OUTLIER_THRESHOLD || samplingItem.getAZ() > OUTLIER_THRESHOLD) {
            println("outlier sampling : $samplingItem")
        } else {
            samplingsForCorrecting.add(samplingItem)

            if (samplingsForCorrecting.size > NUMBER_OF_CORRECTION_SAMPLING) {
                onSamplingFulled()
            }
        }
    }

    fun resetCorrection() {
        correctionVector = null
        samplingsForCorrecting.clear()
    }

    fun calcCorrectionVector() {
        val result = FloatArray(3)
        var sumX = 0f
        var sumY = 0f
        var sumZ = 0f
        for (sampling in samplingsForCorrecting) {
            sumX += sampling.getAX()
            sumY += sampling.getAY()
            sumZ += sampling.getAZ()
        }
        result[0] = sumX / samplingsForCorrecting.size.toFloat() * -1f
        result[1] = sumY / samplingsForCorrecting.size.toFloat() * -1f
        result[2] = sumZ / samplingsForCorrecting.size.toFloat() * -1f

        correctionVector = result
    }
}