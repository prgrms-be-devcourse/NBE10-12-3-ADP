package com.back.standard.recommend.util

interface SimilarityCalcer {

    data class Similar(
        val id: Long,
        val score: Double
    )

    fun setVectorA(vector: Vector)
    fun setVectorB(vector: Vector)
    fun getSimilarity(): Double

    fun getSimilarList(target: Vector, matrix: Map<Long, Vector>): List<Similar>
}