package sample.Model.RankingAlgorithms;
import org.apache.commons.lang3.Pair;
import java.util.ArrayList;
import java.util.Map;

public abstract class IRankingAlgorithm {
    public Map<String, Double> rank(ArrayList<Pair<String, double[]>> allDocsToTermsValues){return null;};
}
