package sample.Model;

import java.io.FileNotFoundException;
import java.util.Observable;

public class MyModel extends Observable {
    CorpusHandler corpusHandler;

    public void testInitFileOfTest(String path){
        corpusHandler=new CorpusHandler(path);
        corpusHandler.initListOfFilesPaths();


    }

    public void startParse(boolean stemming) {
        try {
            corpusHandler.findTextInDocs(stemming);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setPostingFilesPath(String postingFilesPath) {
        corpusHandler.setPostingFilesPath(postingFilesPath);
    }
}
