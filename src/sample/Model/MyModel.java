package sample.Model;

import java.io.FileNotFoundException;
import java.util.Observable;

public class MyModel extends Observable {
    private String path="";
    CorpusHandler corpusHandler;

    public void testInitFileOfTest(String path){
        corpusHandler=new CorpusHandler(path);
        corpusHandler.initListOfFilesPaths();
        try {
            corpusHandler.findTextInDocs();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
