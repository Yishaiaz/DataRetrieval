package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Controller.Controller;
import sample.Model.*;
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
      launch(args);



//        String ZabaryFileAddress = "/Users/yishaiazabary/Desktop/University/שנה ד/DataRetrieval/corpus";
//        String SababiFileAddress = "C:\\Users\\Sahar Ben Baruch\\Desktop\\corpus";
//        CorpusHandler manger = new CorpusHandler(SababiFileAddress);
//        manger.initListOfFilesPaths();
//        try {
//            manger.findTextInDocs();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

//


  //    DocIndexer indexer=new DocIndexer();
       // new File(Paths.get("").toAbsolutePath().toString()+"\\tempFiles").mkdir();
      //  indexer.mergeTwoDocuments(Paths.get("").toAbsolutePath().toString()+"\\tempFiles\\FBIS3-1.txt","tempFiles\\FBIS3-2.txt");


        System.exit(0);

    }
}






