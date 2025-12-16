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

    // ==================== COMPREHENSIVE STRESS TESTS ====================

    @Test
    @DisplayName("Stress test: Medium scale (1000 students, 50 courses, varied classrooms)")
    void testMediumScaleScheduling() {
        System.out.println("\n=== MEDIUM SCALE STRESS TEST ===");

        long startTime = System.currentTimeMillis();

        examschd.util.TestDataGenerator.GeneratedData data =
            examschd.util.TestDataGenerator.builder()
                .studentCount(1000)
                .courseCount(50)
                .classroomCount(20)
                .avgStudentsPerCourse(30, 15)
                .avgCoursesPerStudent(5, 2)
                .classroomCapacities(20, 30, 40, 50, 100)
                .seed(42)
                .build()
                .generate();

        long genTime = System.currentTimeMillis();
        System.out.println("Data generation time: " + (genTime - startTime) + "ms");
        data.printStatistics();

        // Schedule
        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(13); // 14-day exam period

        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            data.getStudents(), data.getCourses(), data.getClassrooms(),
            data.getEnrollments(), config, start, end
        );

        long schedTime = System.currentTimeMillis();
        System.out.println("Scheduling time: " + (schedTime - genTime) + "ms");
        System.out.println("Total time: " + (schedTime - startTime) + "ms");

        // Verify results
        int totalScheduled = result.values().stream().mapToInt(List::size).sum();
        System.out.println("Courses scheduled: " + totalScheduled + "/" + data.getCourses().size());

        assertTrue(totalScheduled > 0, "Should schedule at least some courses");

        // Verify classroom distribution (THIS TESTS THE FIX!)
        Map<String, Integer> classroomUsage = new HashMap<>();
        for (List<ExamSession> sessions : result.values()) {
            for (ExamSession session : sessions) {
                for (ExamPartition partition : session.getPartitions()) {
                    String roomName = partition.getClassroom().getName();
                    classroomUsage.merge(roomName, 1, Integer::sum);
                }
            }
        }

        System.out.println("Classroom usage distribution: " + classroomUsage);

        // CRITICAL: Verify multiple classrooms are used (not just Classroom_01)
        assertTrue(classroomUsage.size() > 1,
            "Multiple classrooms should be used, not just one! Usage: " + classroomUsage);

        // Verify no student has overlapping exams
        verifyNoStudentConflicts(result);
    }

    @Test
    @DisplayName("Stress test: Large scale (10,000 students, 500 courses)")
    void testLargeScaleScheduling() {
        System.out.println("\n=== LARGE SCALE STRESS TEST ===");

        long startTime = System.currentTimeMillis();

        examschd.util.TestDataGenerator.GeneratedData data =
            examschd.util.TestDataGenerator.builder()
                .studentCount(10000)
                .courseCount(500)
                .classroomCount(50)
                .avgStudentsPerCourse(25, 12)
                .avgCoursesPerStudent(6, 3)
                .classroomCapacities(30, 50, 80, 100, 150, 200)
                .seed(123)
                .build()
                .generate();

        long genTime = System.currentTimeMillis();
        System.out.println("Data generation time: " + (genTime - startTime) + "ms");
        data.printStatistics();

        // Schedule over 3 weeks
        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(20); // 21-day exam period

        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            data.getStudents(), data.getCourses(), data.getClassrooms(),
            data.getEnrollments(), config, start, end
        );

        long schedTime = System.currentTimeMillis();
        System.out.println("Scheduling time: " + (schedTime - genTime) + "ms");
        System.out.println("Total time: " + (schedTime - startTime) + "ms");

        int totalScheduled = result.values().stream().mapToInt(List::size).sum();
        System.out.println("Courses scheduled: " + totalScheduled + "/" + data.getCourses().size());

        assertTrue(totalScheduled > 0, "Should schedule courses even at large scale");

        // Performance assertion: should complete in reasonable time (< 30 seconds)
        long totalTime = schedTime - startTime;
        assertTrue(totalTime < 30000,
            "Large scale scheduling should complete in under 30 seconds, took " + totalTime + "ms");
    }

    @Test
    @DisplayName("Stress test: Dense conflicts (all students share many courses)")
    void testDenseConflictScheduling() {
        System.out.println("\n=== DENSE CONFLICT STRESS TEST ===");

        // Generate data where students have high overlap in courses
        examschd.util.TestDataGenerator.GeneratedData data =
            examschd.util.TestDataGenerator.builder()
                .studentCount(500)
                .courseCount(30)
                .classroomCount(15)
                .avgStudentsPerCourse(250, 50)  // Most students in most courses!
                .avgCoursesPerStudent(20, 5)    // Students take many courses
                .classroomCapacities(50, 100, 150, 200)
                .seed(999)
                .build()
                .generate();

        data.printStatistics();

        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(20); // May need more days due to conflicts

        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            data.getStudents(), data.getCourses(), data.getClassrooms(),
            data.getEnrollments(), config, start, end
        );

        int totalScheduled = result.values().stream().mapToInt(List::size).sum();
        System.out.println("Courses scheduled with dense conflicts: " + totalScheduled + "/" + data.getCourses().size());

        // With high conflicts, we may not schedule everything, but should schedule some
        assertTrue(totalScheduled > 0, "Should schedule some courses even with dense conflicts");

        // Verify no conflicts
        verifyNoStudentConflicts(result);
    }

    @Test
    @DisplayName("Stress test: Varied classroom capacities (10-500 students per course)")
    void testVariedClassroomCapacities() {
        System.out.println("\n=== VARIED CAPACITY STRESS TEST ===");

        // Generate courses with wildly different sizes
        examschd.util.TestDataGenerator.GeneratedData data =
            examschd.util.TestDataGenerator.builder()
                .studentCount(2000)
                .courseCount(100)
                .classroomCount(30)
                .avgStudentsPerCourse(50, 80)  // High variance!
                .avgCoursesPerStudent(5, 2)
                .classroomCapacities(10, 20, 30, 50, 80, 100, 150, 200, 300, 500)
                .seed(777)
                .build()
                .generate();

        data.printStatistics();

        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(13);

        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            data.getStudents(), data.getCourses(), data.getClassrooms(),
            data.getEnrollments(), config, start, end
        );

        int totalScheduled = result.values().stream().mapToInt(List::size).sum();
        System.out.println("Courses scheduled: " + totalScheduled + "/" + data.getCourses().size());

        // Verify large courses are split across multiple rooms
        for (List<ExamSession> sessions : result.values()) {
            for (ExamSession session : sessions) {
                int studentCount = session.getCourse().getStudents().size();
                int totalCapacity = session.getPartitions().stream()
                    .mapToInt(ExamPartition::getCapacityAssigned)
                    .sum();

                assertEquals(studentCount, totalCapacity,
                    "Assigned capacity should match student count for " + session.getCourse().getCourseName());
            }
        }
    }

    @Test
    @DisplayName("Stress test: Edge case - Very few time slots available")
    void testLimitedTimeSlots() {
        System.out.println("\n=== LIMITED TIME SLOTS STRESS TEST ===");

        examschd.util.TestDataGenerator.GeneratedData data =
            examschd.util.TestDataGenerator.builder()
                .studentCount(500)
                .courseCount(80)
                .classroomCount(25)
                .avgStudentsPerCourse(30, 10)
                .avgCoursesPerStudent(4, 2)
                .classroomCapacities(30, 40, 50)
                .seed(555)
                .build()
                .generate();

        data.printStatistics();

        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(2); // Only 3 days! (18 time slots with 6 slots/day)

        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            data.getStudents(), data.getCourses(), data.getClassrooms(),
            data.getEnrollments(), config, start, end
        );

        int totalScheduled = result.values().stream().mapToInt(List::size).sum();
        System.out.println("Courses scheduled in limited time: " + totalScheduled + "/" + data.getCourses().size());

        // With limited time, we should still be able to schedule some courses
        // Note: Multiple courses can be in same slot if no conflicts and enough classrooms!
        assertTrue(totalScheduled > 0, "Should schedule at least some courses");
        assertTrue(totalScheduled <= 80, "Should not schedule more courses than we have");
    }

    // Helper method to verify no student has conflicting exams
    private void verifyNoStudentConflicts(Map<LocalDate, List<ExamSession>> schedule) {
        Map<String, List<ExamSession>> slotMap = new HashMap<>();

        for (Map.Entry<LocalDate, List<ExamSession>> entry : schedule.entrySet()) {
            for (ExamSession session : entry.getValue()) {
                String key = entry.getKey().toString() + "-" + session.getTimeSlot();
                slotMap.computeIfAbsent(key, k -> new ArrayList<>()).add(session);
            }
        }

        for (Map.Entry<String, List<ExamSession>> slotEntry : slotMap.entrySet()) {
            List<ExamSession> sessionsInSlot = slotEntry.getValue();

            for (int i = 0; i < sessionsInSlot.size(); i++) {
                for (int j = i + 1; j < sessionsInSlot.size(); j++) {
                    ExamSession s1 = sessionsInSlot.get(i);
                    ExamSession s2 = sessionsInSlot.get(j);

                    // Check if any student is in both courses
                    List<Student> students1 = s1.getCourse().getStudents();
                    List<Student> students2 = s2.getCourse().getStudents();

                    for (Student student : students1) {
                        assertFalse(students2.contains(student),
                            "Student " + student.getStudentName() + " has conflict at " + slotEntry.getKey() +
                            " between " + s1.getCourse().getCourseName() + " and " + s2.getCourse().getCourseName());
                    }
                }
            }
        }
    }
}
