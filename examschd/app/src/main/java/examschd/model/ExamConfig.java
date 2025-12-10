package examschd.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExamConfig {

    private Map<LocalDate, Boolean> allowedExamDays = new LinkedHashMap<>();
    private int maxExamsPerDay = 2;
    private int breakTimeBetweenExams = 30;

    private Map<String, Integer> courseDurations = new LinkedHashMap<>();

    public ExamConfig() {

    }

    public ExamConfig(Map<LocalDate, Boolean> allowedExamDays,
                      int maxExamsPerDay,
                      int breakTimeBetweenExams,
                      Map<String, Integer> courseDurations) {

        this.allowedExamDays = allowedExamDays;
        this.maxExamsPerDay = maxExamsPerDay;
        this.breakTimeBetweenExams = breakTimeBetweenExams;
        this.courseDurations = courseDurations;
    }

    public Map<LocalDate, Boolean> getAllowedExamDays() {
        return allowedExamDays;
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

    public void setAllowedExamDays(Map<LocalDate, Boolean> allowedExamDays) {
        this.allowedExamDays = allowedExamDays;
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

    @Override
    public String toString() {
        return "ExamConfig{" +
                "allowedDays=" + allowedExamDays +
                ", maxExamsPerDay=" + maxExamsPerDay +
                ", breakTimeBetweenExams=" + breakTimeBetweenExams +
                ", courseDurations=" + courseDurations +
                '}';
    }
}
