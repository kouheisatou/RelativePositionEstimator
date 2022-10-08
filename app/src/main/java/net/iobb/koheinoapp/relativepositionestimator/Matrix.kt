package net.iobb.koheinoapp.relativepositionestimator

class Matrix(val rows: Int, val cols: Int) {
    private val elements = FloatArray(rows * cols) { 0f }

    constructor(rows: Int, cols: Int, values: FloatArray) : this(rows, cols) {

        if (rows * cols > values.size) throw Exception("初期値配列の長さが不足しています")
        if (rows * cols < values.size) throw Exception("初期値配列の長さオーバーしています")

        for (i in elements.indices) {
            elements[i] = values[i]
        }
    }

    constructor(rows: Int, cols: Int, init: (row: Int, col: Int) -> Float) : this(rows, cols) {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                this[row, col] = init(row, col)
            }
        }
    }

    operator fun get(row: Int, col: Int): Float {
        return elements[row * cols + col]
    }

    private operator fun set(row: Int, col: Int, value: Float) {
        elements[row * cols + col] = value
    }

    operator fun times(rhs: Matrix): Matrix {

        if (cols != rhs.rows) throw Exception("左値の列数と右値の行数は一致している必要があります")

        val result = Matrix(rows, rhs.cols)
        for (row in 0 until result.rows) {
            for (col in 0 until result.cols) {
                for (i in 0 until cols) {
                    result[row, col] += (this[row, i] * rhs[i, col])
                }
            }
        }
        return result
    }

    override fun toString(): String {
        var s = ""
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                s += "${this[row, col]}, "
            }
            if(row != rows -1){
                s += "\n"
            }
        }
        return s
    }
}

fun main() {
    val a = Matrix(2, 3) { row: Int, _: Int -> (row).toFloat() }
    println(a)
    val b = Matrix(3, 2) { _: Int, col: Int -> (col).toFloat() }
    println(b)
    println(a * b)
}