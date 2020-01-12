package sample.Controller;

import sample.Model.MyModel;
import sample.View.MyView;

import java.util.Observable;
import java.util.Observer;

/**
 * controller part of MVC
 */
public class Controller extends Observable implements Observer {
    private MyModel model;
    private MyView view;
    public String corpusPath;
    public String postingFilesPath;
    public String queryPath;
    public String freeTypingQuery;
    public Boolean withStemming;
   // public Boolean withSemantic;
    public int withSemantic;
    private String resultPath;

    public Controller(MyModel model, MyView view){
        this.model=model;
        this.view=view;
    }
    @Override
    /**
     * function update calls to function from model.
     */
    public void update(Observable o, Object arg) {
        switch ((String)arg){
            case "corpusPath":
                model.testInitFileOfTest(corpusPath);
                break;
            case "corpusPath2":
                model.createCorpusHandler(corpusPath);
                break;
            case "postingFilesPath":
                model.setPostingFilesPath(postingFilesPath);
                break;
            case "resultPathUpdate":
                model.setResultPath(resultPath);
                break;
            case "parseWithStemming":
                model.startParse (true);
                break;
            case "parseWithoutStemming":
                model.startParse (false);
                break;
            case "search":
                model.setIsWithSemantic(withSemantic);
                model.search(queryPath);
                break;
            case "updateStemming":
                model.withStemming=true;
                break;
            case "updateNoStemming":
                model.withStemming=false;
                break;
            case "searchFreeTyping":
                try {
                    model.setIsWithSemantic(withSemantic);
                    model.searchFreeTyping(freeTypingQuery);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    /**
     * set corpus path from gui.
     * @param corpusPath
     */
    public void setCorpusPath(String corpusPath) {
        this.corpusPath = corpusPath;
    }
    /**
     * set posting path from gui.
     * @param postingFilesPath
     */
    public void setPostingFilesPath(String postingFilesPath) {this.postingFilesPath=postingFilesPath;}

    public void setQueryPath(String queryPath) {
        this.queryPath=queryPath;
    }

    public void setFreeTypingQuery(String text) {
        this.freeTypingQuery=text;
    }

    public void setIsWithSemantic(int selected) {
        this.withSemantic=selected;
    }

    public void setResultsPath(String path) {
        this.resultPath=path;
    }
}
