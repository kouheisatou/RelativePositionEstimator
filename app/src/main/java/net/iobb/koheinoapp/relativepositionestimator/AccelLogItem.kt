package net.iobb.koheinoapp.relativepositionestimator

import java.sql.Time
import java.util.Calendar

class AccelLogItem(
    acceleration: FloatArray,
    val rotationMatrix: FloatArray
) {
    private val time: Long = System.currentTimeMillis()
    private val aX: Float = acceleration[0]
    private val aY: Float = acceleration[1]
    private val aZ: Float = acceleration[2]

    override fun toString(): String {
        var s = ""
        rotationMatrix.forEach {
            s += "$it, "
        }
        return "AccelLogItem{${Time(time)}, a_x=$aX, a_y=$aY, a_z=$aZ, rotationMatrix=$s}"
    }
}