package examschd.controller;

import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.Enrollment;
import examschd.model.Student;

import examschd.service.readers.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulingController {

    @FXML private DatePicker        startDatePicker;
    @FXML private DatePicker        endDatePicker;
    @FXML private AnchorPane        rootPane;
    @FXML private GridPane          scheduleGrid;
    @FXML private Button            openFiltersBtn;
    @FXML private TextField         studentSearchField;
    @FXML private ComboBox<Integer> studentCombo;
    @FXML private Button            showStudentBtn;
    @FXML private TextField         classroomSearchField;
    @FXML private ComboBox<String>  classroomCombo;
    @FXML private Button            showClassroomBtn;

    private List<Classroom>  allClassrooms;
    private List<Enrollment> allEnrollments;
    private List<Student>    allStudentsList;
    private List<Course>     allCourses;

    private ObservableList<Integer> studentIds     = FXCollections.observableArrayList();
    private ObservableList<String>  classroomNames = FXCollections.observableArrayList();

    private File classroomsFile;
    private File coursesFile;
    private File enrollmentsFile;
    private File studentsFile;

    // Takvimde her gün için kolonları tutmak için
    private Map<LocalDate, VBox> dayColumnMap = new HashMap<>();

    @FXML
    public void initialize() {
        setupListeners();
    }

    public void initData(File classroomsFile,
                         File coursesFile,
                         File enrollmentsFile,
                         File studentsFile) {

        this.classroomsFile  = classroomsFile;
        this.coursesFile     = coursesFile;
        this.enrollmentsFile = enrollmentsFile;
        this.studentsFile    = studentsFile;

        System.out.println("paths of csv files passed are:");
        System.out.println(" - classrooms:  " + classroomsFile.getPath());
        System.out.println(" - courses:     " + coursesFile.getPath());
        System.out.println(" - enrollments: " + enrollmentsFile.getPath());
        System.out.println(" - students:    " + studentsFile.getPath());

        try {
            allClassrooms   = ClassroomCsvReader.read(classroomsFile.getAbsolutePath());
            allCourses      = CourseCsvReader.read(coursesFile.getAbsolutePath());
            allEnrollments  = EnrollmentCsvReader.read(enrollmentsFile.getAbsolutePath());
            allStudentsList = StudentCsvReader.read(studentsFile.getAbsolutePath());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        for (Student s : allStudentsList)
            studentIds.add(s.getId());

        for (Classroom c : allClassrooms)
            classroomNames.add(c.getName());

        studentCombo.setItems(studentIds);
        classroomCombo.setItems(classroomNames);

        setupSearchFilters();
    }

    private void renderEmptySchedule(LocalDate start, LocalDate end) {

        scheduleGrid.getChildren().clear();
        scheduleGrid.getColumnConstraints().clear();
        scheduleGrid.getRowConstraints().clear();
        dayColumnMap.clear();

        // Tek satır: gün kolonları
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        scheduleGrid.getRowConstraints().add(row);

        LocalDate d = start;
        int colIndex = 0;

        while (!d.isAfter(end)) {

            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            scheduleGrid.getColumnConstraints().add(cc);

            // Her gün için bir VBox kolon
            VBox dayBox = new VBox(8);
            dayBox.setStyle("-fx-background-color: #F8F8F8; -fx-padding: 10; -fx-background-radius: 8;");
            dayBox.setFillWidth(true);

            Label header = new Label(d.toString());
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
            dayBox.getChildren().add(header);

            scheduleGrid.add(dayBox, colIndex, 0);
            dayColumnMap.put(d, dayBox);

            d = d.plusDays(1);
            colIndex++;
        }
    }

    private void setupSearchFilters() {

        // STUDENT → Integer olduğu için düzeltildi
        studentSearchField.textProperty().addListener((obs, old, val) -> {
            String q = val.trim();

            if (q.isEmpty()) {
                studentCombo.setItems(studentIds);
                return;
            }

            if (!q.matches("\\d+")) {
                studentCombo.setItems(studentIds);
                return;
            }

            studentCombo.setItems(studentIds.filtered(id -> (id + "").contains(q)));
        });

        // CLASSROOM SEARCH
        classroomSearchField.textProperty().addListener((obs, old, val) -> {
            String q = val.toLowerCase();
            classroomCombo.setItems(
                    classroomNames.filtered(name -> name.toLowerCase().contains(q))
            );
        });
    }

    private void setupListeners() {

        // STUDENT SCHEDULE (GridPane’a göre güncellendi)
        showStudentBtn.setOnAction(e -> {
            Integer studentId = studentCombo.getValue();
            if (studentId == null) return;

            System.out.println("Loading schedule for student: " + studentId);
            // İleride burada renderEmptySchedule + exam yerleştirme yapılacak
        });

        // CLASSROOM SCHEDULE
        showClassroomBtn.setOnAction(e -> {
            String room = classroomCombo.getValue();
            if (room == null) return;

            System.out.println("Loading schedule for classroom: " + room);
            // İleride burada renderEmptySchedule + exam yerleştirme yapılacak
        });

        // FILTER BUTTON
        openFiltersBtn.setOnAction(e -> openFiltersPopup());
    }

    // -----------------------------------------------------------
    // FILTER POPUP
    // -----------------------------------------------------------
    private void openFiltersPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/examschd/fxml/filter_settings.fxml"));
            Parent root = loader.load();

            FilterSettingsController ctrl = loader.getController();
            ctrl.setCourses(allCourses);

            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            if (start != null && end != null)
                ctrl.setExamDateRange(start, end);

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Filter Settings");
            popup.setScene(new Scene(root));
            popup.showAndWait();

            Map<LocalDate, Boolean> allowedDays = ctrl.getExamAllowedDays();

            if (allowedDays != null) {
                System.out.println("=== ALLOWED EXAM DAYS ===");
                allowedDays.forEach((day, ok) ->
                        System.out.println(day + " → " + (ok ? "Exam OK" : "NO EXAM")));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
