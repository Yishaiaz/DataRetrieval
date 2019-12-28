package sample.View;


import javafx.collections.FXCollections;

import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;

import org.apache.commons.lang3.text.StrBuilder;
import sample.Controller.Controller;



import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * view according MVC
 */
public class MyView {
    private Controller controller;
    // private String dictionaryContent="";
    private ObservableList<String> dictionaryContent;
    private long timeOfProcess = 0;

    @FXML
    public javafx.scene.control.Button btn_browse;
    public javafx.scene.control.Button btn_parse;
    public javafx.scene.control.CheckBox stemming_cp;
    public javafx.scene.control.TextField txtField_postingFilesInput;
    public javafx.scene.control.TextField txtField_corpusPath;
    public javafx.scene.control.TextField txtField_queryPath;
    public javafx.scene.control.TextField txtField_freeSearch;
    //public javafx.scene.control.TextArea textAreaDic1;
    public ListView listView_dic;


    /**
     * function activate when press on 'browse' button
     * get from user path to corpus
     */
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

    /**
     * function activate when press on 'browse' button
     * get from user path to posting files
     */
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
    public void chooseQueryPath() {
        FileChooser chooser = new FileChooser();
        File f = chooser.showOpenDialog(null);
        txtField_queryPath.appendText(f.getPath());
        if (f == null) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Not found file.");
            Optional<ButtonType> result = a.showAndWait();
            if (result.get() == ButtonType.OK) {
                a.close();
            }
        }
    }

    /**
     * start process of preparing searching engine
     */
    public void activate() {

        //check both paths are fill up
        if (!txtField_postingFilesInput.getText().equals("") && !txtField_corpusPath.getText().equals("")) {

            long start_time = System.currentTimeMillis();

            if (stemming_cp.isSelected()) {
                controller.update(controller, "parseWithStemming");
            } else {
                controller.update(controller, "parseWithoutStemming");
            }

            timeOfProcess = ((System.currentTimeMillis() - start_time) / 60000);

            showResults(timeOfProcess);

        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please fill up all fields before start.");
            alert.showAndWait();
        }

    }

    /**
     * after indexing present info in pop up message.
     *
     * @param timeOfProcess
     */
    private void showResults(long timeOfProcess) {
        try {
            Path pathToDocsInfo = null;
            if (stemming_cp.isSelected()) {
                pathToDocsInfo = Paths.get(Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoStemming.txt");
            } else {
                pathToDocsInfo = Paths.get(Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoNoStemming.txt");
            }
            long numOfDocs = Files.lines(pathToDocsInfo).count();

            Path pathToDic = null;
            if (stemming_cp.isSelected()) {
                pathToDic = Paths.get(txtField_corpusPath.getText() + File.separator + "DictionaryStemming.txt");
            } else {
                pathToDic = Paths.get(txtField_corpusPath.getText() + File.separator + "DictionaryNoStemming.txt");
            }

            long numOfTerms = Files.lines(pathToDic).count();

            StrBuilder content = new StrBuilder();
            content.append("Time: " + timeOfProcess + System.lineSeparator());
            content.append("num of docs: " + numOfDocs + System.lineSeparator());
            content.append("num of terms: " + numOfTerms + System.lineSeparator());

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

    /**
     * activate by pressing 'reset' button
     * delete content of posting path and field from gui
     */
    public void reset() {
        cleanPostingFilePath();
        txtField_postingFilesInput.clear();
        txtField_corpusPath.clear();
        stemming_cp.setSelected(false);
        File docInfoStemming = new File(Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoStemming.txt");
        if (docInfoStemming.exists())
            docInfoStemming.delete();

        //delete docs info
        File docInfoNoStemming = new File(Paths.get("").toAbsolutePath().toString() + File.separator + "DocsInfoNoStemming.txt");
        if (docInfoNoStemming.exists())
            docInfoNoStemming.delete();

        //delete dics
        File dicStemming = new File(txtField_corpusPath.getText() + File.separator + "DictionaryStemming.txt");
        if (dicStemming.exists())
            dicStemming.delete();

        File dicNoStemming = new File(txtField_corpusPath.getText() + File.separator + "DictionaryNoStemming.txt");
        if (dicNoStemming.exists())
            dicNoStemming.delete();


    }

    /**
     * cleaning posting file path- delete all files ends with '.txt'
     */
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

    /**
     * activate by pressing 'load dictionary' button .
     * load dictionary from "DictionaryTest.txt" to RAM .
     */
    public void loadDictionary() {
        if (!txtField_postingFilesInput.getText().equals("") && !txtField_corpusPath.getText().equals("")) {
            String dictionaryPath = txtField_corpusPath.getText();
            File dictionary = null;
            if (stemming_cp.isSelected()) {
                dictionary = new File(dictionaryPath + File.separator + "DictionaryStemming.txt");
            } else {
                dictionary = new File(dictionaryPath + File.separator + "DictionaryNoStemming.txt");
            }
            //  StrBuilder dictionaryContent = new StrBuilder();
            dictionaryContent = FXCollections.observableArrayList();

            String str = "";
            try {
                BufferedReader br = new BufferedReader(new FileReader(dictionary));

                //present only 'term , how many times in all corpus'
                while ((str = br.readLine()) != null && (!str.equals(""))) {
                    str = str.substring(0, str.lastIndexOf(',')); // remove from presentation pointer to line
                    str = str.substring(0, str.lastIndexOf(',')); // remove from presentation df
                    // dictionaryContent.append(str + System.lineSeparator());
                    dictionaryContent.add(str);
                }


                br.close();
                System.out.println("finish loading");
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Dictionary not found.");

                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("Please fill up all fields before loading dictionary.");
            alert.showAndWait();
        }

    }

    /**
     * present dictionary from RAM in pop up window
     *
     * @throws IOException
     */
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
            Scene scene = new Scene(root, 1060, 370);
            secondaryStage.setScene(scene);
            secondaryStage.show();
            //  textAreaDic1 = (javafx.scene.control.TextArea) scene.lookup("#textAreaDic1");
            listView_dic = (javafx.scene.control.ListView) scene.lookup("#listView_dic");
            listView_dic.setItems(dictionaryContent);
//            textAreaDic1.setScrollLeft(1);
//            textAreaDic1.setWrapText(true);
//            textAreaDic1.setText(dictionaryContent);

        }
    }

    public void search() {
        //if both fields are empties.
        if (txtField_queryPath.getText().equals("") && txtField_freeSearch.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(null);
            alert.setContentText("For searching need to fill 'free typing search' or insert path for query file.");

            alert.showAndWait();
        }


        if (!txtField_queryPath.getText().equals("")) {
            String queryPath = txtField_queryPath.getText();
            controller.setQueryPath(queryPath);
            controller.update(controller, "search");
        }

        else if (!txtField_freeSearch.getText().equals("")){
            controller.setFreeTypingQuery(txtField_freeSearch.getText());
            controller.update(controller, "searchFreeTyping");
        }
    }


}
