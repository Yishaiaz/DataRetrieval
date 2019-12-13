package sample.View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import sample.Controller.Controller;
import sample.Model.MyModel;

import javax.swing.*;
import java.io.*;
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
    private String dictionaryContent="";
    private long timeOfProcess=0;

    @FXML
    public javafx.scene.control.Button btn_browse;
    public javafx.scene.control.Button btn_parse;
    public javafx.scene.control.CheckBox stemming_cp;
    public javafx.scene.control.TextField txtField_postingFilesInput;
    public javafx.scene.control.TextField txtField_corpusPath;
    public javafx.scene.control.TextArea textAreaDic1;

    public void chooseCorpusPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        File f = chooser.showDialog(null);
        txtField_corpusPath.appendText(f.getPath());
        if (f != null) {
            controller.setCorpusPath(f.getPath());
            controller.update(controller, "corpusPath");
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR, "Not found file.");
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK) {
                a.close();
            }
        }
    }

    public void choosePostingFilesPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        File f = chooser.showDialog(null);
        txtField_postingFilesInput.appendText(f.getPath());
        if (f != null) {
            controller.setPostingFilesPath(f.getPath());
            controller.update(controller, "postingFilesPath");
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR, "Not found file.");
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK) {
                a.close();
            }
        }
    }

    public void activate() {

        if (!txtField_postingFilesInput.getText().equals("") && !txtField_corpusPath.getText().equals("")) {
            //clean posting file path before starting.
            cleanPostingFilePath();
            long start_time = System.currentTimeMillis();

            if (stemming_cp.isSelected()) {
                controller.update(controller, "parseWithStemming");
            } else {
                controller.update(controller, "parseWithoutStemming");
            }

               timeOfProcess= ((System.currentTimeMillis() - start_time)/60000);

            showResults(timeOfProcess);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please fill up all fields before start.");
            alert.showAndWait();
            }

        }

    private void showResults(long timeOfProcess) {
        try {
            Path pathToDocsInfo = Paths.get(Paths.get("").toAbsolutePath().toString()+File.separator+"DocsInfo.txt");
            long numOfDocs = Files.lines(pathToDocsInfo).count();

            Path pathToDic = Paths.get(txtField_corpusPath.getText()+File.separator+"DictionaryTest.txt");
            long numOfTerms = Files.lines(pathToDic).count();

            StrBuilder content= new StrBuilder ();
            content.append("Time: " + timeOfProcess+ System.lineSeparator());
            content.append("num of docs: " + numOfDocs+System.lineSeparator());
            content.append("num of terms: "+ numOfTerms+ System.lineSeparator());

            //show result
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText(content.toString());

            alert.showAndWait();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void reset() {
        cleanPostingFilePath();
        txtField_postingFilesInput.clear();
        txtField_corpusPath.clear();
        stemming_cp.setSelected(false);
    }

    public void cleanPostingFilePath() {
        //get all paths of Posting Files into 'pathsToPostFiles'.
        File folder = new File(txtField_postingFilesInput.getText());
        ArrayList<String> pathsToPostFiles = new ArrayList<>();
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


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("Path doesn't exist.");
        //delete all files from Disk.
        for (String path : pathsToPostFiles) {
            if (path.endsWith(".txt")) {
                File file = new File(path);
                file.delete();
            }
        }

    }


    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void loadDictionary() {
        if (!txtField_postingFilesInput.getText().equals("") && !txtField_corpusPath.getText().equals("")) {
            String dictionaryPath = txtField_corpusPath.getText();
            File dictionary = new File(dictionaryPath + File.separator + "DictionaryTest.txt");
            StrBuilder dictionaryContent = new StrBuilder();
            String str = "";
            try {
                BufferedReader br = new BufferedReader(new FileReader(dictionary));
                while ((str = br.readLine()) != null) {
                    str = str.substring(0, str.lastIndexOf(',')); // remove from presentation pointer to line
                    str = str.substring(0, str.lastIndexOf(',')); // remove from presentation df
                    dictionaryContent.append(str + System.lineSeparator());
                }

                br.close();
                this.dictionaryContent = dictionaryContent.toString();
                System.out.println("finish loading");
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Dictionary not found.");

                alert.showAndWait();
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please fill up all fields before loading dictionary.");
            alert.showAndWait();
        }

    }

    public void presentDictionary() throws IOException {

        if (dictionaryContent.equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please load dictionary before present it.");

            alert.showAndWait();
        } else {
            FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("Dictionary.fxml"));
            Parent root = fxmlLoader1.load();
            Stage secondaryStage = new Stage();
            secondaryStage.setTitle("Dictionary");
            Scene scene = new Scene(root, 1060, 600);
            secondaryStage.setScene(scene);
            secondaryStage.show();
            textAreaDic1 = (javafx.scene.control.TextArea) scene.lookup("#textAreaDic1");
            textAreaDic1.setScrollLeft(1);
            textAreaDic1.setWrapText(true);
            textAreaDic1.setText(dictionaryContent);
        }
    }
}
