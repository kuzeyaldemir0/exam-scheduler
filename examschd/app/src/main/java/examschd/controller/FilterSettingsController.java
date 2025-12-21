package examschd.controller;

import examschd.model.Course;
import examschd.model.ExamConfig;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class FilterSettingsController {

    /* ===================== FXML ===================== */

    @FXML private VBox courseDurationList;

    @FXML private Spinner<Integer> maxExamsSpinner;
    @FXML private Spinner<Integer> roomTurnoverSpinner;
    @FXML private Spinner<Integer> studentGapSpinner;
    @FXML private Spinner<Integer> examStartHourSpinner;
    @FXML private Button gotItBtn;
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
        if (gotItBtn != null) {
            addGotItHoverColorAnimation();
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

    private void addGotItHoverColorAnimation() {

        Color normal = Color.web("#1976D2");
        Color hover  = Color.web("#125AA0"); // basılmış gibi

        Duration dur = Duration.millis(140);

        gotItBtn.setBackground(
            new Background(new BackgroundFill(
                normal, new CornerRadii(10), Insets.EMPTY
            ))
        );

        gotItBtn.setOnMouseEntered(e -> animateBgColor(gotItBtn, normal, hover, dur));
        gotItBtn.setOnMouseExited(e -> animateBgColor(gotItBtn, hover, normal, dur));
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


}
