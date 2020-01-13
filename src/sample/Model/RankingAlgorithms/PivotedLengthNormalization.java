package sample.Model.RankingAlgorithms;

import org.apache.commons.lang3.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PivotedLengthNormalization extends IRankingAlgorithm {
    private int totalNumOfDocs;
    private double avgDocLength;
    private BM25RankingAlgorithm.IdfFormula useFormala;
    private double possibleConst;

    public PivotedLengthNormalization(int totalNumOfDocs, double avgDocLength, BM25RankingAlgorithm.IdfFormula idfFormula) {
        this.totalNumOfDocs = totalNumOfDocs;
        this.avgDocLength = avgDocLength;
    }

    public Map<String, Double> rank(ArrayList<Pair<String, double[]>> allDocsToTermsValues) {
        Map<String, Double> docScores = new HashMap<String, Double>();
        double bParam = 0.75; // 1 > bParam > 0
        double termWeightInFormula = 0.1;
        double tfIDFWeightInFormula = 1 - termWeightInFormula;
        for (int i = 0; i < allDocsToTermsValues.size(); i++) {
            String docName = allDocsToTermsValues.get(i).left;
            double[] docToTermValues = allDocsToTermsValues.get(i).right;
            double termFreqInDoc = (int)docToTermValues[0]; //term_freq
            double termWeightInDoc = docToTermValues[1]; // term weight
            double numberOfDocsContainingTerm = docToTermValues[3]; // number of docs containing term
            double docLength = docToTermValues[4]; // doc length
//                    docToTermValues[2], // bparam
//            squaredTermInDocs+= Math.pow(termFreqInDoc, 2);
//            squaredTermInQuery += Math.pow()
            double curr_score = 0;
            try{
                curr_score = docScores.get(docName);
            }catch (Exception e){
                curr_score = 0;
            }

            double innerNom = Math.log(1+ Math.log(1+termFreqInDoc));
            double innerDinom = 1- bParam + bParam* docLength/this.avgDocLength;

            curr_score += termWeightInFormula*(termWeightInDoc)+tfIDFWeightInFormula*(termFreqInDoc* (innerNom / innerDinom) * Math.log10((this.totalNumOfDocs+1)/(numberOfDocsContainingTerm)));
            docScores.put(docName, curr_score);
        }

        return docScores;
    }

}
