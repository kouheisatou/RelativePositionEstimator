package net.iobb.koheinoapp.relativepositionestimator

private const val OUTLIER_THRESHOLD = 0.1
private const val NUMBER_OF_CORRECTION_SAMPLING = 1000

class Estimator {

    private val samplings = mutableListOf<SamplingItem>()
    private val samplingsForCorrecting = mutableListOf<SamplingItem>()

    var correctionVector: FloatArray? = null

    fun addLogItem() {

    }

    fun addSamplingForCorrecting(samplingItem: SamplingItem, onSamplingFulled: () -> Unit){
        if (samplingItem.getAX() > OUTLIER_THRESHOLD || samplingItem.getAY() > OUTLIER_THRESHOLD || samplingItem.getAZ() > OUTLIER_THRESHOLD) return
        samplingsForCorrecting.add(samplingItem)

        if(samplingsForCorrecting.size > NUMBER_OF_CORRECTION_SAMPLING){
            onSamplingFulled()
        }
    }

    fun resetCorrection(){
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