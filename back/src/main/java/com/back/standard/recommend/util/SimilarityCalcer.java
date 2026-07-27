package com.back.standard.recommend.util;

import java.util.List;
import java.util.Map;

public interface SimilarityCalcer {

    record Similar (
            long id,
            double score
    ) { }

    void setVectorA(Vector vector);
    void setVectorB(Vector vector);
    double getSimilarity();

    List<Similar> getSimilarList(Vector target, Map<Long, Vector> matrix);
}
