package sample.Model;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.Buffer;

public class DictionaryParser implements Runnable{
    private String alphabetRangeStart;
    private String alphabetRangeEnd;
    private String pathToIndex;
    private String pathToDictionaryDirectory;
    private boolean isNumericParser;

    public DictionaryParser(String alphabetRangeStart, String alphabetRangeEnd, String pathToIndex, String pathToDictionaryDirectory, Boolean numericParser) {
        this.alphabetRangeStart = StringUtils.lowerCase(alphabetRangeStart);
        this.alphabetRangeEnd = StringUtils.lowerCase(alphabetRangeEnd);
        this.pathToIndex = pathToIndex;
        this.pathToDictionaryDirectory = pathToDictionaryDirectory;
        this.isNumericParser = numericParser;
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
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.pathToDictionaryDirectory+"/DictionaryTest.txt"));
            String line = bufferedReader.readLine();
            bufferedWriter.write("Term Name, Docs Appearances, Total Corpus Appearances, line in posting file\n");
//            if(this.isNumericParser){
//                while(!(StringUtils.isNumeric(String.valueOf(line.charAt(0))))){
//                    line = bufferedReader.readLine();
//                    postingLineNumber+=1;
//                }
//            }else{
//                while(!(StringUtils.lowerCase(line).charAt(0) >= this.alphabetRangeStart.charAt(0) && StringUtils.lowerCase(line).charAt(0) <= this.alphabetRangeEnd.charAt(0)) ){
//                    line = bufferedReader.readLine();
//                    postingLineNumber+=1;
//                }
//            }
//            // get to first line of the alphabet range given to this DictionaryParser

            // read until you found the range end char or something 'above' it.
//            while((!this.isNumericParser && ( StringUtils.lowerCase(line).charAt(0) >= this.alphabetRangeStart.charAt(0) &&
//                    StringUtils.lowerCase(line).charAt(0) <= this.alphabetRangeEnd.charAt(0))) || (this.isNumericParser &&(StringUtils.isNumeric(String.valueOf(line.charAt(0)))))){
            while(line!=null){
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
                    bufferedWriter.write(String.format("%s,%d,%d,%d\n", singleTerm, singleTermNumberOfDocsAppearance, singleTermTotalNumberOfApperance, postingLineNumber));
//                    System.out.println(String.format("Term Name: %s - in Number Of Docs %d with total appearances %d line number in posting file %d", singleTerm, singleTermNumberOfDocsAppearance, singleTermTotalNumberOfApperance, postingLineNumber));
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
                line = bufferedReader.readLine();
                postingLineNumber+=1;
            }

                        
        }catch (IOException e){
            System.out.println(e.getMessage());
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
