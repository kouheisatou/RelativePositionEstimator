package net.iobb.koheinoapp.relativepositionestimator

private const val OUTLIER_THRESHOLD = 0.1
private const val NUMBER_OF_CORRECTION_SAMPLING = 1000

private const val NUMBER_OF_RETAINING_SAMPLINGS = 100

class Estimator {

    private val samplings = mutableListOf<SamplingItem>()
    private val samplingsForCorrecting = mutableListOf<SamplingItem>()

    var correctionVector: FloatArray? = null
    val currentVelocity = floatArrayOf(0f, 0f, 0f)
    val currentPosition = floatArrayOf(0f, 0f, 0f)

    fun addSampling(samplingItem: SamplingItem) {
        samplings.add(samplingItem)

        currentVelocity[0] += (samplingItem.getAX() + (correctionVector?.get(0) ?: 0f))
        currentVelocity[1] += (samplingItem.getAY() + (correctionVector?.get(1) ?: 0f))
        currentVelocity[2] += (samplingItem.getAZ() + (correctionVector?.get(2) ?: 0f))

        currentPosition[0] += currentVelocity[0]
        currentPosition[1] += currentVelocity[1]
        currentPosition[2] += currentVelocity[2]

        if (samplings.size > NUMBER_OF_RETAINING_SAMPLINGS) {
            samplings.removeAt(0)
        }
    }

    fun resetCurrentPosition(){
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