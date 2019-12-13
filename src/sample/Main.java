package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.Controller.Controller;
import sample.Model.*;
import sample.Model.Indexer.DocIndexer;
import sample.View.MyView;

import java.util.Optional;

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
        SetStageCloseEvent(primaryStage, model);
    }

    private void SetStageCloseEvent(Stage primaryStage, MyModel model) {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    // user chose OK, close program
                    primaryStage.close();
                } else {
                    // user chose CANCEL or closed the dialog
                    windowEvent.consume();
                }
            }
        });
    }


    public static void main(String[] args) {
    launch(args);

//     DictionaryParser dp= new DictionaryParser("a","b","C:\\Users\\Sahar Ben Baruch\\Desktop\\temp","C:\\Users\\Sahar Ben Baruch\\Desktop\\testCorpus",true);
//
//     dp.run();
     System.exit(0);

    }
}






