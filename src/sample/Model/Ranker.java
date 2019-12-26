package sample.Model;

import org.apache.commons.lang3.StringUtils;
import sample.Model.RankingAlgorithms.IRankingAlgorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;

public class Ranker {
    IRankingAlgorithm rankingAlgorithm;
    String pathToDictionary;

    public Ranker(String pathToDictionary) {
        this.pathToDictionary = pathToDictionary;
    }

    private void DictionaryToPostings(String termIdentifier){
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(this.pathToDictionary));
            String line = br.readLine();
            while(line!=null){
                String[] splitLine = StringUtils.split(" ");
                if (StringUtils.equals(termIdentifier, splitLine[0])){

                }
            }

        }catch(IOException e){
            System.out.println(e);
        }
    }
    private String[] getAllTermDocsData(String termDocDataAsString){
        return null;
    }

}
