package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Controller.Controller;
import sample.Model.*;
import sample.Model.DataStructures.TermHashMapDataStructure;
import sample.Model.DataStructures.TermHashMapEntry;
import sample.Model.Indexer.DocIndexer;
import sample.Model.Number;
import sample.Model.Parser.DocParser;
import sample.View.MyView;

import java.io.*;
import java.nio.file.Paths;
import java.util.Observer;

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



//        String ZabaryFileAddress = "/Users/yishaiazabary/Desktop/University/שנה ד/DataRetrieval/corpus";
//        String SababiFileAddress = "C:\\Users\\Sahar Ben Baruch\\Desktop\\corpus";
//        CorpusHandler manger = new CorpusHandler(SababiFileAddress);
//        manger.initListOfFilesPaths();
//        try {
//            manger.findTextInDocs();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

//        Number numdd= new Number("10,123");   //10.123 K
        //      Number numdd= new Number("123 Thousand");    //123K
        //      Number numdd= new Number("1010.56");     //1.01 K
        //     Number numdd= new Number("10,123,000");   //10.123 M
        //       Number numdd= new Number("55 Million");   //55 M
        //       Number numdd= new Number("10,123,000,000");  //10.123 B
        //       Number numdd= new Number("55.0179 Million");   //55.017 M
        //       Number numdd= new Number("55 Billion");   //55 B
        //       Number numdd= new Number("55.0179 million U.S. dollars");  //55.017 M Dollars
        //      Number numdd= new Number("10,123,000,000");  //10.123 B
        //       Number numdd= new Number("1,120,000");   //1.12 M
        //       Number numdd= new Number("1,123,000");  //1.123 M
        //       Number numdd= new Number("1,000.23");  //1 K
        //       Number numdd= new Number("10,000.23");  //10K
// Number numdd= new Number("204");  //204
//        Number numdd= new Number("204.15");   //204.15
        //       Number numdd= new Number("204.1579");  //204.157
//        Number numdd= new Number("24 2/3");
        //       Number numdd= new Number("63.7413%");  //63.7413 %
        //       Number numdd= new Number("$450,000");  //450000 Dollars
        //    Number numdd= new Number("1,000,000 Dollars"); //1 M Dollars
        //   Number numdd= new Number("$100 million");  // 100 M Dollars
        //     Number numdd= new Number("20.6m Dollars");  //20.6 M Dollars
        //     Number numdd= new Number("20.6 Dollars");  //20.6 Dollars
        //     Number numdd= new Number("$201");  //201 Dollars
        //    Number numdd= new Number("$201000000");  //201  M Dollars
        //     Number numdd= new Number("$100 billion");  // 100000 M Dollars
        //       Number numdd= new Number("100 billion U.S. dollars");  //100000 M Dollars
        //    Number numdd= new Number("320 million U.S. dollars");  //320 M Dollars
        //   Number numdd= new Number("320 m Dollars");  //320 M Dollars
        //       Number numdd= new Number("$1.1 billion");  //1100 M Dollars
        //   Number numdd= new Number("100bn Dollars");  //100000 M Dollars
        //       Number numdd= new Number("123 Thousand dollars");   //123000 Dollars
        //      Number numdd= new Number("123.23 Thousand dollars");   //123230 Dollars
//        Number numdd= new Number("1 trillion dollars");  //--- not working yet !!


//            Word numdd=new Word("child");


//        numdd.prepareTerm();
//
//       System.out.println("Value is :" +numdd.content);
//        System.out.println("unit is :"+ numdd.unit);
//       System.out.println("mark is :"+ numdd.mark);

//        TermHashMapDataStructure structure= new TermHashMapDataStructure();
//        structure.insert("Child",7);
//        structure.insert("boy",9);
//        structure.insert("child",10);
//        structure.insert("boy",15);


      DocIndexer indexer=new DocIndexer();
       // new File(Paths.get("").toAbsolutePath().toString()+"\\tempFiles").mkdir();
        indexer.mergeTwoDocuments(Paths.get("").toAbsolutePath().toString()+"\\tempFiles\\FBIS3-1.txt","tempFiles\\FBIS3-2.txt");


        System.exit(0);

    }
}






