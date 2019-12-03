package sample.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;

public class DictionaryParser implements Runnable{


    private String pathToIndex;
    private String pathToDictionaryDirectory;

    public DictionaryParser(String pathToIndex, String pathToDictionaryDirectory) {
        this.pathToIndex = pathToIndex;
        this.pathToDictionaryDirectory = pathToDictionaryDirectory;
    }
    @Override
    public void run() {
        parseInvertedIndex();
    }

    public void parseInvertedIndex(){
        String singleTerm;
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.pathToIndex));
            String line = bufferedReader.readLine();
                        
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
