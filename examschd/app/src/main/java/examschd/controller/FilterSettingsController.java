package examschd.controller;

import examschd.model.Course;
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

    private List<Course> courses = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;

    private final Map<Course, Spinner<Integer>> durationSpinners = new HashMap<>();
    private final Map<LocalDate, CheckBox> dayCheckboxes = new LinkedHashMap<>();

    private final Map<LocalDate, Boolean> savedExamDays = new LinkedHashMap<>();

    public void setCourses(List<Course> list) {
        this.courses = list;
        loadCourseDurationInputs();
    }

    public void setExamDateRange(LocalDate start, LocalDate end) {
        this.startDate = start;
        this.endDate = end;
        loadExamDays();
    }

    private void loadCourseDurationInputs() {
        courseDurationList.getChildren().clear();
        durationSpinners.clear();

        if (courses == null) return;

        for (Course c : courses) {

            Label lbl = new Label(c.getCourseName());
            lbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 500;");
            lbl.setPrefWidth(250);

            Spinner<Integer> sp = new Spinner<>();
            sp.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 300, c.getDurationMinutes(), 5)
            );
            sp.setPrefWidth(90);

            HBox row = new HBox(20, lbl, sp);
            row.setStyle("-fx-padding: 5 5 5 5;");

            courseDurationList.getChildren().add(row);
            durationSpinners.put(c, sp);
        }
    }

    // -----------------------------------------------------------
    // EXAM DAYS CHECKBOX LIST
    // -----------------------------------------------------------
    private void loadExamDays() {
        examDaysList.getChildren().clear();
        dayCheckboxes.clear();

        if (startDate == null || endDate == null) return;

        LocalDate d = startDate;

        while (!d.isAfter(endDate)) {

            CheckBox cb = new CheckBox(d.toString());
            cb.setSelected(true); // default: sınav yapılabilir
            cb.setStyle("-fx-font-size: 15px;");

            examDaysList.getChildren().add(cb);
            dayCheckboxes.put(d, cb);

            d = d.plusDays(1);
        }
    }

    // -----------------------------------------------------------
    // SAVE
    // -----------------------------------------------------------
    @FXML
    private void saveSettings() {

        // Save durations
        for (Course c : courses) {
            Spinner<Integer> sp = durationSpinners.get(c);
            if (sp != null) c.setDurationMinutes(sp.getValue());
        }

        // Save allowed exam days
        savedExamDays.clear();
        for (LocalDate d : dayCheckboxes.keySet()) {
            savedExamDays.put(d, dayCheckboxes.get(d).isSelected());
        }

        closePopup();
    }

    @FXML
    private void closePopup() {
        Stage st = (Stage) courseDurationList.getScene().getWindow();
        st.close();
    }

    // SchedulingController popup kapanınca buradan okur
    public Map<LocalDate, Boolean> getExamAllowedDays() {
        return savedExamDays;
    }
}
