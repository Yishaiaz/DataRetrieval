package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Model.CorpusHandler;

import java.io.FileNotFoundException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("View/sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);

        CorpusHandler manger = new CorpusHandler("C:\\Users\\Sahar Ben Baruch\\Desktop\\corpus");
        manger.initListOfFilesPaths();
        try {
            manger.findTextInDocs();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
