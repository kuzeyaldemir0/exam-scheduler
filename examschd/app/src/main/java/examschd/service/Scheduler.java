package examschd.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.Student;

public class Scheduler {
    // Lists to hold the loaded data
    private List<Student> allStudents;
    private List<Course> allCourses;
    private List<Classroom> allClassrooms;

    public Scheduler() {
        this.allStudents = new ArrayList<>();
        this.allCourses = new ArrayList<>();
        this.allClassrooms = new ArrayList<>();
    }

    // Method to import data from file (e.g., CSV or Excel)
    public void importData(File file) {
        // Implementation TODO:
        // 1. Parse file
        // 2. Populate allStudents, allCourses, allClassrooms
        System.out.println("Importing data from " + file.getName());
    }

    // Method to run the scheduling algorithm
    public void generateSchedule() {
        // Implementation TODO:
        // 1. Sort courses (Greedy approach)
        // 2. Loop through courses and assign to ExamSessions
        // 3. Check constraints using checkHardConstraints()
        System.out.println("Generating schedule...");
    }

    // Method to validate constraints (e.g., max 2 exams per day)
    // Returns true if constraints are met, false otherwise
    private boolean checkHardConstraints() {
        // Implementation TODO:
        // Iterate through students and check overlap/consecutive rules
        return true;
    }
}