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
            case "postingFilesPath":
                model.setPostingFilesPath(postingFilesPath);
                break;
            case "parseWithStemming":
                model.startParse (true);
                break;
            case "parseWithoutStemming":
                model.startParse (false);
                break;
            case "search":
                model.search(queryPath);

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
}
