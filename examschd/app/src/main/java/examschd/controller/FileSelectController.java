package examschd.controller;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;

import examschd.db.DBInitializer;

public class FileSelectController {

    @FXML private Label classroomsLabel;
    @FXML private Label coursesLabel;
    @FXML private Label enrollmentsLabel;
    @FXML private Label studentsLabel;

    @FXML private Label warningLabel;
    @FXML private HBox warningBox;
    @FXML private javafx.scene.layout.VBox helpOverlay;

    private File classroomsFile;
    private File coursesFile;
    private File enrollmentsFile;
    private File studentsFile;

    // Initialize database
    public void initialize() {
        DBInitializer.initialize(); 
    }

    public boolean hasAllFilesSelected() {
        return classroomsFile != null &&
            coursesFile != null &&
            enrollmentsFile != null &&
            studentsFile != null;
    }

    public File getClassroomsFile() { return classroomsFile; }
    public File getCoursesFile() { return coursesFile; }
    public File getEnrollmentsFile() { return enrollmentsFile; }
    public File getStudentsFile() { return studentsFile; }


    private File selectCSV() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

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

        if (!hasAllFilesSelected()) {
            warningLabel.setText("Please select all files before continuing.");
            warningBox.setVisible(true);
            return;
        }

        clearWarning();

        Stage stage = (Stage) classroomsLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void showHelp() {
        helpOverlay.setVisible(true);
        helpOverlay.setManaged(true);
    }

    @FXML
    private void closeHelp() {
        helpOverlay.setVisible(false);
        helpOverlay.setManaged(false);
    }

}
