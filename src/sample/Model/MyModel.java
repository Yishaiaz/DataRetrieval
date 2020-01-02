package sample.Model;

import java.io.FileNotFoundException;
import java.util.Observable;

/**
 * model according MVC
 */
public class MyModel extends Observable {
    CorpusHandler corpusHandler;
    public Boolean withSemantic;

    /**
     * create corpus manger
     * @param path
     */
    public void testInitFileOfTest(String path){
        corpusHandler=new CorpusHandler(path);
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
        corpusHandler.search(queryPath,withSemantic);
    }

    public void searchFreeTyping(String freeTypingQuery) throws Exception {
        corpusHandler.searchFreeTyping(freeTypingQuery,withSemantic);
    }

    public void setIsWithSemantic(Boolean withSemantic) {
        this.withSemantic=withSemantic;
    }
}
