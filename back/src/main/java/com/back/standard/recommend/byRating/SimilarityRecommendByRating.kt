package com.back.standard.recommend.byRating

import com.back.standard.recommend.util.CosineSimilarityCalcer
import com.back.standard.recommend.util.SimilarityCalcer
import com.back.standard.recommend.util.Vector

class SimilarityRecommendByRating(
    private val calcer: SimilarityCalcer = CosineSimilarityCalcer()
) {

    data class Rating(
        val reviewerId: Long,
        val subjectId: Long,
        val rating: Float
    )

    private val ratingMatrix = HashMap<Long, Vector>()

    fun clear() {
        ratingMatrix.clear()
    }

    fun setData(reviews: List<Rating>) {
        reviews.forEach { review ->
            ratingMatrix.getOrPut(review.reviewerId) { Vector() }
                .putValue(review.subjectId, review.rating.toDouble())
        }
    }

    fun getRecommendList(targetUserId: Long, referenceCnt: Int, maxRecommend: Int): List<Long> {
        val targetUser = ratingMatrix[targetUserId] ?: return emptyList()
        if (targetUser.isEmpty()) return emptyList()

        ratingMatrix.values.forEach { it.subtractionValue(it.getAverageValue()) }

        val similarList = calcer.getSimilarList(targetUser, ratingMatrix)

        val recommends = HashMap<Long, Double>()
        val alreadyRead = targetUser.getLabels()

        for (i in 0 until minOf(referenceCnt, similarList.size)) {
            val similarScore = similarList[i].score

            val compareReviewList = ratingMatrix.getValue(similarList[i].id).getVectorElementList()

            compareReviewList
                .filter { it.label !in alreadyRead }
                .forEach { rating ->
                    val score = rating.value * similarScore
                    val existing = recommends[rating.label]
                    if (existing != null && existing <= score) return@forEach
                    recommends[rating.label] = score
                }
        }

        return recommends.entries
            .sortedByDescending { it.value }
            .take(maxRecommend)
            .map { it.key }
    }
}