package com.back.standard.recommend.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.back.standard.recommend.util.Vector.VectorElement;

public class CosineSimilarityCalcer implements SimilarityCalcer {

    private double vectorASqrMagnitude;
    private double vectorBSqrMagnitude;

    private List<VectorElement> sortedVectorA;
    private List<VectorElement> sortedVectorB;

    private static List<VectorElement> getSortedVector(Vector vector) {
        return vector
                .getVectorElementList()
                .stream()
                .sorted(Comparator
                        .comparingLong(
                                VectorElement::label))
                .toList();
    }

    private static double getSqrMagnitude(List<VectorElement> ratings) {

        return ratings
                .stream()
                .mapToDouble(
                vectorElement -> vectorElement.value() * vectorElement.value())
                .sum();
    }

    public void setVectorA(Vector vector) {

        sortedVectorA = getSortedVector(vector);
        vectorASqrMagnitude = getSqrMagnitude(sortedVectorA);
    }

    public void setVectorB(Vector vector) {
        sortedVectorB = getSortedVector(vector);
        vectorBSqrMagnitude = getSqrMagnitude(sortedVectorB);
    }

    public double getSimilarity() {

        int refA = 0;
        int refB = 0;

        double ret = 0;

        while (refA < sortedVectorA.size() && refB < sortedVectorB.size()) {
            VectorElement ratingA = sortedVectorA.get(refA);
            VectorElement ratingB = sortedVectorB.get(refB);

            if (ratingA.label() < ratingB.label()) {
                refA++;
                continue;
            }
            if (ratingA.label() > ratingB.label()) {
                refB++;
                continue;
            }

            ret += ratingA.value() * ratingB.value();

            refA++;
            refB++;
        }

        return ret / Math.sqrt(vectorASqrMagnitude * vectorBSqrMagnitude);
    }


    public List<Similar> getSimilarList(Vector target, Map<Long, Vector> matrix) {

        List<Similar> similarList = new ArrayList<>();

        setVectorA(target);

        matrix.entrySet().stream()
                .filter(set -> {
                    if (set.getValue() == target) return false;
                    return !set.getValue().isEmpty();
                })
                .forEach(set -> {
                    setVectorB(set.getValue());
                    similarList.add(new Similar(set.getKey(), getSimilarity()));
                });

        similarList.sort(Comparator.comparingDouble(a -> -a.score()));

        return similarList;
    }
}
