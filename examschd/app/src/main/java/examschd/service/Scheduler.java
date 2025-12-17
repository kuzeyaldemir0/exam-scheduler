package examschd.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.Enrollment;
import examschd.model.ExamConfig;
import examschd.model.ExamPartition;
import examschd.model.ExamSession;
import examschd.model.ScheduleResult;
import examschd.model.Student;
import examschd.model.StudentAssignment;

public class Scheduler {

    public Scheduler() {}

    /* ===================== HELPERS ===================== */

    private List<LocalDateTime> generatePossibleStartTimes(LocalDate day, ExamConfig config) {
        List<LocalDateTime> startTimes = new ArrayList<>();
        int startHour = config.getExamStartHour();
        int endHour = config.getExamEndHour();

        // Generate possible start times every 30 minutes
        for (int hour = startHour; hour < endHour; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                startTimes.add(LocalDateTime.of(day, LocalTime.of(hour, minute)));
            }
        }

        return startTimes;
    }

    private boolean wouldExceedDayBoundary(LocalDateTime startTime, int durationMinutes, ExamConfig config) {
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        int endHour = endTime.getHour();
        int endMinute = endTime.getMinute();

        if (endHour > config.getExamEndHour()) return true;
        if (endHour == config.getExamEndHour() && endMinute > 0) return true;

        return false;
    }

    private boolean studentHasConflictAt(Student student, LocalDateTime startTime, LocalDateTime endTime,
                                         int breakTimeMinutes, List<ExamSession> allScheduledSessions) {
        for (ExamSession session : allScheduledSessions) {
            if (session.getCourse().getStudents().contains(student)) {
                if (session.overlaps(new ExamSession(-1, startTime, endTime, 0, null), breakTimeMinutes)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean courseHasStudentConflictWith(Course course, List<ExamSession> sessionsAtTime) {
        Set<Student> courseStudents = new HashSet<>(course.getStudents());

        for (ExamSession session : sessionsAtTime) {
            for (Student student : session.getCourse().getStudents()) {
                if (courseStudents.contains(student)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<ExamSession> getSessionsAtTime(LocalDateTime startTime, LocalDateTime endTime,
                                                 int breakTimeMinutes, List<ExamSession> allScheduledSessions) {
        List<ExamSession> result = new ArrayList<>();
        for (ExamSession session : allScheduledSessions) {
            if (session.overlaps(new ExamSession(-1, startTime, endTime, 0, null), breakTimeMinutes)) {
                result.add(session);
            }
        }
        return result;
    }

    private int getStudentExamsOnDay(Student student, LocalDate day, List<ExamSession> allScheduledSessions) {
        int count = 0;
        for (ExamSession session : allScheduledSessions) {
            if (session.getStartTime() != null && session.getStartTime().toLocalDate().equals(day)) {
                if (session.getCourse().getStudents().contains(student)) {
                    count++;
                }
            }
        }
        return count;
    }

    private List<Classroom> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime,
                                               int studentCount, List<Classroom> allRooms,
                                               int breakTimeMinutes, List<ExamSession> allScheduledSessions) {
        List<Classroom> availableRooms = new ArrayList<>();
        LocalDateTime endTimeWithBreak = endTime.plusMinutes(breakTimeMinutes);

        for (Classroom room : allRooms) {
            boolean isAvailable = true;
            for (ExamSession session : allScheduledSessions) {
                for (ExamPartition partition : session.getPartitions()) {
                    if (partition.getClassroom().equals(room)) {
                        // Check if this room is occupied during our time window
                        LocalDateTime sessionEndWithBreak = session.getEndTime().plusMinutes(breakTimeMinutes);
                        if (!(endTimeWithBreak.isBefore(session.getStartTime()) ||
                              sessionEndWithBreak.isBefore(startTime))) {
                            isAvailable = false;
                            break;
                        }
                    }
                }
                if (!isAvailable) break;
            }
            if (isAvailable) {
                availableRooms.add(room);
            }
        }

        // Sort by capacity descending (largest first)
        availableRooms.sort((a, b) -> Integer.compare(b.getCapacity(), a.getCapacity()));

        // Use bin-packing to assign rooms
        List<Classroom> assignedRooms = new ArrayList<>();
        int remaining = studentCount;

        for (Classroom room : availableRooms) {
            if (remaining <= 0) break;
            assignedRooms.add(room);
            remaining -= room.getCapacity();
        }

        return remaining > 0 ? null : assignedRooms;
    }

    private List<Course> sortByEnrollment(List<Course> courses) {
        List<Course> sorted = new ArrayList<>(courses);
        sorted.sort((a, b) ->
                Integer.compare(b.getStudents().size(), a.getStudents().size()));
        return sorted;
    }

    private int assignStudentsToPartitions(ExamSession session, List<Student> students,
                                           List<Classroom> assignedRooms, int startAssignmentId) {
        int assignmentId = startAssignmentId;
        List<Student> studentList = new ArrayList<>(students);
        int studentIndex = 0;

        for (int i = 0; i < assignedRooms.size(); i++) {
            Classroom room = assignedRooms.get(i);
            ExamPartition partition = session.getPartitions().get(i);
            int seatNumber = 1;

            // Assign students to this partition up to room capacity
            while (studentIndex < studentList.size() && seatNumber <= room.getCapacity()) {
                Student student = studentList.get(studentIndex);
                StudentAssignment assignment = new StudentAssignment(
                    assignmentId++, seatNumber++, student, partition
                );
                partition.addAssignment(assignment);
                studentIndex++;
            }
        }

        return assignmentId;
    }

    private List<LocalDate> buildDateRange(LocalDate start, LocalDate end) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            days.add(d);
            d = d.plusDays(1);
        }
        return days;
    }

    /* ===================== MAIN ===================== */

    public ScheduleResult generateSchedule(
            List<Student> students,
            List<Course> courses,
            List<Classroom> classrooms,
            List<Enrollment> enrollments,
            ExamConfig config,
            LocalDate startDate,
            LocalDate endDate) {

        System.out.println("=== Generating Schedule ===");
        System.out.println("Config: startHour=" + config.getExamStartHour() +
            ", endHour=" + config.getExamEndHour() +
            ", maxExamsPerDay=" + config.getMaxExamsPerDay() +
            ", breakTime=" + config.getBreakTimeBetweenExams());
        System.out.println("Classrooms available: " + classrooms.size());
        for (Classroom c : classrooms) {
            System.out.println("  - " + c.getName() + " (capacity: " + c.getCapacity() + ")");
        }

        buildRelationships(students, courses, enrollments);
        applyCourseDurations(courses, config);

        List<LocalDate> examDays = buildDateRange(startDate, endDate);
        if (examDays.isEmpty()) return new ScheduleResult(new LinkedHashMap<>(), new ArrayList<>());

        int maxExamsPerDay = config.getMaxExamsPerDay();
        int breakTimeMinutes = config.getBreakTimeBetweenExams();

        List<Course> sortedCourses = sortByEnrollment(courses);
        List<Course> unscheduledCourses = new ArrayList<>();

        Map<LocalDate, List<ExamSession>> result = new LinkedHashMap<>();
        List<ExamSession> allScheduledSessions = new ArrayList<>();

        int sessionId = 1;
        int partitionId = 1;
        int assignmentId = 1;

        // Track unique time slots that have been used (for bin-packing)
        Set<LocalDateTime> usedTimeSlots = new LinkedHashSet<>();

        // Try to schedule each course
        for (Course course : sortedCourses) {
            boolean placed = false;
            int studentCount = course.getStudents().size();
            int duration = course.getDurationMinutes();

            // FIRST: Try to pack into existing time slots (bin-packing priority)
            for (LocalDateTime existingStart : usedTimeSlots) {
                if (placed) break;

                LocalDate day = existingStart.toLocalDate();
                LocalDateTime endTime = existingStart.plusMinutes(duration);

                // Check if exam would exceed day boundary
                if (wouldExceedDayBoundary(existingStart, duration, config)) {
                    continue;
                }

                // Get sessions already at this time slot
                List<ExamSession> sessionsAtTime = getSessionsAtTime(existingStart, endTime, breakTimeMinutes, allScheduledSessions);

                // Check if course has student conflict with any session at this time
                if (courseHasStudentConflictWith(course, sessionsAtTime)) {
                    continue;
                }

                // Check student constraints
                boolean canSchedule = true;
                for (Student student : course.getStudents()) {
                    if (getStudentExamsOnDay(student, day, allScheduledSessions) >= maxExamsPerDay) {
                        canSchedule = false;
                        break;
                    }
                }

                if (!canSchedule) continue;

                // Try to assign rooms
                List<Classroom> assignedRooms = findAvailableRooms(
                    existingStart, endTime, studentCount, classrooms, breakTimeMinutes, allScheduledSessions
                );

                if (assignedRooms == null) {
                    continue; // Not enough room capacity at this time
                }

                // Create the exam session
                ExamSession session = new ExamSession(
                    sessionId++,
                    existingStart,
                    endTime,
                    duration,
                    course
                );

                // Add room partitions
                int remaining = studentCount;
                for (Classroom room : assignedRooms) {
                    int capacity = Math.min(room.getCapacity(), remaining);
                    session.addPartition(new ExamPartition(partitionId++, capacity, room));
                    remaining -= capacity;
                }

                // Assign students to partitions
                assignmentId = assignStudentsToPartitions(session, course.getStudents(), assignedRooms, assignmentId);

                // Validate partitions were added
                if (session.getPartitions().isEmpty()) {
                    System.out.println("ERROR: Session for " + course.getCourseName() + " has no partitions after creation!");
                }

                // Add to schedule
                allScheduledSessions.add(session);
                result.computeIfAbsent(day, k -> new ArrayList<>()).add(session);
                course.getExamSessions().add(session);

                System.out.println("Bin-packed: " + course.getCourseName() + " at " +
                    existingStart.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                    " in " + assignedRooms.stream().map(Classroom::getName).reduce((a, b) -> a + ", " + b).orElse("N/A") +
                    " (partitions: " + session.getPartitions().size() + ")");

                placed = true;
            }

            // SECOND: If bin-packing failed, try new time slots
            if (!placed) {
                for (LocalDate day : examDays) {
                    if (placed) break;

                    // Generate possible start times for this day
                    List<LocalDateTime> possibleStarts = generatePossibleStartTimes(day, config);

                    // Try each possible start time
                    for (LocalDateTime startTime : possibleStarts) {
                        if (placed) break;

                        LocalDateTime endTime = startTime.plusMinutes(duration);

                        // Check if exam would exceed day boundary
                        if (wouldExceedDayBoundary(startTime, duration, config)) {
                            continue;
                        }

                        boolean canSchedule = true;

                        // Check constraints for each student in this course
                        for (Student student : course.getStudents()) {
                            // Check if student has time conflict
                            if (studentHasConflictAt(student, startTime, endTime, breakTimeMinutes, allScheduledSessions)) {
                                canSchedule = false;
                                break;
                            }

                            // Check if student exceeds max exams per day
                            if (getStudentExamsOnDay(student, day, allScheduledSessions) >= maxExamsPerDay) {
                                canSchedule = false;
                                break;
                            }
                        }

                        if (!canSchedule) continue;

                        // Try to assign rooms
                        List<Classroom> assignedRooms = findAvailableRooms(
                            startTime, endTime, studentCount, classrooms, breakTimeMinutes, allScheduledSessions
                        );

                        if (assignedRooms == null) {
                            continue; // Not enough capacity
                        }

                        // Create the exam session
                        ExamSession session = new ExamSession(
                            sessionId++,
                            startTime,
                            endTime,
                            duration,
                            course
                        );

                        // Add room partitions
                        int remaining = studentCount;
                        for (Classroom room : assignedRooms) {
                            int capacity = Math.min(room.getCapacity(), remaining);
                            session.addPartition(new ExamPartition(partitionId++, capacity, room));
                            remaining -= capacity;
                        }

                        // Assign students to partitions
                        assignmentId = assignStudentsToPartitions(session, course.getStudents(), assignedRooms, assignmentId);

                        // Validate partitions were added
                        if (session.getPartitions().isEmpty()) {
                            System.out.println("ERROR: Session for " + course.getCourseName() + " has no partitions after creation!");
                        }

                        // Add to schedule
                        allScheduledSessions.add(session);
                        result.computeIfAbsent(day, k -> new ArrayList<>()).add(session);
                        course.getExamSessions().add(session);

                        // Track this time slot for future bin-packing
                        usedTimeSlots.add(startTime);

                        System.out.println("Scheduled: " + course.getCourseName() + " at " +
                            startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                            " in " + assignedRooms.stream().map(Classroom::getName).reduce((a, b) -> a + ", " + b).orElse("N/A") +
                            " (partitions: " + session.getPartitions().size() + ")");

                        placed = true;
                    }
                }
            }

            if (!placed) {
                unscheduledCourses.add(course);
                System.out.println("WARNING: Could not schedule " + course.getCourseName());
            }
        }

        // SECOND PASS: Pack unscheduled courses into existing time slots (bin-packing optimization)
        System.out.println("\n=== Second Pass: Bin-Packing Unscheduled Courses ===");
        List<Course> stillUnscheduled = new ArrayList<>(unscheduledCourses);
        unscheduledCourses.clear();

        for (Course course : stillUnscheduled) {
            boolean packed = false;
            int studentCount = course.getStudents().size();
            int duration = course.getDurationMinutes();

            // Try to find an existing time slot where this course fits
            for (LocalDate day : examDays) {
                if (packed) break;

                List<LocalDateTime> possibleStarts = generatePossibleStartTimes(day, config);

                for (LocalDateTime startTime : possibleStarts) {
                    if (packed) break;

                    LocalDateTime endTime = startTime.plusMinutes(duration);

                    // Check if exam would exceed day boundary
                    if (wouldExceedDayBoundary(startTime, duration, config)) {
                        continue;
                    }

                    // Get all sessions at this time slot
                    List<ExamSession> sessionsAtTime = getSessionsAtTime(startTime, endTime, breakTimeMinutes, allScheduledSessions);

                    // Check if course has student conflict with any session at this time
                    if (courseHasStudentConflictWith(course, sessionsAtTime)) {
                        continue;
                    }

                    // Check student constraints
                    boolean canSchedule = true;
                    for (Student student : course.getStudents()) {
                        // Check if student exceeds max exams per day (now includes new session)
                        int examsOnDay = getStudentExamsOnDay(student, day, allScheduledSessions);
                        if (examsOnDay >= maxExamsPerDay) {
                            canSchedule = false;
                            break;
                        }
                    }

                    if (!canSchedule) continue;

                    // Try to assign rooms
                    List<Classroom> assignedRooms = findAvailableRooms(
                        startTime, endTime, studentCount, classrooms, breakTimeMinutes, allScheduledSessions
                    );

                    if (assignedRooms == null) {
                        continue; // Not enough capacity
                    }

                    // Create the exam session
                    ExamSession session = new ExamSession(
                        sessionId++,
                        startTime,
                        endTime,
                        duration,
                        course
                    );

                    // Add room partitions
                    int remaining = studentCount;
                    for (Classroom room : assignedRooms) {
                        int capacity = Math.min(room.getCapacity(), remaining);
                        session.addPartition(new ExamPartition(partitionId++, capacity, room));
                        remaining -= capacity;
                    }

                    // Assign students to partitions
                    assignmentId = assignStudentsToPartitions(session, course.getStudents(), assignedRooms, assignmentId);

                    // Add to schedule
                    allScheduledSessions.add(session);
                    result.computeIfAbsent(day, k -> new ArrayList<>()).add(session);
                    course.getExamSessions().add(session);

                    System.out.println("✓ Packed " + course.getCourseName() + " at " + startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) +
                        " (bin-packed with other courses at this time)");

                    packed = true;
                }
            }

            if (!packed) {
                unscheduledCourses.add(course);
                System.out.println("✗ Still unscheduled: " + course.getCourseName());
            }
        }

        System.out.println("=== Schedule Complete ===");
        return new ScheduleResult(result, unscheduledCourses);
    }

    /* ===================== DATA BUILD ===================== */

    private void buildRelationships(
            List<Student> students,
            List<Course> courses,
            List<Enrollment> enrollments) {

        Map<Integer, Student> studentMap = new HashMap<>();
        for (Student s : students) studentMap.put(s.getId(), s);

        Map<String, Course> courseMap = new HashMap<>();
        for (Course c : courses) courseMap.put(c.getCourseName(), c);

        for (Enrollment e : enrollments) {
            Course c = courseMap.get(e.getCourseName());
            if (c == null) continue;

            for (int id : e.getStudentIds()) {
                Student s = studentMap.get(id);
                if (s != null) {
                    s.enrollInCourse(c);
                    c.addStudent(s);
                }
            }
        }
    }

    private void applyCourseDurations(List<Course> courses, ExamConfig config) {
        Map<String, Integer> durations = config.getCourseDurations();
        if (durations == null) return;

        for (Course c : courses) {
            Integer d = durations.get(c.getCourseName());
            if (d != null) c.setDurationMinutes(d);
        }
    }
}
