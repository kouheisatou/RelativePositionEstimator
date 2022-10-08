package net.iobb.koheinoapp.relativepositionestimator

class Estimator {

    val samplings = mutableListOf<SamplingItem>()
    val samplingsForCorrecting = mutableListOf<SamplingItem>()

    var correction: FloatArray? = null

    fun addLogItem(){

    }

    fun calcCorrectionVector(){
        val result = FloatArray(3)
        var sumX = 0f
        var sumY = 0f
        var sumZ = 0f
        for(sampling in samplingsForCorrecting){
            sumX += sampling.getAX()
            sumY += sampling.getAY()
            sumZ += sampling.getAZ()
        }
        result[0] = sumX / samplingsForCorrecting.size.toFloat() * -1f
        result[1] = sumY / samplingsForCorrecting.size.toFloat() * -1f
        result[2] = sumZ / samplingsForCorrecting.size.toFloat() * -1f

        correction = result
    }
}