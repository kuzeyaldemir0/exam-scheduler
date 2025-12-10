package examschd.controller;

import examschd.model.Course;
import examschd.model.ExamConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;

public class FilterSettingsController {

    @FXML private VBox courseDurationList;
    @FXML private VBox examDaysList;

    @FXML private Spinner<Integer> maxExamsSpinner;
    @FXML private Spinner<Integer> breakTimeSpinner;

    private List<Course> courses = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;

    private final Map<Course, Spinner<Integer>> durationSpinners = new HashMap<>();
    private final Map<LocalDate, CheckBox> dayCheckboxes = new LinkedHashMap<>();

    private final Map<LocalDate, Boolean> savedExamDays = new LinkedHashMap<>();
    private int savedMaxExams = 2;
    private int savedBreakTime = 30;

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
            sp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                    30, 300, c.getDurationMinutes(), 1
            ));
            sp.setEditable(true);
            sp.setPrefWidth(90);

            HBox row = new HBox(20, lbl, sp);
            courseDurationList.getChildren().add(row);

            durationSpinners.put(c, sp);
        }
    }

    public void setExamDateRange(LocalDate start, LocalDate end) {
        this.startDate = start;
        this.endDate = end;
        loadExamDays();
    }

    private void loadExamDays() {

        examDaysList.getChildren().clear();
        dayCheckboxes.clear();

        if (startDate == null || endDate == null) return;

        LocalDate d = startDate;

        while (!d.isAfter(endDate)) {

            CheckBox cb = new CheckBox(d.toString());
            cb.setSelected(savedExamDays.getOrDefault(d, true));

            examDaysList.getChildren().add(cb);
            dayCheckboxes.put(d, cb);

            d = d.plusDays(1);
        }
    }

    public void loadSavedExamDays(Map<LocalDate, Boolean> map) {
        savedExamDays.clear();
        savedExamDays.putAll(map);

        if (startDate != null && endDate != null)
            loadExamDays();
    }

    public void loadExtraSettings(int maxExams, int breakTime) {
        savedMaxExams = maxExams;
        savedBreakTime = breakTime;

        maxExamsSpinner.getValueFactory().setValue(maxExams);
        breakTimeSpinner.getValueFactory().setValue(breakTime);
    }

    public ExamConfig buildConfig() {

        Map<String, Integer> durations = new LinkedHashMap<>();
        for (Course c : courses) {
            durations.put(c.getCourseName(), c.getDurationMinutes());
        }

        return new ExamConfig(
                new LinkedHashMap<>(savedExamDays),   
                savedMaxExams,                        
                savedBreakTime,
                durations                             
        );
    }

    @FXML
    private void saveSettings() {

        for (Course c : courses) {
            Spinner<Integer> sp = durationSpinners.get(c);
            if (sp != null) c.setDurationMinutes(sp.getValue());
        }

        savedExamDays.clear();
        dayCheckboxes.forEach((day, cb) ->
                savedExamDays.put(day, cb.isSelected())
        );

        savedMaxExams = maxExamsSpinner.getValue();
        savedBreakTime = breakTimeSpinner.getValue();

        closePopup();
    }

    @FXML
    private void closePopup() {
        Stage st = (Stage) courseDurationList.getScene().getWindow();
        st.close();
    }

    public Map<LocalDate, Boolean> getExamAllowedDays() { return savedExamDays; }
    public int getMaxExamsPerDay() { return savedMaxExams; }
    public int getBreakTimeBetweenExams() { return savedBreakTime; }
}
