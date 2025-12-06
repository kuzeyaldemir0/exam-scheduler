package examschd.model;

import examschd.model.Student;
import examschd.model.ExamSession; 

import java.util.ArrayList;
import java.util.List;

public class Course {
    private int courseId;
    private String courseName;

    // Constructor
    public Course(int courseId, String courseName) {

        this.courseId = courseId;
        this.courseName = courseName;
    }

    // Getter & Setter
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;

    }
}