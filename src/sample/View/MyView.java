package sample.View;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import sample.Controller.Controller;

import java.io.File;
import java.util.Optional;

public class MyView {
    private Controller controller;
    public FileChooser fileChooser;
    @FXML
    public javafx.scene.control.Button btn_browse;

    public void chooseCorpusPath(){
        DirectoryChooser chooser=new DirectoryChooser();
        File f = chooser.showDialog(null);
        if (f!=null){
            controller.setPath(f.getPath());
            controller.update(controller,"path");
        }
        else{
            Alert a= new Alert(Alert.AlertType.ERROR,"Not found file.");
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK){
                a.close();
            }
        }
    }

    public void setController(Controller controller) {
        this.controller=controller;
    }
}
