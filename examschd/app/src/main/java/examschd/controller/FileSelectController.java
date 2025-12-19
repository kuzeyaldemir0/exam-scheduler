package examschd.controller;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;

import examschd.db.DBInitializer;
import examschd.dao.StudentDAO;
import examschd.dao.CourseDAO;
import examschd.dao.ClassroomDAO;
import examschd.dao.EnrollmentDAO;
import examschd.daoimpl.StudentDAOImpl;
import examschd.daoimpl.CourseDAOImpl;
import examschd.daoimpl.ClassroomDAOImpl;
import examschd.daoimpl.EnrollmentDAOImpl;

public class FileSelectController {

    @FXML private Label classroomsLabel;
    @FXML private Label coursesLabel;
    @FXML private Label enrollmentsLabel;
    @FXML private Label studentsLabel;

    @FXML private Label warningLabel;
    @FXML private HBox warningBox;

    @FXML private VBox dbStatusBox;
    @FXML private Label dbStudentsLabel;
    @FXML private Label dbCoursesLabel;
    @FXML private Label dbClassroomsLabel;
    @FXML private Label dbEnrollmentsLabel;

    private File classroomsFile;
    private File coursesFile;
    private File enrollmentsFile;
    private File studentsFile;

    // Initialize database
    public void initialize() {
        DBInitializer.initialize();
        checkAndDisplayDatabaseStatus();
    }

    /**
     * Checks if data exists in the database and displays a status message
     */
    private void checkAndDisplayDatabaseStatus() {
        try {
            // Create DAO instances to query the database
            StudentDAO studentDAO = new StudentDAOImpl();
            CourseDAO courseDAO = new CourseDAOImpl();
            ClassroomDAO classroomDAO = new ClassroomDAOImpl();
            EnrollmentDAO enrollmentDAO = new EnrollmentDAOImpl();

            // Get counts of existing data
            int studentCount = studentDAO.getAll().size();
            int courseCount = courseDAO.getAll().size();
            int classroomCount = classroomDAO.getAll().size();
            int enrollmentCount = enrollmentDAO.getAll().size();

            // If any data exists, show the status box
            if (studentCount > 0 || courseCount > 0 || classroomCount > 0 || enrollmentCount > 0) {
                dbStudentsLabel.setText("• " + studentCount + " students");
                dbCoursesLabel.setText("• " + courseCount + " courses");
                dbClassroomsLabel.setText("• " + classroomCount + " classrooms");
                dbEnrollmentsLabel.setText("• " + enrollmentCount + " enrollments");
                dbStatusBox.setVisible(true);
            }
        } catch (Exception e) {
            // If there's an error querying the database, just don't show the status
            System.err.println("Error checking database status: " + e.getMessage());
        }
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
}
