package examschd.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExamConfig {

    private int maxExamsPerDay = 2;
    private int breakTimeBetweenExams = 30;

    private Map<String, Integer> courseDurations = new LinkedHashMap<>();

    // Exam day hours (24-hour format: 0-23)
    private int examStartHour = 9;    // 9:00 AM
    private int examEndHour = 21;     // 9:00 PM

    public ExamConfig() {

    }

    public ExamConfig(Map<LocalDate, Boolean> allowedExamDays,
                      int maxExamsPerDay,
                      int breakTimeBetweenExams,
                      Map<String, Integer> courseDurations) {

        this.maxExamsPerDay = maxExamsPerDay;
        this.breakTimeBetweenExams = breakTimeBetweenExams;
        this.courseDurations = courseDurations;
    }


    public int getMaxExamsPerDay() {
        return maxExamsPerDay;
    }

    public int getBreakTimeBetweenExams() {
        return breakTimeBetweenExams;
    }

    public Map<String, Integer> getCourseDurations() {
        return courseDurations;
    }

    public void setMaxExamsPerDay(int maxExamsPerDay) {
        this.maxExamsPerDay = maxExamsPerDay;
    }

    public void setBreakTimeBetweenExams(int breakTimeBetweenExams) {
        this.breakTimeBetweenExams = breakTimeBetweenExams;
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
                ", maxExamsPerDay=" + maxExamsPerDay +
                ", breakTimeBetweenExams=" + breakTimeBetweenExams +
                ", courseDurations=" + courseDurations +
                ", examStartHour=" + examStartHour +
                ", examEndHour=" + examEndHour +
                '}';
    }
}
