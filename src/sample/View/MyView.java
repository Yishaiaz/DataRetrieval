package sample.View;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import sample.Controller.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyView {
    private Controller controller;


    @FXML
    public javafx.scene.control.Button btn_browse;
    public javafx.scene.control.Button btn_parse;
    public javafx.scene.control.CheckBox stemming_cp;
    public javafx.scene.control.TextField txtField_postingFilesInput;
    public javafx.scene.control.TextField txtField_corpusPath;

    public void chooseCorpusPath(){
        DirectoryChooser chooser=new DirectoryChooser();
        File f = chooser.showDialog(null);
        txtField_corpusPath.appendText(f.getPath());
        if (f!=null){
            controller.setCorpusPath(f.getPath());
            controller.update(controller,"corpusPath");
        }
        else{
            Alert a= new Alert(Alert.AlertType.ERROR,"Not found file.");
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK){
                a.close();
            }
        }
    }

    public void choosePostingFilesPath(){
        DirectoryChooser chooser=new DirectoryChooser();
        File f = chooser.showDialog(null);
        txtField_postingFilesInput.appendText(f.getPath());
        if (f!=null){
            controller.setPostingFilesPath(f.getPath());
            controller.update(controller,"postingFilesPath");
        }
        else{
            Alert a= new Alert(Alert.AlertType.ERROR,"Not found file.");
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK){
                a.close();
            }
        }
    }

    public void activate(){
        if (!txtField_postingFilesInput.getText().equals("") || !txtField_corpusPath.getText().equals("")) {

            if (stemming_cp.isSelected()) {
                controller.update(controller, "parseWithStemming");
            } else {
                controller.update(controller, "parseWithoutStemming");
            }
        }

        else{
            System.out.println("Please fill Corpus Path and Posting Files Path.");
        }
    }

    public void reset(){

        //get all paths of Posting Files into 'pathsToPostFiles'.
        File folder = new File(txtField_postingFilesInput.getText());
        ArrayList<String> pathsToPostFiles= new ArrayList<>();
        //check if path exist
        if (folder != null) {
            try (Stream<Path> walk = Files.walk(Paths.get(folder.getPath()))) {

                List<String> folders = walk.filter(Files::isDirectory)
                        .map(x -> x.toString()).collect(Collectors.toList());

                try (Stream<Path> walk2 = Files.walk(Paths.get(folders.get(0)))) {
                    //result contains all paths of files inside one folder
                    List<String> result = walk2.filter(Files::isRegularFile)
                            .map(x -> x.toString()).collect(Collectors.toList());

                    //insert all files' paths from folder to 'filesPath'
                    for (String r : result)
                        pathsToPostFiles.add(r);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                //    System.out.println(filesPaths.size());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("Path doesn't exist.");

        //delete all files from Disk.
        for (String path: pathsToPostFiles){
            File file= new File(path);
            file.delete();
        }

        txtField_postingFilesInput.clear();
        txtField_corpusPath.clear();
        stemming_cp.setSelected(false);
    }


    public void setController(Controller controller) {
        this.controller=controller;
    }
}
