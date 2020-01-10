package sample.Model;

import java.io.FileNotFoundException;
import java.util.Observable;

/**
 * model according MVC
 */
public class MyModel extends Observable {
    public boolean withStemming;
    CorpusHandler corpusHandler;
    public Boolean withSemantic;
    private String resultPath;

    /**
     * create corpus manger
     * @param path
     */
    public void testInitFileOfTest(String path){
        createCorpusHandler(path);
        corpusHandler.initListOfFilesPaths();


    }

    /**
     * start process of parsing
     * @param stemming
     */
    public void startParse(boolean stemming) {
        try {
            corpusHandler.findTextInDocs(stemming);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * setter
     * @param postingFilesPath
     */
    public void setPostingFilesPath(String postingFilesPath) {
        corpusHandler.setPostingFilesPath(postingFilesPath);
    }

    public void search(String queryPath) {
        corpusHandler.search(queryPath,withSemantic,resultPath,withStemming);
    }

    public void searchFreeTyping(String freeTypingQuery) throws Exception {
        corpusHandler.searchFreeTyping(freeTypingQuery,withSemantic,resultPath,withStemming);
    }

    public void setIsWithSemantic(Boolean withSemantic) {
        this.withSemantic=withSemantic;
    }

    public void setResultPath(String resultPath) {
        this.resultPath=resultPath;
    }

    public void createCorpusHandler(String corpusPath) {
        corpusHandler=new CorpusHandler(corpusPath);
    }
}
