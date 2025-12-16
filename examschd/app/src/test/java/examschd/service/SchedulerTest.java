package examschd.service;

import examschd.model.*;
import examschd.service.readers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new Scheduler();
    }

    @Test
    @DisplayName("Schedule courses from CSV test data")
    void testScheduleWithCsvData() throws Exception {
        // Load test data from CSV files
        String basePath = "src/test/resources/csv/";

        List<Student> students = StudentCsvReader.read(basePath + "students.csv");
        List<Course> courses = CourseCsvReader.read(basePath + "courses.csv");
        List<Classroom> classrooms = ClassroomCsvReader.read(basePath + "classrooms.csv");
        List<Enrollment> enrollments = EnrollmentCsvReader.read(basePath + "enrollments.csv");

        // Build enrollment relationships (simulate ImportService behavior)
        Map<String, Integer> studentIdMap = new HashMap<>();
        for (Student s : students) {
            studentIdMap.put(s.getStudentName(), s.getId());
        }

        for (Enrollment e : enrollments) {
            List<String> studentNumbers = e.getStudentNumbers();
            int[] studentIds = studentNumbers.stream()
                .mapToInt(sn -> studentIdMap.getOrDefault(sn.trim(), 0))
                .toArray();
            e.setStudentIds(studentIds);
        }

        // Create config
        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);

        // Date range for scheduling
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(13);

        // Run scheduler
        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            students, courses, classrooms, enrollments, config, start, end
        );

        // Verify all courses scheduled
        int totalScheduled = result.values().stream().mapToInt(List::size).sum();
        assertEquals(20, totalScheduled, "All 20 courses should be scheduled");

        // Verify each session has classroom partitions
        for (List<ExamSession> sessions : result.values()) {
            for (ExamSession session : sessions) {
                assertFalse(session.getPartitions().isEmpty(),
                    "Each session should have at least one classroom partition");
            }
        }
    }

    @Test
    @DisplayName("Classroom splitting when course exceeds single room capacity")
    void testClassroomSplitting() {
        // Create a course with 100 students
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            students.add(new Student(i, "Student_" + i));
        }

        Course largeCourse = new Course(1, "LargeCourse");
        for (Student s : students) {
            largeCourse.addStudent(s);
            s.enrollInCourse(largeCourse);
        }
        List<Course> courses = List.of(largeCourse);

        // Create classrooms with capacity 30 each (need 4 rooms for 100 students)
        List<Classroom> classrooms = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            classrooms.add(new Classroom(i, "Room_" + i, 30));
        }

        // Empty enrollments (relationships already built manually)
        List<Enrollment> enrollments = new ArrayList<>();

        // Config
        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);

        // Date range for scheduling
        LocalDate start = LocalDate.now();
        LocalDate end = start;

        // Run scheduler
        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            students, courses, classrooms, enrollments, config, start, end
        );

        // Verify course was scheduled
        assertEquals(1, result.values().stream().mapToInt(List::size).sum(),
            "The large course should be scheduled");

        // Verify it was split across multiple classrooms
        ExamSession session = result.values().iterator().next().get(0);
        assertTrue(session.getPartitions().size() >= 4,
            "Course with 100 students should be split across at least 4 rooms (30 capacity each)");

        // Verify total capacity covers all students
        int totalCapacity = session.getPartitions().stream()
            .mapToInt(ExamPartition::getCapacityAssigned)
            .sum();
        assertEquals(100, totalCapacity, "Total assigned capacity should equal student count");

        // Print partition details for visibility
        System.out.println("Classroom splitting result:");
        for (ExamPartition p : session.getPartitions()) {
            System.out.println("  " + p.getClassroom().getName() + ": " + p.getCapacityAssigned() + " students");
        }
    }

    @Test
    @DisplayName("No student conflicts - same student cannot have two exams at same time")
    void testNoStudentConflicts() {
        // Create shared students
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            students.add(new Student(i, "Student_" + i));
        }

        // Two courses that share ALL students (must be in different slots)
        Course course1 = new Course(1, "Course_A");
        Course course2 = new Course(2, "Course_B");

        for (Student s : students) {
            course1.addStudent(s);
            course2.addStudent(s);
            s.enrollInCourse(course1);
            s.enrollInCourse(course2);
        }

        List<Course> courses = List.of(course1, course2);
        List<Classroom> classrooms = List.of(new Classroom(1, "Room1", 50));
        List<Enrollment> enrollments = new ArrayList<>();

        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);

        // Date range for scheduling
        LocalDate start = LocalDate.now();
        LocalDate end = start;

        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            students, courses, classrooms, enrollments, config, start, end
        );

        // Both should be scheduled
        assertEquals(2, result.values().stream().mapToInt(List::size).sum());

        // They should be in different time slots
        List<ExamSession> sessions = result.values().iterator().next();
        assertNotEquals(sessions.get(0).getTimeSlot(), sessions.get(1).getTimeSlot(),
            "Courses with shared students must be in different time slots");
    }

    @Test
    @DisplayName("Classroom reuse across different time slots")
    void testClassroomReuseAcrossSlots() {
        // Create 3 courses with NO overlapping students
        List<Student> allStudents = new ArrayList<>();
        List<Course> courses = new ArrayList<>();

        for (int c = 1; c <= 3; c++) {
            Course course = new Course(c, "Course_" + c);
            for (int s = 1; s <= 10; s++) {
                int studentId = (c - 1) * 10 + s;
                Student student = new Student(studentId, "Student_" + studentId);
                allStudents.add(student);
                course.addStudent(student);
                student.enrollInCourse(course);
            }
            courses.add(course);
        }

        // Only ONE classroom - should be reused across slots
        List<Classroom> classrooms = List.of(new Classroom(1, "OnlyRoom", 50));
        List<Enrollment> enrollments = new ArrayList<>();

        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(3);

        // Date range for scheduling
        LocalDate start = LocalDate.now();
        LocalDate end = start;

        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            allStudents, courses, classrooms, enrollments, config, start, end
        );

        // All 3 courses should be scheduled (same room, different slots)
        assertEquals(3, result.values().stream().mapToInt(List::size).sum(),
            "All 3 courses should be scheduled using the same room in different slots");

        // Verify all use the same classroom
        for (List<ExamSession> sessions : result.values()) {
            for (ExamSession session : sessions) {
                assertEquals("OnlyRoom", session.getPartitions().get(0).getClassroom().getName());
            }
        }
    }

    @Test
    @DisplayName("Cannot schedule when insufficient classroom capacity")
    void testInsufficientCapacity() {
        // 50 students but only 20 capacity available
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            students.add(new Student(i, "Student_" + i));
        }

        Course course = new Course(1, "BigCourse");
        for (Student s : students) {
            course.addStudent(s);
            s.enrollInCourse(course);
        }

        // Only one small classroom
        List<Classroom> classrooms = List.of(new Classroom(1, "TinyRoom", 20));
        List<Enrollment> enrollments = new ArrayList<>();

        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);

        // Date range for scheduling
        LocalDate start = LocalDate.now();
        LocalDate end = start;

        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            students, List.of(course), classrooms, enrollments, config, start, end
        );

        // Course cannot be scheduled
        assertEquals(0, result.values().stream().mapToInt(List::size).sum(),
            "Course with 50 students should not be scheduled when only 20 capacity available");
    }
}
