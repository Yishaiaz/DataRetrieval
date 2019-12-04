package sample.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;

public class DictionaryParser implements Runnable{
    private String alphabetRangeStart;
    private String alphabetRangeEnd;
    private String pathToIndex;
    private String pathToDictionaryDirectory;

    public DictionaryParser(String alphabetRangeStart, String alphabetRangeEnd, String pathToIndex, String pathToDictionaryDirectory) {
        this.alphabetRangeStart = StringUtils.lowerCase(alphabetRangeStart);
        this.alphabetRangeEnd = StringUtils.lowerCase(alphabetRangeEnd);
        this.pathToIndex = pathToIndex;
        this.pathToDictionaryDirectory = pathToDictionaryDirectory;
    }

    @Override
    public void run() {
        parseInvertedIndex();
    }

    /**
     * runs from the first alphabet char until the final one NOT INCLUDING the last one.
     */
    public void parseInvertedIndex(){
        int postingLineNumber = 0;
        String singleTerm;
        int singleTermNumberOfDocsAppearance;
        int singleTermTotalNumberOfApperance;
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.pathToIndex));
            String line = bufferedReader.readLine();
            // get to first line of the alphabet range given to this DictionaryParser
            while(!(StringUtils.lowerCase(line).charAt(0) >= this.alphabetRangeStart.charAt(0) && StringUtils.lowerCase(line).charAt(0) < this.alphabetRangeEnd.charAt(0)) ){
                line = bufferedReader.readLine();
                postingLineNumber+=1;
            }

            // read until you found the range end char or something 'above' it.
            while(!StringUtils.startsWith(StringUtils.lowerCase(line), this.alphabetRangeEnd) &&
                    StringUtils.lowerCase(line).charAt(0) < this.alphabetRangeEnd.charAt(0)){

                int termEndIndex =  StringUtils.indexOf(line,"|");

                singleTerm = StringUtils.substring(line,0, termEndIndex );

                try {
                    singleTermNumberOfDocsAppearance = Integer.parseInt(StringUtils.substring(line, termEndIndex+1, StringUtils.indexOf(line, "|", termEndIndex+1)));
                    // calculating entire appearances
                    int sum=0;
                    int currentIndex = termEndIndex+2;
                    while(currentIndex +1 < line.length()){
                        int singleDocStart = StringUtils.indexOf(line, "<", currentIndex)+1;
                        int singleDocEnd = StringUtils.indexOf(line, ">", currentIndex+1);
                        int amountInDoc = Integer.parseInt(StringUtils.split(StringUtils.substring(line, singleDocStart, singleDocEnd),",")[1]);
                        sum+=amountInDoc;
                        currentIndex = singleDocEnd;
                    }
                    singleTermTotalNumberOfApperance = sum;
                    // todo: here we have everything about the single term to write to the dictionary.
                    System.out.println(String.format("Term Name: %s - in Number Of Docs %d with total appearances %d line number in posting file %d", singleTerm, singleTermNumberOfDocsAppearance, singleTermTotalNumberOfApperance, postingLineNumber));
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
                line = bufferedReader.readLine();
                postingLineNumber+=1;
            }

                        
        }catch (IOException e){

        }
    }


    public String getPathToIndex() {
        return pathToIndex;
    }

    public String getPathToDictionaryDirectory() {
        return pathToDictionaryDirectory;
    }

    public void setPathToIndex(String pathToIndex) {
        this.pathToIndex = pathToIndex;
    }

    public void setPathToDictionaryDirectory(String pathToDictionaryDirectory) {
        this.pathToDictionaryDirectory = pathToDictionaryDirectory;
    }
}
