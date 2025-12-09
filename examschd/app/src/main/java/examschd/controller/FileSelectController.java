package examschd.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.File;

public class FileSelectController {

    @FXML private Label classroomsLabel;
    @FXML private Label coursesLabel;
    @FXML private Label enrollmentsLabel;
    @FXML private Label studentsLabel;

    @FXML private Label warningLabel;
    @FXML private HBox warningBox;

    private File classroomsFile;
    private File coursesFile;
    private File enrollmentsFile;
    private File studentsFile;

    private File selectCSV() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // Null yerine sahneyi kullanmak daha güvenli
        return chooser.showOpenDialog(classroomsLabel.getScene().getWindow());
    }

    private void clearWarning() {
        warningBox.setVisible(false);
        warningLabel.setText("");
    }

    @FXML
    private void chooseClassroomsFile() {
        classroomsFile = selectCSV();
        if (classroomsFile != null) {
            classroomsLabel.setText("✔ " + classroomsFile.getName());
            clearWarning();
        }
    }

    @FXML
    private void chooseCoursesFile() {
        coursesFile = selectCSV();
        if (coursesFile != null) {
            coursesLabel.setText("✔ " + coursesFile.getName());
            clearWarning();
        }
    }

    @FXML
    private void chooseEnrollmentsFile() {
        enrollmentsFile = selectCSV();
        if (enrollmentsFile != null) {
            enrollmentsLabel.setText("✔ " + enrollmentsFile.getName());
            clearWarning();
        }
    }

    @FXML
    private void chooseStudentsFile() {
        studentsFile = selectCSV();
        if (studentsFile != null) {
            studentsLabel.setText("✔ " + studentsFile.getName());
            clearWarning();
        }
    }

    @FXML
    private void continueToScheduling() {

        if (classroomsFile == null ||
            coursesFile == null ||
            enrollmentsFile == null ||
            studentsFile == null) {

            warningLabel.setText("Please select all files before continuing.");
            warningBox.setVisible(true);
            return;
        }

        clearWarning();

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/examschd/fxml/scheduling.fxml"));

            Parent root = loader.load();

            // Controller'a erişip dosyaları gönder
            SchedulingController controller = loader.getController();
            controller.initData(classroomsFile, coursesFile, enrollmentsFile, studentsFile);

            Stage stage = (Stage) classroomsLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
