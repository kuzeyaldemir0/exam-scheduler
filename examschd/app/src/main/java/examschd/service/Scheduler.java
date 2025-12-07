package examschd.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.ExamSession;
import examschd.model.Student;

public class Scheduler {
    // TODO FUTURE: Make exam duration configurable per course instead of hardcoded
    // TODO FUTURE: Implement dynamic time slots instead of fixed 2-hour slots
    // TODO FUTURE: Use actual exam dates instead of placeholder Date objects
    // TODO FUTURE: Make number of days and slots per day configurable

    // Lists to hold the loaded data
    private List<Student> allStudents;
    private List<Course> allCourses;
    private List<Classroom> allClassrooms;

    public Scheduler() {
        this.allStudents = new ArrayList<>();
        this.allCourses = new ArrayList<>();
        this.allClassrooms = new ArrayList<>();
    }

    // Method to create realistic test data for development/testing
    public void createTestData() {
        Random random = new Random(42); // Fixed seed for reproducible results

        // 1. CREATE CLASSROOMS (6 rooms with varying capacities)
        String[] roomCodes = {"A101", "A102", "B201", "B202", "C301", "D401"};
        int[] capacities = {150, 100, 80, 60, 50, 40};

        for (int i = 0; i < roomCodes.length; i++) {
            Classroom room = new Classroom(i, roomCodes[i], capacities[i]);
            allClassrooms.add(room);
        }

        // 2. CREATE COURSES (12 common university courses)
        String[] courseCodes = {
            "MATH101", "PHYS101", "CS101", "CHEM101",
            "ENG101", "HIST101", "BIO101", "ECON101",
            "PSYCH101", "STAT101", "CS201", "MATH201"
        };

        for (int i = 0; i < courseCodes.length; i++) {
            Course course = new Course(i, courseCodes[i]);
            allCourses.add(course);
        }

        // 3. CREATE STUDENTS (150 students)
        for (int i = 0; i < 150; i++) {
            String studentNumber = String.format("2021%04d", i + 1);
            Student student = new Student(i, studentNumber);
            allStudents.add(student);
        }

        // 4. ENROLL STUDENTS IN COURSES (each student takes 4-6 courses)
        for (Student student : allStudents) {
            int numCourses = 4 + random.nextInt(3); // 4, 5, or 6 courses
            List<Course> availableCourses = new ArrayList<>(allCourses);

            for (int i = 0; i < numCourses && !availableCourses.isEmpty(); i++) {
                int randomIndex = random.nextInt(availableCourses.size());
                Course selectedCourse = availableCourses.remove(randomIndex);
                student.enrollInCourse(selectedCourse);
                selectedCourse.addStudent(student); // Bidirectional relationship
            }
        }

        System.out.println("Test data created:");
        System.out.println("  - " + allStudents.size() + " students");
        System.out.println("  - " + allCourses.size() + " courses");
        System.out.println("  - " + allClassrooms.size() + " classrooms");

        // Print enrollment stats for verification
        for (Course course : allCourses) {
            System.out.println("  - " + course.getCourseName() + ": " +
                             course.getStudents().size() + " students");
        }
    }

    // Method to import data from file (e.g., CSV or Excel)
    public void importData(File file) {
        // Implementation TODO:
        // 1. Parse file
        // 2. Populate allStudents, allCourses, allClassrooms
        System.out.println("Importing data from " + file.getName());
    }

    // Main scheduling algorithm using greedy approach
    // Creates ExamSession objects for each course and assigns them to time slots
    // Checks constraints: no conflicts, max 2 exams/day, no back-to-back exams
    public void generateSchedule() {
        System.out.println("Generating schedule...");

        List<Course> sortedCourses = sortByEnrollment();
        List<String> timeSlots = createTimeSlots();
        Map<String, List<ExamSession>> schedule = new HashMap<>();

        int sessionIdCounter = 1;

        for (int c = 0; c < sortedCourses.size(); c++) {
            Course course = sortedCourses.get(c);
            boolean scheduled = false;

            for (int t = 0; t < timeSlots.size(); t++) {
                String timeSlot = timeSlots.get(t);

                String[] parts = timeSlot.split("-");
                int day = Integer.parseInt(parts[0].replace("Day", ""));
                int slot = Integer.parseInt(parts[1].replace("Slot", ""));

                boolean canScheduleHere = true;
                List<Student> studentsInCourse = course.getStudents();

                for (int s = 0; s < studentsInCourse.size(); s++) {
                    Student student = studentsInCourse.get(s);

                    // Check if student has exam at this exact time
                    List<ExamSession> sessionsAtThisTime = schedule.get(timeSlot);
                    if (sessionsAtThisTime != null) {
                        for (int i = 0; i < sessionsAtThisTime.size(); i++) {
                            if (sessionsAtThisTime.get(i).getCourse().getStudents().contains(student)) {
                                canScheduleHere = false;
                                break;
                            }
                        }
                    }

                    if (!canScheduleHere) break;

                    if (studentAlreadyHasTwoOrMoreExamsOnDay(schedule, student, day)) {
                        canScheduleHere = false;
                        break;
                    }

                    if (studentWouldHaveBackToBackExam(schedule, student, day, slot)) {
                        canScheduleHere = false;
                        break;
                    }
                }

                if (canScheduleHere) {
                    // TODO FUTURE: Use actual exam date instead of new Date()
                    // TODO FUTURE: Make duration configurable per course
                    ExamSession examSession = new ExamSession(
                        sessionIdCounter++,
                        new Date(),
                        timeSlot,
                        120, // Fixed 2-hour duration
                        course
                    );

                    course.getExamSessions().add(examSession);

                    if (schedule.get(timeSlot) == null) {
                        schedule.put(timeSlot, new ArrayList<>());
                    }
                    schedule.get(timeSlot).add(examSession);

                    scheduled = true;
                    System.out.println("Scheduled " + course.getCourseName() + " at " + timeSlot);
                    break;
                }
            }

            if (!scheduled) {
                System.out.println("ERROR: Could not schedule " + course.getCourseName());
            }
        }

        System.out.println("\nScheduling complete!");
        // TODO FUTURE: Implement classroom assignment
    }

    // Generate all time slots for the exam period
    // Returns 54 time slot strings: "Day1-Slot1" through "Day9-Slot6"
    private List<String> createTimeSlots() {
        int numDays = 9;
        int slotsPerDay = 6;
        List<String> timeSlots = new ArrayList<>();

        for (int day = 1; day <= numDays; day++) {
            for (int slot = 1; slot <= slotsPerDay; slot++) {
                timeSlots.add("Day" + day + "-Slot" + slot);
            }
        }

        return timeSlots;
    }

    // Check if student already has 2 or more exams on the given day
    // Used to enforce constraint: max 2 exams per day per student
    private boolean studentAlreadyHasTwoOrMoreExamsOnDay(Map<String, List<ExamSession>> schedule, Student student, int day) {
        int examCount = 0;

        for (int slot = 1; slot <= 6; slot++) {
            String timeSlot = "Day" + day + "-Slot" + slot;
            List<ExamSession> sessionsAtThisTime = schedule.get(timeSlot);

            if (sessionsAtThisTime != null) {
                for (int i = 0; i < sessionsAtThisTime.size(); i++) {
                    Course course = sessionsAtThisTime.get(i).getCourse();
                    if (course.getStudents().contains(student)) {
                        examCount++;
                        break;
                    }
                }
            }
        }

        return examCount >= 2;
    }

    // Check if a student would have a back-to-back exam at a specific time slot
    // Back-to-back = consecutive time slots on the same day
    private boolean studentWouldHaveBackToBackExam(Map<String, List<ExamSession>> schedule, Student student, int day, int slot) {
        // Check previous slot
        if (slot > 1) {
            int previousSlot = slot - 1;
            String previousTimeSlot = "Day" + day + "-Slot" + previousSlot;
            List<ExamSession> sessionsAtPrevious = schedule.get(previousTimeSlot);

            if (sessionsAtPrevious != null) {
                for (int i = 0; i < sessionsAtPrevious.size(); i++) {
                    if (sessionsAtPrevious.get(i).getCourse().getStudents().contains(student)) {
                        return true;
                    }
                }
            }
        }

        // Check next slot
        if (slot < 6) {
            int nextSlot = slot + 1;
            String nextTimeSlot = "Day" + day + "-Slot" + nextSlot;
            List<ExamSession> sessionsAtNext = schedule.get(nextTimeSlot);

            if (sessionsAtNext != null) {
                for (int i = 0; i < sessionsAtNext.size(); i++) {
                    if (sessionsAtNext.get(i).getCourse().getStudents().contains(student)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Sort courses by enrollment count (greedy strategy: biggest first)
    // Courses with more students are harder to schedule, so we do them first
    private List<Course> sortByEnrollment() {
        List<Course> sorted = new ArrayList<>(allCourses);

        // Bubble sort - simple sorting algorithm
        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i + 1; j < sorted.size(); j++) {
                if (sorted.get(i).getStudents().size() < sorted.get(j).getStudents().size()) {
                    // Swap to put larger enrollment first
                    Course temp = sorted.get(i);
                    sorted.set(i, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }

        return sorted;
    }

    // TEMPORARY: Main method for testing the scheduler
    public static void main(String[] args) {
        System.out.println("=== Testing Greedy Scheduler ===\n");

        // Create a scheduler instance
        Scheduler scheduler = new Scheduler();

        // Create test data (150 students, 12 courses, 6 classrooms)
        scheduler.createTestData();

        System.out.println("\n=== Starting Greedy Algorithm ===\n");

        // Run the greedy scheduling algorithm
        scheduler.generateSchedule();

        System.out.println("\n=== Test Complete ===");
    }
}
