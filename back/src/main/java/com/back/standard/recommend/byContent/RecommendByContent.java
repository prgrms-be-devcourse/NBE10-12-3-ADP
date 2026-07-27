package com.back.standard.recommend.byContent;

import com.back.standard.recommend.util.CosineSimilarityCalcer;
import com.back.standard.recommend.util.SimilarityCalcer;
import com.back.standard.recommend.util.Vector;

import java.util.*;

import static com.back.standard.recommend.util.SimilarityCalcer.Similar;

public class RecommendByContent {

    public record Content(
            Long id,
            String content
    ) { }

    private Map<Long, Vector> tfMap = new HashMap<>();
    private Map<Long, Integer> idfMap = new HashMap<>();

    private long totalCnt = 0;

    private SimilarityCalcer calcer = new CosineSimilarityCalcer();

    public RecommendByContent() {}
    public RecommendByContent(SimilarityCalcer calcer) { this.calcer = calcer; }

    private Vector getTFVector(String content) {

        totalCnt++;
        String[] words = content.split(" ");

        Map<Long, Integer> wordMap = new HashMap<>();

        for (String word : words) {
            long wordHash = word.hashCode();
            if (wordMap.containsKey(wordHash)) {
                wordMap.put(wordHash, wordMap.get(wordHash) + 1);
                continue;
            }
            wordMap.put(wordHash, 1);
            idfMap.put(wordHash, idfMap.getOrDefault(wordHash, 0) + 1);
        }

        Vector ret = new Vector();

        wordMap.forEach((key, value) ->
                ret.putValue(key, (float) value / wordMap.size()));

        return ret;
    }

    public void setData(List<Content> contents) {

        contents.forEach((content) -> {
            if (tfMap.containsKey(content.id())) return;
            tfMap.put(content.id(), getTFVector(content.content()));
        });

    }

    public List<Long> getRecommendList(Long id, int maxRecommends) {

        if (!tfMap.containsKey(id)) return List.of();

        Vector idf = new Vector();

        idfMap.forEach((key, value) -> {
            idf.putValue(key, Math.log((double) totalCnt / value));
        });

        Map<Long, Vector> tfIdf = new HashMap<>();

        tfMap.forEach((key, value) -> {
            tfIdf.put(key, Vector.hadamardProduct(value, idf));
        });

        List<Similar> similarList = calcer.getSimilarList(tfIdf.get(id), tfIdf);

        return similarList.stream().limit(maxRecommends).map(Similar::id).toList();
    }


}
