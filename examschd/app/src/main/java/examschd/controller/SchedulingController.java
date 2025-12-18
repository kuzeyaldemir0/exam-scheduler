package examschd.controller;

import examschd.model.*;
import examschd.model.StudentAssignment;
import examschd.service.ImportService;
import examschd.service.Scheduler;

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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class SchedulingController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private GridPane scheduleGrid;
    @FXML private VBox unscheduledSection;

    @FXML private Button openFiltersBtn;
    @FXML private Button applyDateRangeBtn;

    @FXML private TextField studentSearchField;
    @FXML private ComboBox<Integer> studentCombo;
    @FXML private Button showStudentBtn;

    @FXML private TextField classroomSearchField;
    @FXML private ComboBox<String> classroomCombo;
    @FXML private Button showClassroomBtn;
    @FXML private Button generateBtn;


    private List<Classroom> allClassrooms = new ArrayList<>();
    private List<Enrollment> allEnrollments = new ArrayList<>();
    private List<Student> allStudentsList = new ArrayList<>();
    private List<Course> allCourses = new ArrayList<>();

    private ObservableList<Integer> studentIds = FXCollections.observableArrayList();
    private ObservableList<String> classroomNames = FXCollections.observableArrayList();

    private Map<LocalDate, VBox> dayColumnMap = new LinkedHashMap<>();
    private ScheduleResult preparedScheduleResult;

    private ExamConfig userConfig = new ExamConfig();
    private final BooleanProperty dateRangeApplied = new SimpleBooleanProperty(false);
    private Integer filteredStudentId = null;  // Track student filter for room display

    private final ImportService importService = new ImportService();
    private final Scheduler scheduler = new Scheduler();



    @FXML
    public void initialize() {
        setupListeners();
        loadExistingDataOnStartup();

        // 🔒 Generate, Apply basılmadan aktif olmasın
        generateBtn.disableProperty().bind(
            startDatePicker.valueProperty().isNull()
                .or(endDatePicker.valueProperty().isNull())
                .or(dateRangeApplied.not())
        );
    }


    private void loadExistingDataOnStartup() {
        try {
            importService.loadExistingData();

            allStudentsList = importService.getAllStudents();
            allCourses = importService.getAllCourses();
            allClassrooms = importService.getAllClassrooms();
            allEnrollments = importService.getAllEnrollments();

            studentIds.clear();
            classroomNames.clear();

            for (Student s : allStudentsList) studentIds.add(s.getId());
            for (Classroom c : allClassrooms) classroomNames.add(c.getName());

            studentCombo.setItems(studentIds);
            classroomCombo.setItems(classroomNames);

            setupSearchFilters();
            initDefaultConfig();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initDefaultConfig() {
        Map<String, Integer> dur = new LinkedHashMap<>();
        for (Course c : allCourses)
            dur.put(c.getCourseName(), c.getDurationMinutes());

        userConfig.setCourseDurations(dur);
        userConfig.setMaxExamsPerDay(2);
        userConfig.setRoomTurnoverMinutes(15);
        userConfig.setStudentMinGapMinutes(90);
        userConfig.setExamStartHour(9);
        userConfig.setExamEndHour(21);
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
        int col = 0;

        while (!d.isAfter(end)) {

            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(220);
            cc.setHgrow(Priority.ALWAYS);
            scheduleGrid.getColumnConstraints().add(cc);

            VBox dayBox = new VBox(8);
            dayBox.setFillWidth(true);
            VBox.setVgrow(dayBox, Priority.ALWAYS);

            dayBox.setStyle(
                "-fx-background-color:#F8F8F8;" +
                "-fx-padding:10;" +
                "-fx-background-radius:8;"
            );

            Label header = new Label(d.toString());
            header.setStyle("-fx-font-weight:bold; -fx-font-size:14;");

            dayBox.getChildren().add(header);

            scheduleGrid.add(dayBox, col++, 0);
            dayColumnMap.put(d, dayBox);

            d = d.plusDays(1);
        }
    }


    private void clearRenderedSchedule() {
        for (VBox box : dayColumnMap.values()) {
            if (box.getChildren().size() > 1) {
                box.getChildren().remove(1, box.getChildren().size());
            }
        }
    }

        private void renderSchedule(Map<LocalDate, List<ExamSession>> schedule) {

        if (dayColumnMap.isEmpty()) {
            LocalDate s = startDatePicker.getValue();
            LocalDate e = endDatePicker.getValue();
            if (s != null && e != null) {
                renderEmptySchedule(s, e);
            }
        }

        clearRenderedSchedule();

        for (Map.Entry<LocalDate, List<ExamSession>> entry : schedule.entrySet()) {

            VBox dayBox = dayColumnMap.get(entry.getKey());
            if (dayBox == null) continue;

            List<ExamSession> sessions = entry.getValue();

            sessions.sort(Comparator.comparing(ExamSession::getTimeSlot));

            for (ExamSession session : sessions) {

                // Defensive null checks
                Course course = session.getCourse();
                if (course == null) {
                    System.out.println("ERROR: Session " + session.getSessionId() + " has null course!");
                    continue;
                }

                String courseName = course.getCourseName() != null ? course.getCourseName() : "[Unknown Course]";
                String time = session.getStartTime() != null ? session.getTimeSlot() : "[No Time]";
                int studentCount = course.getStudents() != null ? course.getStudents().size() : 0;

                Set<String> rooms = new LinkedHashSet<>();
                List<ExamPartition> partitions = session.getPartitions();
                if (partitions != null) {
                    // If filtering by student, only show their assigned classroom
                    if (filteredStudentId != null) {
                        for (ExamPartition p : partitions) {
                            if (p != null && p.getStudentAssignments() != null) {
                                for (StudentAssignment sa : p.getStudentAssignments()) {
                                    if (sa.getStudent() != null && sa.getStudent().getId() == filteredStudentId) {
                                        if (p.getClassroom() != null) {
                                            rooms.add(p.getClassroom().getName());
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // Show all classrooms
                        for (ExamPartition p : partitions) {
                            if (p != null && p.getClassroom() != null) {
                                rooms.add(p.getClassroom().getName());
                            }
                        }
                    }
                }

                // Debug logging for problematic sessions
                if (time.equals("[No Time]") || rooms.isEmpty()) {
                    System.out.println("WARN: Session " + courseName + " has issues - time=" + time +
                        ", partitions=" + (partitions != null ? partitions.size() : 0) +
                        ", rooms=" + rooms.size());
                }

                String text =
                    courseName + "\n" +
                    "⏰ " + time + "\n" +
                    "🏫 " + (rooms.isEmpty() ? "N/A" : String.join(", ", rooms));

                Label examLabel = new Label(text);
                examLabel.setWrapText(true);
                examLabel.setMaxWidth(Double.MAX_VALUE);
                examLabel.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

                examLabel.setStyle(
                    "-fx-background-color:#E3F2FD;" +
                    "-fx-padding:10;" +
                    "-fx-background-radius:6;" +
                    "-fx-font-size:12;"
                );

                Tooltip.install(
                    examLabel,
                    new Tooltip(
                        "Students: " + studentCount +
                        "\nDuration: " + session.getCourse().getDurationMinutes() + " min"
                    )
                );

                dayBox.getChildren().add(examLabel);
            }
        }
    }

    private void displayUnscheduledCourses(List<Course> unscheduledCourses) {
        unscheduledSection.getChildren().clear();

        if (unscheduledCourses == null || unscheduledCourses.isEmpty()) {
            Label success = new Label("All courses scheduled successfully!");
            success.setStyle(
                "-fx-text-fill: #2E7D32;" +
                "-fx-font-size: 14;" +
                "-fx-font-weight: bold;"
            );
            unscheduledSection.getChildren().add(success);
            return;
        }

        Label title = new Label("Unscheduled Courses (" + unscheduledCourses.size() + "):");
        title.setStyle(
            "-fx-text-fill: #C62828;" +
            "-fx-font-size: 14;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 0 5 0;"
        );
        unscheduledSection.getChildren().add(title);

        TextArea unscheduledList = new TextArea();
        unscheduledList.setEditable(false);
        unscheduledList.setWrapText(true);
        unscheduledList.setPrefRowCount(3);
        unscheduledList.setMaxHeight(100);

        StringBuilder text = new StringBuilder();
        for (Course course : unscheduledCourses) {
            text.append("- ").append(course.getCourseName())
                .append(" (").append(course.getStudents().size()).append(" students)\n");
        }
        unscheduledList.setText(text.toString());

        Tooltip extendTip = new Tooltip(
            "These courses could not fit in the current schedule.\n\n" +
            "Try extending the date range to provide more exam slots,\n" +
            "or reducing the number of exams per day in Filter Settings."
        );
        Tooltip.install(unscheduledList, extendTip);

        unscheduledSection.getChildren().add(unscheduledList);
    }


    public void initData(
            File classroomsFile,
            File coursesFile,
            File enrollmentsFile,
            File studentsFile
    ) {

        try {
            importService.loadExistingData();

            importService.importStudents(studentsFile.getAbsolutePath());
            importService.importCourses(coursesFile.getAbsolutePath());
            importService.importClassrooms(classroomsFile.getAbsolutePath());
            importService.importEnrollments(enrollmentsFile.getAbsolutePath());

            allStudentsList = importService.getAllStudents();
            allCourses = importService.getAllCourses();
            allClassrooms = importService.getAllClassrooms();
            allEnrollments = importService.getAllEnrollments();

            if (allStudentsList == null) allStudentsList = new ArrayList<>();
            if (allCourses == null) allCourses = new ArrayList<>();
            if (allClassrooms == null) allClassrooms = new ArrayList<>();
            if (allEnrollments == null) allEnrollments = new ArrayList<>();

            studentIds.clear();
            classroomNames.clear();

            for (Student s : allStudentsList) {
                studentIds.add(s.getId());
            }

            for (Classroom c : allClassrooms) {
                classroomNames.add(c.getName());
            }

            studentCombo.setItems(studentIds);
            classroomCombo.setItems(classroomNames);

            setupSearchFilters();
            initDefaultConfig();

            System.out.println("Data imported and UI refreshed");
            System.out.println("Exam hours set to: " + userConfig.getExamStartHour() + ":00 - " + userConfig.getExamEndHour() + ":00");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showHelp() {
        Alert helpDialog = new Alert(Alert.AlertType.INFORMATION);
        helpDialog.setTitle("How to Use Exam Scheduler");
        helpDialog.setHeaderText("Getting Started Guide");
        helpDialog.setContentText(
            "1. IMPORT DATA\n" +
            "   Click 'Import all data' to upload your CSV files:\n" +
            "   • students.csv - List of student IDs\n" +
            "   • courses.csv - List of course codes\n" +
            "   • classrooms.csv - Room names and capacities\n" +
            "   • enrollments.csv - Which students take which courses\n\n" +
            "2. SET DATE RANGE\n" +
            "   Select start and end dates for your exam period,\n" +
            "   then click 'Apply Date Range' to confirm.\n\n" +
            "3. CONFIGURE SETTINGS (Optional)\n" +
            "   Click 'Filter Settings' to adjust:\n" +
            "   • Max exams per student per day\n" +
            "   • Room turnover time between exams\n" +
            "   • Minimum gap between exams for students\n" +
            "   • Exam duration per course\n\n" +
            "4. GENERATE SCHEDULE\n" +
            "   Click 'Generate Schedule' to create the timetable.\n" +
            "   The calendar will show all scheduled exams.\n\n" +
            "5. VIEW SCHEDULES\n" +
            "   • Select a student ID to see their personal exam schedule\n" +
            "   • Select a classroom to see exams in that room"
        );
        helpDialog.getDialogPane().setMinWidth(500);
        helpDialog.showAndWait();
    }

    @FXML
    private void openImportPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/examschd/fxml/file_select.fxml")
            );

            Parent root = loader.load();
            FileSelectController ctrl = loader.getController();

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Import CSV Files");
            popup.setScene(new Scene(root));
            popup.setResizable(false);

            popup.showAndWait();

            if (ctrl.hasAllFilesSelected()) {
                initData(
                    ctrl.getClassroomsFile(),
                    ctrl.getCoursesFile(),
                    ctrl.getEnrollmentsFile(),
                    ctrl.getStudentsFile()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupListeners() {

        showStudentBtn.setOnAction(e -> {
            if (preparedScheduleResult == null) return;

            Integer id = studentCombo.getValue();
            if (id == null) return;

            filteredStudentId = id;  // Set filter for room display

            Map<LocalDate, List<ExamSession>> filtered = new LinkedHashMap<>();

            for (var entry : preparedScheduleResult.getSchedule().entrySet()) {
                List<ExamSession> list = new ArrayList<>();
                for (ExamSession s : entry.getValue()) {
                    boolean ok = s.getCourse().getStudents()
                                  .stream()
                                  .anyMatch(st -> st.getId() == id);
                    if (ok) list.add(s);
                }
                filtered.put(entry.getKey(), list);
            }

            renderSchedule(filtered);
        });

        showClassroomBtn.setOnAction(e -> {
            if (preparedScheduleResult == null) return;

            String room = classroomCombo.getValue();
            if (room == null) return;

            filteredStudentId = null;  // Clear student filter

            Map<LocalDate, List<ExamSession>> filtered = new LinkedHashMap<>();

            for (var entry : preparedScheduleResult.getSchedule().entrySet()) {
                List<ExamSession> list = new ArrayList<>();
                for (ExamSession s : entry.getValue()) {
                    boolean ok = s.getPartitions().stream()
                                  .anyMatch(p ->
                                      p.getClassroom() != null &&
                                      p.getClassroom().getName().equals(room)
                                  );
                    if (ok) list.add(s);
                }
                filtered.put(entry.getKey(), list);
            }

            renderSchedule(filtered);
        }); 

        startDatePicker.valueProperty().addListener((obs, oldV, newV) -> {
            dateRangeApplied.set(false);
        });

        endDatePicker.valueProperty().addListener((obs, oldV, newV) -> {
            dateRangeApplied.set(false);
        });

        openFiltersBtn.setOnAction(e -> openFiltersPopup());
        applyDateRangeBtn.setOnAction(e -> applyDateRange());
    }


    private void setupSearchFilters() {

        studentSearchField.textProperty().addListener((o,a,b) ->
            studentCombo.setItems(
                studentIds.filtered(id -> b.isEmpty() || id.toString().contains(b))
            )
        );

        classroomSearchField.textProperty().addListener((o,a,b) ->
            classroomCombo.setItems(
                classroomNames.filtered(n -> n.toLowerCase().contains(b.toLowerCase()))
            )
        );
    }

    @FXML
    private void applyDateRange() {

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null || end.isBefore(start)) {
            return;
        }

        renderEmptySchedule(start, end);
        dateRangeApplied.set(true);
    }


    @FXML
    private void onGenerateSchedule() {
        if (!dateRangeApplied.get()) {
            System.out.println("Please click Apply Date Range first.");
            return;
        }

        filteredStudentId = null;  // Clear any student filter

        preparedScheduleResult = scheduler.generateSchedule(
            allStudentsList,
            allCourses,
            allClassrooms,
            allEnrollments,
            userConfig,
            startDatePicker.getValue(),
            endDatePicker.getValue()
        );

        renderSchedule(preparedScheduleResult.getSchedule());
        displayUnscheduledCourses(preparedScheduleResult.getUnscheduledCourses());
    }

    private void openFiltersPopup() {
        try {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/examschd/fxml/filter_settings.fxml"));
            Parent root = loader.load();

            FilterSettingsController ctrl = loader.getController();

            ctrl.loadSavedDurations(allCourses);

            ctrl.loadExtraSettings(
                userConfig.getMaxExamsPerDay(),
                userConfig.getRoomTurnoverMinutes(),
                userConfig.getStudentMinGapMinutes(),
                userConfig.getExamStartHour(),
                userConfig.getExamEndHour()
            );

            

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Filter Settings");
            popup.setScene(new Scene(root));
            popup.showAndWait();

            userConfig = ctrl.buildConfig();
            preparedScheduleResult = null;

            if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
                renderEmptySchedule(
                    startDatePicker.getValue(),
                    endDatePicker.getValue()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
