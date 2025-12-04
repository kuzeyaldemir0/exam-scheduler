package examschd.controller;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.File;

public class FileSelectController {

    @FXML
    private Label fileNameLabel;

    @FXML
    private Button chooseFileButton;

    @FXML
    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = chooser.showOpenDialog(null);
        if (file != null) {
            fileNameLabel.setText(file.getName());
        }
    }

    @FXML
    private void continueToScheduling() {
        // Sonraki ekrana geçme
    }
}
