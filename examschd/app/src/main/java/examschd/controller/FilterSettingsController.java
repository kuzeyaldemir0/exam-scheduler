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
    @FXML private Spinner<Integer> breakTimeSpinner;

    /* ===================== STATE ===================== */

    private List<Course> courses = new ArrayList<>();

    private final Map<Course, Spinner<Integer>> durationSpinners = new LinkedHashMap<>();

    private int savedMaxExams = 2;
    private int savedBreakTime = 30;

    /* ===================== INIT ===================== */

    @FXML
    public void initialize() {
        setupExtraSpinners();
    }

    private void setupExtraSpinners() {

        maxExamsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, savedMaxExams)
        );

        breakTimeSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 180, savedBreakTime)
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
                    30,
                    300,
                    c.getDurationMinutes(),
                    5
                )
            );
            sp.setEditable(true);
            sp.setPrefWidth(90);

            durationSpinners.put(c, sp);

            HBox row = new HBox(20, lbl, sp);
            courseDurationList.getChildren().add(row);
        }
    }

    public void loadExtraSettings(int maxExams, int breakTime) {

        savedMaxExams = maxExams;
        savedBreakTime = breakTime;

        maxExamsSpinner.getValueFactory().setValue(maxExams);
        breakTimeSpinner.getValueFactory().setValue(breakTime);
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

        return new ExamConfig(
            Collections.emptyMap(),          // ❌ allowed days YOK
            maxExamsSpinner.getValue(),
            breakTimeSpinner.getValue(),
            durations
        );
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
