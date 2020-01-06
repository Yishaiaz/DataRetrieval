package sample.Model.RankingAlgorithms;

import org.apache.commons.lang3.Pair;

import java.util.*;

public class BM25RankingAlgorithm extends IRankingAlgorithm{
    private int totalNumOfDocs;
    private double avgDocLength;
    public enum IdfFormula {
        OKAPIREGULAR,
        FLOORCONST,
        NONNEGATIVE
    }
    private IdfFormula useFormala;
    private double possibleConst;

    public BM25RankingAlgorithm(int totalNumOfDocs, double avgDocLength, IdfFormula idfFormula) {
        this.totalNumOfDocs = totalNumOfDocs;
        this.avgDocLength = avgDocLength;
        this.useFormala = idfFormula;
        this.possibleConst = -0.01;
    }

    @Override
    public Map<String, Double> rank(ArrayList<Pair<String, double[]>> allDocsToTermsValues) {
        Map<String, Double> docScores = new HashMap<String, Double>();
        for (int i = 0; i < allDocsToTermsValues.size(); i++) {
            String docName = allDocsToTermsValues.get(i).left;
            double[] docToTermValues = allDocsToTermsValues.get(i).right;
            double docNewScore = this.calcSingleTermScoreForDoc((int)docToTermValues[0],
                    docToTermValues[1],
                    docToTermValues[2],
                    (int)docToTermValues[3],
                    (int)docToTermValues[4]);
            if(docScores.containsKey(docName)){
                docScores.replace(docName, docScores.get(docName)+docNewScore);
            }
            else{
                docScores.put(docName, docNewScore);
            }
        }
        return docScores;
    }
    private double calcSingleTermScoreForDoc(int termFreqInDoc, double kParam, double bParam, int numberOfDocContainTerm, int docLength){
        double idfRes = calcIDF(numberOfDocContainTerm);
        double freq = ((termFreqInDoc) *(kParam + 1)/ (termFreqInDoc + kParam*((1-bParam)+bParam*(docLength/this.avgDocLength))));
        return idfRes * freq;

    }

    /**
     * this function can
     * @param numberOfDocsContainingTerm
     * @return
     */
    private double calcIDF(int numberOfDocsContainingTerm){
        switch(this.useFormala){
            case OKAPIREGULAR:
                return Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5));
            case FLOORCONST:
                return Math.max(this.possibleConst, Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5)));
            case NONNEGATIVE:
                return Math.max(0,Math.log((this.totalNumOfDocs - numberOfDocsContainingTerm + 0.5)/ (this.totalNumOfDocs + 0.5)));
        }
        return 0;
    }

}
