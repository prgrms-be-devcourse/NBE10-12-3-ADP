package com.back.standard.recommend.byRating;

import com.back.standard.recommend.util.CosineSimilarityCalcer;
import com.back.standard.recommend.util.SimilarityCalcer;
import com.back.standard.recommend.util.Vector;

import java.util.*;
import static com.back.standard.recommend.util.Vector.VectorElement;
import static com.back.standard.recommend.util.SimilarityCalcer.Similar;

public class SimilarityRecommendByRating {

    public record Rating(
            long reviewerId,
            long subjectId,
            float rating
    ) { }

    private final Map<Long, Vector> ratingMatrix;
    private SimilarityCalcer calcer = new CosineSimilarityCalcer();

    public SimilarityRecommendByRating() {
        ratingMatrix = new HashMap<>();
    }

    public SimilarityRecommendByRating(SimilarityCalcer calcer) {
        ratingMatrix = new HashMap<>();
        this.calcer = calcer;
    }

    public void clear() {
        ratingMatrix.clear();
    }

    public void setData(List<Rating> reviews) {

        reviews.forEach(review -> {
            if (!ratingMatrix.containsKey(review.reviewerId())) {
                ratingMatrix.put(review.reviewerId(), new Vector());
            }
            ratingMatrix.get(review.reviewerId()).putValue(
                    review.subjectId(), review.rating());

        });

    }

    public List<Long> getRecommendList(long targetUserId, int referenceCnt, int maxRecommend) {

        Vector targetUser = ratingMatrix.getOrDefault(targetUserId, null);

        if (targetUser == null) return List.of();
        if (targetUser.isEmpty()) return List.of();

        ratingMatrix.replaceAll((_, oldValue) ->
                oldValue.subtractionValue(oldValue.getAverageValue()));

        List<Similar> similarList = calcer.getSimilarList(targetUser, ratingMatrix);

        Map<Long, Double> recommends = new HashMap<>();
        Set<Long> alreadyRead = targetUser.getLabels();

        for (int i = 0; i < referenceCnt && i < similarList.size(); i++) {

            double similarScore = similarList.get(i).score();

            List<VectorElement> compareReviewList =
                    ratingMatrix.get(similarList.get(i).id()).getVectorElementList();

            compareReviewList.stream()
                    .filter(rating -> !alreadyRead.contains(rating.label()))
                    .forEach(rating -> {
                        double score = rating.value() * similarScore;
                        if (recommends.containsKey(rating.label())
                                && recommends.get(rating.label()) <= score) {
                            return;
                        }
                        recommends.put(rating.label(), score);
                    });

        }

        return recommends
                .entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(maxRecommend)
                .map(Map.Entry::getKey)
                .toList();
    }

}
