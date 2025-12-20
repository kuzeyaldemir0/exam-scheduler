package examschd.controller;

import examschd.model.Course;
import examschd.model.ExamConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class FilterSettingsController {

    /* ===================== FXML ===================== */

    @FXML private VBox courseDurationList;

    @FXML private Spinner<Integer> maxExamsSpinner;
    @FXML private Spinner<Integer> roomTurnoverSpinner;
    @FXML private Spinner<Integer> studentGapSpinner;
    @FXML private Spinner<Integer> examStartHourSpinner;
    @FXML private Spinner<Integer> examEndHourSpinner;

    // 🔹 HELP OVERLAY
    @FXML private VBox helpOverlay;

    /* ===================== STATE ===================== */

    private List<Course> courses = new ArrayList<>();
    private final Map<Course, Spinner<Integer>> durationSpinners = new LinkedHashMap<>();

    private int savedMaxExams = 2;
    private int savedRoomTurnover = 15;
    private int savedStudentGap = 90;
    private int savedExamStartHour = 9;
    private int savedExamEndHour = 21;

    /* ===================== INIT ===================== */

    @FXML
    public void initialize() {
        setupExtraSpinners();

        // 🔒 Help overlay başlangıçta tamamen kapalı
        if (helpOverlay != null) {
            helpOverlay.setVisible(false);
            helpOverlay.setManaged(false);
        }
    }

    private void setupExtraSpinners() {

        maxExamsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, savedMaxExams)
        );

        roomTurnoverSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, savedRoomTurnover)
        );

        studentGapSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 180, savedStudentGap)
        );

        examStartHourSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, savedExamStartHour)
        );

        examEndHourSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 23, savedExamEndHour)
        );
    }

    /* ===================== LOAD DATA ===================== */

    public void loadSavedDurations(List<Course> list) {
        courses = list;
        loadCourseDurationInputs();
    }

    private void loadCourseDurationInputs() {

        courseDurationList.getChildren().clear();
        durationSpinners.clear();

        for (Course c : courses) {

            Label lbl = new Label(c.getCourseName());
            lbl.setPrefWidth(250);

            Spinner<Integer> sp = new Spinner<>();
            sp.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                    30, 300, c.getDurationMinutes(), 5
                )
            );
            sp.setEditable(true);
            sp.setPrefWidth(90);

            durationSpinners.put(c, sp);

            HBox row = new HBox(20, lbl, sp);
            courseDurationList.getChildren().add(row);
        }
    }

    public void loadExtraSettings(int maxExams, int roomTurnover, int studentGap) {
        loadExtraSettings(maxExams, roomTurnover, studentGap, 9, 21);
    }

    public void loadExtraSettings(int maxExams, int roomTurnover, int studentGap,
                                  int startHour, int endHour) {

        savedMaxExams = maxExams;
        savedRoomTurnover = roomTurnover;
        savedStudentGap = studentGap;
        savedExamStartHour = startHour;
        savedExamEndHour = endHour;

        maxExamsSpinner.getValueFactory().setValue(maxExams);
        roomTurnoverSpinner.getValueFactory().setValue(roomTurnover);
        studentGapSpinner.getValueFactory().setValue(studentGap);
        examStartHourSpinner.getValueFactory().setValue(startHour);
        examEndHourSpinner.getValueFactory().setValue(endHour);
    }

    /* ===================== BUILD CONFIG ===================== */

    public ExamConfig buildConfig() {

        Map<String, Integer> durations = new LinkedHashMap<>();

        for (Course c : courses) {
            Spinner<Integer> sp = durationSpinners.get(c);
            if (sp != null) {
                durations.put(c.getCourseName(), sp.getValue());
            }
        }

        ExamConfig config = new ExamConfig(
            Collections.emptyMap(),
            maxExamsSpinner.getValue(),
            roomTurnoverSpinner.getValue(),
            studentGapSpinner.getValue(),
            durations
        );

        config.setExamStartHour(examStartHourSpinner.getValue());
        config.setExamEndHour(examEndHourSpinner.getValue());

        return config;
    }

    /* ===================== HELP ACTIONS ===================== */

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

    /* ===================== ACTIONS ===================== */

    @FXML
    private void saveSettings() {
        closePopup();
    }

    @FXML
    private void closePopup() {
        Stage st = (Stage) courseDurationList.getScene().getWindow();
        st.close();
    }
}
