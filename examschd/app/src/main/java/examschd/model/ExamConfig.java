package examschd.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExamConfig {

    private int maxExamsPerDay = 2;
    private int roomTurnoverMinutes = 15;      // Time for room changeover between exams
    private int studentMinGapMinutes = 90;     // Minimum gap between exams for same student

    private Map<String, Integer> courseDurations = new LinkedHashMap<>();

    // Exam day hours (24-hour format: 0-23)
    private int examStartHour = 9;    // 9:00 AM
    private int examEndHour = 21;     // 9:00 PM

    public ExamConfig() {

    }

    public ExamConfig(int maxExamsPerDay,
                      int roomTurnoverMinutes,
                      int studentMinGapMinutes,
                      Map<String, Integer> courseDurations) {

        this.maxExamsPerDay = maxExamsPerDay;
        this.roomTurnoverMinutes = roomTurnoverMinutes;
        this.studentMinGapMinutes = studentMinGapMinutes;
        this.courseDurations = courseDurations;
    }


    public int getMaxExamsPerDay() {
        return maxExamsPerDay;
    }

    public int getRoomTurnoverMinutes() {
        return roomTurnoverMinutes;
    }

    public int getStudentMinGapMinutes() {
        return studentMinGapMinutes;
    }

    public Map<String, Integer> getCourseDurations() {
        return courseDurations;
    }

    public void setMaxExamsPerDay(int maxExamsPerDay) {
        this.maxExamsPerDay = maxExamsPerDay;
    }

    public void setRoomTurnoverMinutes(int roomTurnoverMinutes) {
        this.roomTurnoverMinutes = roomTurnoverMinutes;
    }

    public void setStudentMinGapMinutes(int studentMinGapMinutes) {
        this.studentMinGapMinutes = studentMinGapMinutes;
    }

    public void setCourseDurations(Map<String, Integer> courseDurations) {
        this.courseDurations = courseDurations;
    }

    public int getExamStartHour() {
        return examStartHour;
    }

    public void setExamStartHour(int examStartHour) {
        this.examStartHour = examStartHour;
    }

    public int getExamEndHour() {
        return examEndHour;
    }

    public void setExamEndHour(int examEndHour) {
        this.examEndHour = examEndHour;
    }

    @Override
    public String toString() {
        return "ExamConfig{" +
                "maxExamsPerDay=" + maxExamsPerDay +
                ", roomTurnoverMinutes=" + roomTurnoverMinutes +
                ", studentMinGapMinutes=" + studentMinGapMinutes +
                ", courseDurations=" + courseDurations +
                ", examStartHour=" + examStartHour +
                ", examEndHour=" + examEndHour +
                '}';
    }
}
