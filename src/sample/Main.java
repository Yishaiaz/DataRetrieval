package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Controller.Controller;
import sample.Model.*;
import sample.Model.Indexer.DocIndexer;
import sample.View.MyView;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("View/sample.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Search Engine");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
        MyView view = fxmlLoader.getController();
        MyModel model = new MyModel();
        Controller controller = new Controller(model, view);
        model.addObserver(controller);
        view.setController(controller);
    }


    public static void main(String[] args) {
  //   launch(args);



      DocIndexer indexer=new DocIndexer("C:\\Users\\Sahar Ben Baruch\\Desktop\\DataRetrieval\\temp");
        indexer.mergeTwoDocuments("C:\\Users\\Sahar Ben Baruch\\Desktop\\1129222140.txt","C:\\Users\\Sahar Ben Baruch\\Desktop\\-1415959223.txt");


        System.exit(0);

    }
}






