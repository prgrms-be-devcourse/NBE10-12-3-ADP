package com.back.standard.recommend.util

class CosineSimilarityCalcer : SimilarityCalcer {

    private var vectorASqrMagnitude = 0.0
    private var vectorBSqrMagnitude = 0.0

    private var sortedVectorA: List<Vector.VectorElement> = emptyList()
    private var sortedVectorB: List<Vector.VectorElement> = emptyList()

    override fun setVectorA(vector: Vector) {
        sortedVectorA = getSortedVector(vector)
        vectorASqrMagnitude = getSqrMagnitude(sortedVectorA)
    }

    override fun setVectorB(vector: Vector) {
        sortedVectorB = getSortedVector(vector)
        vectorBSqrMagnitude = getSqrMagnitude(sortedVectorB)
    }

    override fun getSimilarity(): Double {
        var refA = 0
        var refB = 0

        var ret = 0.0

        while (refA < sortedVectorA.size && refB < sortedVectorB.size) {
            val ratingA = sortedVectorA[refA]
            val ratingB = sortedVectorB[refB]

            if (ratingA.label < ratingB.label) {
                refA++
                continue
            }
            if (ratingA.label > ratingB.label) {
                refB++
                continue
            }

            ret += ratingA.value * ratingB.value

            refA++
            refB++
        }

        return ret / Math.sqrt(vectorASqrMagnitude * vectorBSqrMagnitude)
    }

    override fun getSimilarList(target: Vector, matrix: Map<Long, Vector>): List<SimilarityCalcer.Similar> {
        setVectorA(target)

        return matrix.entries
            .filter { it.value !== target && !it.value.isEmpty() }
            .map { entry ->
                setVectorB(entry.value)
                SimilarityCalcer.Similar(entry.key, getSimilarity())
            }
            .sortedByDescending { it.score }
    }

    companion object {
        private fun getSortedVector(vector: Vector): List<Vector.VectorElement> =
            vector.getVectorElementList().sortedBy { it.label }

        private fun getSqrMagnitude(elements: List<Vector.VectorElement>): Double =
            elements.sumOf { it.value * it.value }
    }
}