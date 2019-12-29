package sample.Model.RankingAlgorithms;

import java.util.Dictionary;
import java.util.Enumeration;

public class BM25RankingAlgorithm extends IRankingAlgorithm{
    private int totalNumOfDocs;
    private double avgDocLength;

    public BM25RankingAlgorithm(int totalNumOfDocs, double avgDocLength) {
        this.totalNumOfDocs = totalNumOfDocs;
        this.avgDocLength = avgDocLength;
    }

    @Override
    public Dictionary rank() {
        return super.rank();
    }

    private double calcTotatlScoreForDoc(Dictionary<String, double[]> termsDataInDoc){
        double score = 0;
        for (Enumeration key = termsDataInDoc.keys(); key.hasMoreElements();){
            double[] singleTermDocData = termsDataInDoc.get(key);
            score += calcSingleTermScoreForDoc(
                    (int)(singleTermDocData[0]), //termFreqInDoc
                    singleTermDocData[1], //kparam
                    singleTermDocData[2], //bParam
                    (int)(singleTermDocData[3]), //numberOfDocContainTerm
                    (int)(singleTermDocData[4])//docLength
            );
        }
        return score;
    }
    private double calcSingleTermScoreForDoc(int termFreqInDoc, double kParam, double bParam, int numberOfDocContainTerm, int docLength){
        double idfRes = calcIDF(numberOfDocContainTerm);
        double freq = ((termFreqInDoc) *(kParam + 1)/ (termFreqInDoc + kParam*((1-bParam)+bParam*(docLength/this.avgDocLength))));
        return idfRes * freq;

    }

    private double calcIDF(int numberOfDocsContainingTerm){
        return Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5));
    }

}
