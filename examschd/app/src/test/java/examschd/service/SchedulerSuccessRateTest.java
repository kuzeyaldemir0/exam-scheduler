package examschd.service;

import examschd.model.*;
import examschd.util.TestDataGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.*;

/**
 * Test to measure scheduling success rates under various conditions
 */
public class SchedulerSuccessRateTest {

    private final Scheduler scheduler = new Scheduler();

    @Test
    @DisplayName("Success Rate Analysis: Various Scenarios")
    void analyzeSuccessRates() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCHEDULING SUCCESS RATE ANALYSIS");
        System.out.println("=".repeat(80));

        // Scenario 1: Ideal conditions (plenty of time, rooms, low conflicts)
        runScenario("Ideal Conditions",
            500, 30, 15,
            25, 10, 4, 1,
            new int[]{30, 40, 50, 100},
            14, 42);

        // Scenario 2: Moderate conditions (realistic university setting)
        runScenario("Moderate/Realistic",
            1000, 50, 20,
            30, 15, 5, 2,
            new int[]{20, 30, 40, 50, 100},
            14, 123);

        // Scenario 3: Tight constraints (limited time)
        runScenario("Tight Time Constraints",
            500, 60, 15,
            25, 10, 5, 2,
            new int[]{30, 40, 50},
            7, 555);

        // Scenario 4: Dense conflicts (many shared courses)
        runScenario("Dense Conflicts",
            500, 40, 15,
            180, 40, 12, 3,
            new int[]{50, 100, 150, 200},
            14, 999);

        // Scenario 5: Limited classroom capacity
        runScenario("Limited Capacity",
            800, 50, 10,
            30, 12, 5, 2,
            new int[]{20, 25, 30},
            14, 777);

        // Scenario 6: Large scale (stress test)
        runScenario("Large Scale",
            10000, 500, 50,
            25, 12, 6, 3,
            new int[]{30, 50, 80, 100, 150, 200},
            21, 12345);

        System.out.println("=".repeat(80));
        System.out.println("Analysis complete!");
        System.out.println("=".repeat(80) + "\n");
    }

    private void runScenario(String scenarioName,
                            int students, int courses, int classrooms,
                            int avgStudentsPerCourse, int stdDevStudents,
                            int avgCoursesPerStudent, int stdDevCourses,
                            int[] capacities,
                            int examDays,
                            long seed) {

        System.out.println("\n" + "-".repeat(80));
        System.out.println("Scenario: " + scenarioName);
        System.out.println("-".repeat(80));

        // Generate data
        TestDataGenerator.GeneratedData data = TestDataGenerator.builder()
            .studentCount(students)
            .courseCount(courses)
            .classroomCount(classrooms)
            .avgStudentsPerCourse(avgStudentsPerCourse, stdDevStudents)
            .avgCoursesPerStudent(avgCoursesPerStudent, stdDevCourses)
            .classroomCapacities(capacities)
            .seed(seed)
            .build()
            .generate();

        System.out.println("Configuration:");
        System.out.println("  Students: " + students);
        System.out.println("  Courses: " + courses);
        System.out.println("  Classrooms: " + classrooms + " (capacities: " + Arrays.toString(capacities) + ")");
        System.out.println("  Exam days: " + examDays + " (" + (examDays * 6) + " total time slots)");

        // Configure and run scheduler
        ExamConfig config = new ExamConfig();
        config.setMaxExamsPerDay(2);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(examDays - 1);

        long startTime = System.currentTimeMillis();
        Map<LocalDate, List<ExamSession>> result = scheduler.generateSchedule(
            data.getStudents(), data.getCourses(), data.getClassrooms(),
            data.getEnrollments(), config, start, end
        ).getSchedule();
        long elapsed = System.currentTimeMillis() - startTime;

        // Calculate metrics
        int totalCourses = data.getCourses().size();
        int scheduledCourses = result.values().stream().mapToInt(List::size).sum();
        double successRate = (scheduledCourses * 100.0) / totalCourses;

        // Count unique classrooms used
        Set<String> roomsUsed = new HashSet<>();
        int totalExamSlots = 0;
        for (List<ExamSession> sessions : result.values()) {
            for (ExamSession session : sessions) {
                totalExamSlots++;
                for (ExamPartition partition : session.getPartitions()) {
                    roomsUsed.add(partition.getClassroom().getName());
                }
            }
        }

        // Calculate utilization
        int availableSlots = examDays * 6;
        double slotUtilization = (totalExamSlots * 100.0) / availableSlots;
        double roomUtilization = (roomsUsed.size() * 100.0) / classrooms;

        // Print results
        System.out.println("\nResults:");
        System.out.println("  ✓ Courses scheduled: " + scheduledCourses + "/" + totalCourses);
        System.out.printf("  ✓ Success rate: %.1f%%\n", successRate);
        System.out.printf("  ✓ Time taken: %dms\n", elapsed);
        System.out.printf("  ✓ Slot utilization: %.1f%% (%d/%d slots used)\n",
            slotUtilization, totalExamSlots, availableSlots);
        System.out.printf("  ✓ Classrooms used: %d/%d (%.1f%%)\n",
            roomsUsed.size(), classrooms, roomUtilization);

        if (scheduledCourses < totalCourses) {
            int failed = totalCourses - scheduledCourses;
            System.out.println("  ⚠ Failed to schedule: " + failed + " courses (" +
                String.format("%.1f%%", (failed * 100.0) / totalCourses) + ")");
        }
    }
}
