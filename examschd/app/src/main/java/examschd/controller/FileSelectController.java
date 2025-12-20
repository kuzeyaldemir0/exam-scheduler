package examschd.controller;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.File;

import examschd.db.DBInitializer;

public class FileSelectController {

    @FXML private Label classroomsLabel;
    @FXML private Label coursesLabel;
    @FXML private Label enrollmentsLabel;
    @FXML private Label studentsLabel;

    @FXML private Label warningLabel;
    @FXML private HBox warningBox;
    @FXML private VBox helpOverlay;
    @FXML private Button gotItBtn;

    private File classroomsFile;
    private File coursesFile;
    private File enrollmentsFile;
    private File studentsFile;

    // Initialize database
    public void initialize() {
        DBInitializer.initialize();

        if (gotItBtn != null) {
            addPressedLikeHover(gotItBtn);
        }
    }

    private void addPressedLikeHover(Button btn) {

        Color normal = Color.web("#1976D2");
        Color hover  = Color.web("#125AA0");
        Duration dur = Duration.millis(140);

        btn.setBackground(
            new Background(new BackgroundFill(
                normal, new CornerRadii(10), Insets.EMPTY
            ))
        );

        btn.setOnMouseEntered(e ->
            animateBgColor(btn, normal, hover, dur)
        );

        btn.setOnMouseExited(e ->
            animateBgColor(btn, hover, normal, dur)
        );
    }

    private void animateBgColor(Button btn, Color from, Color to, Duration dur) {

        Transition t = new Transition() {
            {
                setCycleDuration(dur);
                setInterpolator(Interpolator.EASE_BOTH);
            }

            @Override
            protected void interpolate(double frac) {
                Color c = from.interpolate(to, frac);
                btn.setBackground(
                    new Background(new BackgroundFill(
                        c, new CornerRadii(10), Insets.EMPTY
                    ))
                );
            }
        };

        t.play();
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
