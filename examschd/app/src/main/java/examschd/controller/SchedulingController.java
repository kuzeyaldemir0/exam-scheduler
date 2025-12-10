package examschd.controller;

import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.Enrollment;
import examschd.model.Student;
import examschd.model.ExamConfig;

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
import java.util.*;

public class SchedulingController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private AnchorPane rootPane;
    @FXML private GridPane scheduleGrid;

    @FXML private Button openFiltersBtn;
    @FXML private Button applyDateRangeBtn;

    @FXML private TextField studentSearchField;
    @FXML private ComboBox<Integer> studentCombo;
    @FXML private Button showStudentBtn;

    @FXML private TextField classroomSearchField;
    @FXML private ComboBox<String> classroomCombo;
    @FXML private Button showClassroomBtn;

    private List<Classroom> allClassrooms;
    private List<Enrollment> allEnrollments;
    private List<Student> allStudentsList;
    private List<Course> allCourses;

    private ObservableList<Integer> studentIds = FXCollections.observableArrayList();
    private ObservableList<String> classroomNames = FXCollections.observableArrayList();

    private Map<LocalDate, VBox> dayColumnMap = new HashMap<>();

    private ExamConfig userConfig = new ExamConfig();  

    @FXML
    public void initialize() {
        setupListeners();
    }

    public void initData(File classroomsFile,
                         File coursesFile,
                         File enrollmentsFile,
                         File studentsFile) {

        try {
            allClassrooms   = ClassroomCsvReader.read(classroomsFile.getAbsolutePath());
            allCourses      = CourseCsvReader.read(coursesFile.getAbsolutePath());
            allEnrollments  = EnrollmentCsvReader.read(enrollmentsFile.getAbsolutePath());
            allStudentsList = StudentCsvReader.read(studentsFile.getAbsolutePath());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        for (Student s : allStudentsList) studentIds.add(s.getId());
        for (Classroom c : allClassrooms) classroomNames.add(c.getName());

        studentCombo.setItems(studentIds);
        classroomCombo.setItems(classroomNames);

        setupSearchFilters();

        initDefaultConfig();
    }

    private void initDefaultConfig() {

        Map<String, Integer> dur = new LinkedHashMap<>();
        for (Course c : allCourses)
            dur.put(c.getCourseName(), c.getDurationMinutes());

        userConfig.setCourseDurations(dur);

        userConfig.setMaxExamsPerDay(2);
        userConfig.setBreakTimeBetweenExams(30);

    }

    private void renderEmptySchedule(LocalDate start, LocalDate end) {

        scheduleGrid.getChildren().clear();
        scheduleGrid.getColumnConstraints().clear();
        scheduleGrid.getRowConstraints().clear();
        dayColumnMap.clear();

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        scheduleGrid.getRowConstraints().add(row);

        LocalDate d = start;
        int colIndex = 0;

        while (!d.isAfter(end)) {

            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            scheduleGrid.getColumnConstraints().add(cc);

            VBox dayBox = new VBox(8);
            dayBox.setFillWidth(true);

            Label header = new Label(d.toString());
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

            boolean allowed = userConfig.getAllowedExamDays().getOrDefault(d, true);

            if (allowed) {
                dayBox.setStyle("-fx-background-color: #F8F8F8; -fx-padding: 10; -fx-background-radius: 8;");
            } else {
                dayBox.setStyle("-fx-background-color: #FFE5E5; -fx-padding: 10; -fx-background-radius: 8;");
            }

            dayBox.getChildren().add(header);

            scheduleGrid.add(dayBox, colIndex, 0);
            dayColumnMap.put(d, dayBox);

            d = d.plusDays(1);
            colIndex++;
        }
    }

    private void setupSearchFilters() {

        studentSearchField.textProperty().addListener((obs, old, val) -> {
            String q = val.trim();
            if (q.isEmpty() || !q.matches("\\d+")) {
                studentCombo.setItems(studentIds);
            } else {
                studentCombo.setItems(studentIds.filtered(id -> (id + "").contains(q)));
            }
        });

        classroomSearchField.textProperty().addListener((obs, old, val) -> {
            String q = val.toLowerCase();
            classroomCombo.setItems(
                    classroomNames.filtered(name -> name.toLowerCase().contains(q))
            );
        });
    }

    private void setupListeners() {

        showStudentBtn.setOnAction(e -> {
            Integer studentId = studentCombo.getValue();
            if (studentId == null) return;
            System.out.println("Loading schedule for student: " + studentId);
        });

        showClassroomBtn.setOnAction(e -> {
            String room = classroomCombo.getValue();
            if (room == null) return;
            System.out.println("Loading schedule for classroom: " + room);
        });

        openFiltersBtn.setOnAction(e -> openFiltersPopup());

        applyDateRangeBtn.setOnAction(e -> applyDateRange());
    }

    private void openFiltersPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/examschd/fxml/filter_settings.fxml"));
            Parent root = loader.load();

            FilterSettingsController ctrl = loader.getController();

            ctrl.loadSavedDurations(allCourses);

            ctrl.loadExtraSettings(
                    userConfig.getMaxExamsPerDay(),
                    userConfig.getBreakTimeBetweenExams()
            );

            if (startDatePicker.getValue() != null && endDatePicker.getValue() != null)
                ctrl.setExamDateRange(startDatePicker.getValue(), endDatePicker.getValue());

            if (!userConfig.getAllowedExamDays().isEmpty())
                ctrl.loadSavedExamDays(userConfig.getAllowedExamDays());

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Filter Settings");
            popup.setScene(new Scene(root));
            popup.showAndWait();

            userConfig = ctrl.buildConfig();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void applyDateRange() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null || end.isBefore(start)) {
            System.out.println("Invalid date range.");
            return;
        }

        if (userConfig.getAllowedExamDays().isEmpty()) {
            Map<LocalDate, Boolean> tmp = new LinkedHashMap<>();
            LocalDate d = start;
            while (!d.isAfter(end)) {
                tmp.put(d, true);
                d = d.plusDays(1);
            }
            userConfig.setAllowedExamDays(tmp);
        }

        renderEmptySchedule(start, end);
    }
}
