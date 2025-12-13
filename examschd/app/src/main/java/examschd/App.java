package examschd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import examschd.db.DBInitializer;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/examschd/fxml/file_select.fxml"));
        DBInitializer.initialize();
        stage.setScene(new Scene(root));
        stage.setTitle("Exam Scheduler");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
