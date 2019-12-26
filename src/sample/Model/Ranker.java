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
    String pathToPosting;

    public Ranker(String pathToDictionary, String pathToPosting) {
        this.pathToDictionary = pathToDictionary;
        this.pathToPosting = pathToPosting;
    }

    private void DictionaryToPostings(String termIdentifier){
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(this.pathToDictionary));
            String line = br.readLine();
            while(line!=null){
                String[] splitLine = StringUtils.split(",");
                if (StringUtils.equals(termIdentifier, splitLine[0])){
                    int pointerToLine = Integer.parseInt(splitLine[3]); // extracting the pointer
                    String lineData = this.getPostingLine(pointerToLine);
                }
                else{
                    line = br.readLine();
                }
            }

        }catch(IOException e){
            System.out.println(e);
        }
    }
    private String getPostingLine(int pointerToLine){
        BufferedReader bufferedReader = null;
        try{
            bufferedReader = new BufferedReader(new FileReader(this.pathToPosting));
            String line = bufferedReader.readLine();
            int currentLineNum = 0;
            while(line!=null){
                if(currentLineNum==pointerToLine){
                    return line;
                }
                line = bufferedReader.readLine();
                currentLineNum += 1;
            }

        }catch(IOException e){
            System.out.println(e);
        }
        return null;
    }
    private String[] getAllTermDocsData(String termDocDataAsString){
        return null;
    }

}
