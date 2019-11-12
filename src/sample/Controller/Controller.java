package sample.Controller;

import sample.Model.MyModel;
import sample.View.MyView;

import java.util.Observable;
import java.util.Observer;

public class Controller extends Observable implements Observer {
    private MyModel model;
    private MyView view;
    public String path;

    public Controller(MyModel model, MyView view){
        this.model=model;
        this.view=view;
    }
    @Override
    public void update(Observable o, Object arg) {
        switch ((String)arg){
            case "path":
                model.testInitFileOfTest(path);
        }
    }

    public void setPath(String path) {
        this.path=path;
    }
}
