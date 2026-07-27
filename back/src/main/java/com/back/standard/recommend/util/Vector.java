package com.back.standard.recommend.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Vector {

    public record VectorElement(
            long label,
            double value
    ) {
    }

    private final Map<Long, Double> values = new HashMap<>();
    private double sum = 0;

    public Set<Long> getLabels() {
        return values.keySet();
    }

    public List<VectorElement> getVectorElementList() {
        return values.entrySet().stream()
                .map(entry ->
                        new VectorElement(entry.getKey(), entry.getValue()))
                .toList();
    }

    public void putValue(long label, double value) {
        if (values.containsKey(label)) {
            sum -= values.get(label);
        }
        values.put(label, value);
        sum += value;
    }

    public Vector subtractionValue(double value) {
        values.replaceAll((key, oldValue) -> oldValue - value);
        return this;
    }

    public static Vector hadamardProduct(Vector v1, Vector v2) {
        Vector v = new Vector();

        v1.values.forEach((key, oldValue) -> {
            if (!v2.values.containsKey(key)) {
                return;
            }
            v.putValue(key, oldValue * v2.values.get(key));

        });

        return v;
    }

    public double getAverageValue() {
        return sum / values.size();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

}
