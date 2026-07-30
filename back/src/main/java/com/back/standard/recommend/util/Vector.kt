package com.back.standard.recommend.util

class Vector {

    data class VectorElement(
        val label: Long,
        val value: Double
    )

    private val values = HashMap<Long, Double>()
    private var sum = 0.0

    fun getLabels(): Set<Long> = values.keys

    fun getVectorElementList(): List<VectorElement> =
        values.entries.map { VectorElement(it.key, it.value) }

    fun putValue(label: Long, value: Double) {
        values[label]?.let { sum -= it }
        values[label] = value
        sum += value
    }

    fun subtractionValue(value: Double): Vector {
        values.replaceAll { _, oldValue -> oldValue - value }
        return this
    }

    fun getAverageValue(): Double = sum / values.size

    fun isEmpty(): Boolean = values.isEmpty()

    companion object {
        @JvmStatic
        fun hadamardProduct(v1: Vector, v2: Vector): Vector {
            val v = Vector()

            v1.values.forEach { (key, oldValue) ->
                val other = v2.values[key] ?: return@forEach
                v.putValue(key, oldValue * other)
            }

            return v
        }
    }
}