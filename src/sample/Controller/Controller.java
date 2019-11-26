package sample.Controller;

import sample.Model.MyModel;
import sample.View.MyView;

import java.util.Observable;
import java.util.Observer;

public class Controller extends Observable implements Observer {
    private MyModel model;
    private MyView view;
    public String corpusPath;
    public String postingFilesPath;

    public Controller(MyModel model, MyView view){
        this.model=model;
        this.view=view;
    }
    @Override
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

        }
    }

    public void setCorpusPath(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public void setPostingFilesPath(String postingFilesPath) {this.postingFilesPath=postingFilesPath;}
}
