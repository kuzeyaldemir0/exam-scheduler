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
import examschd.model.TimeSlottedExam;

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

    /**
     * Calculates the conflict score for a course.
     * The conflict score is the number of OTHER courses that share students with this course.
     * Higher conflict score = harder to schedule (more constrained).
     *
     * @param course the course to analyze
     * @param allCourses all courses in the system
     * @return the number of courses that have overlapping students
     */
    private int calculateConflictScore(Course course, List<Course> allCourses) {
        int conflictingCoursesCount = 0;

        for (Course otherCourse : allCourses) {
            // Skip comparing course with itself
            if (otherCourse.equals(course)) {
                continue;
            }

            // Check if this course and otherCourse share any students
            boolean hasSharedStudents = false;
            for (Student student : course.getStudents()) {
                if (otherCourse.getStudents().contains(student)) {
                    hasSharedStudents = true;
                    break;
                }
            }

            if (hasSharedStudents) {
                conflictingCoursesCount++;
            }
        }

        return conflictingCoursesCount;
    }

    /**
     * Sorts courses by their conflict score in descending order (most constrained first).
     * This is the "Most Constrained Variable First" heuristic from constraint satisfaction.
     * Courses with more conflicts are scheduled first, leaving more flexible courses for later.
     *
     * @param courses the courses to sort
     * @return courses sorted by conflict score (descending)
     */
    private List<Course> sortByConflicts(List<Course> courses) {
        List<Course> sorted = new ArrayList<>(courses);

        // Sort by conflict score descending (most conflicts first)
        sorted.sort((courseA, courseB) -> {
            int conflictA = calculateConflictScore(courseA, courses);
            int conflictB = calculateConflictScore(courseB, courses);
            return Integer.compare(conflictB, conflictA);
        });

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

    /* ===================== PHASE 1: TIME SLOT ASSIGNMENT ===================== */

    /**
     * Calculates the remaining classroom capacity available at a specific time slot.
     * This method tracks EXACTLY how much capacity is left after accounting for exams
     * already scheduled at this time. This precise tracking ensures Phase 2 will never
     * fail due to insufficient classroom capacity.
     *
     * @param startTime the start time of the time slot
     * @param endTime the end time of the time slot
     * @param allRooms all available classrooms
     * @param roomTurnoverMinutes buffer time needed between exams in same room
     * @param alreadyTimeSlottedExams exams that have already been assigned to a time slot
     * @return the remaining capacity available at this exact time slot (int)
     */
    private int getRemainingCapacityAtTimeSlot(
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<Classroom> allRooms,
            int roomTurnoverMinutes,
            List<TimeSlottedExam> alreadyTimeSlottedExams) {

        // Step 1: Calculate total capacity of all available rooms at this time
        int totalCapacityAtThisTime = 0;

        for (Classroom currentRoom : allRooms) {
            boolean isRoomAvailable = true;

            // Check if this room is already committed to another exam at this time
            LocalDateTime endTimeWithTurnover = endTime.plusMinutes(roomTurnoverMinutes);

            for (TimeSlottedExam existingExam : alreadyTimeSlottedExams) {
                // Check if the existing exam's time window overlaps with our requested time
                LocalDateTime existingEndWithTurnover = existingExam.getEndTime().plusMinutes(roomTurnoverMinutes);

                // If time windows overlap, this room cannot be used
                if (!(endTimeWithTurnover.isBefore(existingExam.getStartTime()) ||
                      existingEndWithTurnover.isBefore(startTime))) {
                    isRoomAvailable = false;
                    break;
                }
            }

            if (isRoomAvailable) {
                totalCapacityAtThisTime += currentRoom.getCapacity();
            }
        }

        // Step 2: Subtract capacity already committed to exams at this exact time slot
        int capacityCommitted = 0;

        for (TimeSlottedExam existingExam : alreadyTimeSlottedExams) {
            // Only count exams that are scheduled at this exact same time slot
            if (existingExam.getStartTime().equals(startTime)) {
                capacityCommitted += existingExam.getStudentCount();
            }
        }

        // Step 3: Return the remaining available capacity
        int remainingCapacity = totalCapacityAtThisTime - capacityCommitted;
        return Math.max(0, remainingCapacity); // Never return negative
    }

    /**
     * Phase 1 of scheduling: Assigns time slots to all courses.
     * This method checks student constraints and classroom capacity to determine
     * if a course can be scheduled at a specific time.
     *
     * @param sortedCourses courses to schedule (sorted by enrollment size)
     * @param allRooms all available classrooms
     * @param examDays all available exam days
     * @param config scheduling configuration with constraints
     * @return list of exams with assigned time slots (no classrooms yet)
     */
    private List<TimeSlottedExam> assignTimeSlots(
            List<Course> sortedCourses,
            List<Classroom> allRooms,
            List<LocalDate> examDays,
            ExamConfig config) {

        System.out.println("\n=== PHASE 1: TIME SLOT ASSIGNMENT ===");

        List<TimeSlottedExam> timeSlottedExams = new ArrayList<>();
        Set<LocalDateTime> usedTimeSlots = new LinkedHashSet<>();

        int maxExamsPerDay = config.getMaxExamsPerDay();
        int roomTurnoverMinutes = config.getRoomTurnoverMinutes();
        int studentGapMinutes = config.getStudentMinGapMinutes();

        // Try to schedule each course
        for (Course currentCourse : sortedCourses) {
            boolean hasBeenScheduled = false;
            int studentCount = currentCourse.getStudents().size();
            int durationMinutes = currentCourse.getDurationMinutes();

            // STRATEGY 1: Try to pack into existing time slots (bin-packing for efficiency)
            for (LocalDateTime existingSlotStart : usedTimeSlots) {
                if (hasBeenScheduled) {
                    break; // Already scheduled this course, move to next
                }

                LocalDate dayOfExam = existingSlotStart.toLocalDate();
                LocalDateTime slotEnd = existingSlotStart.plusMinutes(durationMinutes);

                // Check 1: Would the exam exceed the day boundary?
                if (wouldExceedDayBoundary(existingSlotStart, durationMinutes, config)) {
                    continue; // Try next time slot
                }

                // Check 2: Do any students in this course already have exams at this time?
                List<ExamSession> sessionsAtThisTime = getSessionsAtTime(
                    existingSlotStart, slotEnd, studentGapMinutes, new ArrayList<>()
                );

                if (courseHasStudentConflictWith(currentCourse, sessionsAtThisTime)) {
                    continue; // Try next time slot
                }

                // Check 3: Does any student exceed max exams per day?
                boolean studentExceedsMaxPerDay = false;
                for (Student currentStudent : currentCourse.getStudents()) {
                    int examsAlreadyOnThisDay = 0;
                    for (TimeSlottedExam alreadyScheduled : timeSlottedExams) {
                        if (alreadyScheduled.getStartTime().toLocalDate().equals(dayOfExam)) {
                            boolean studentTakesThisCourse = alreadyScheduled.getCourse()
                                .getStudents()
                                .contains(currentStudent);
                            if (studentTakesThisCourse) {
                                examsAlreadyOnThisDay++;
                            }
                        }
                    }

                    if (examsAlreadyOnThisDay >= maxExamsPerDay) {
                        studentExceedsMaxPerDay = true;
                        break;
                    }
                }

                if (studentExceedsMaxPerDay) {
                    continue; // Try next time slot
                }

                // Check 4: Is there enough remaining capacity at this time slot?
                int remainingCapacity = getRemainingCapacityAtTimeSlot(
                    existingSlotStart, slotEnd, allRooms, roomTurnoverMinutes, timeSlottedExams
                );

                if (remainingCapacity < studentCount) {
                    continue; // Not enough room, try next time slot
                }

                // SUCCESS: Schedule the exam at this existing time slot
                TimeSlottedExam timeSlottedExam = new TimeSlottedExam(
                    currentCourse,
                    existingSlotStart,
                    slotEnd,
                    durationMinutes,
                    studentCount
                );

                timeSlottedExams.add(timeSlottedExam);
                System.out.println("✓ Phase 1 Bin-packed: " + currentCourse.getCourseName() +
                    " at " + existingSlotStart.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                hasBeenScheduled = true;
            }

            // STRATEGY 2: If bin-packing failed, try new time slots
            if (!hasBeenScheduled) {
                for (LocalDate examDay : examDays) {
                    if (hasBeenScheduled) {
                        break; // Already scheduled, move to next course
                    }

                    List<LocalDateTime> possibleStartTimes = generatePossibleStartTimes(examDay, config);

                    for (LocalDateTime startTime : possibleStartTimes) {
                        if (hasBeenScheduled) {
                            break; // Already scheduled
                        }

                        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

                        // Check 1: Would exam exceed day boundary?
                        if (wouldExceedDayBoundary(startTime, durationMinutes, config)) {
                            continue;
                        }

                        // Check 2: Does each student in this course have any conflicts?
                        boolean hasStudentConflict = false;
                        for (Student currentStudent : currentCourse.getStudents()) {
                            // Check if student has overlapping exam with gap buffer
                            if (studentHasConflictAt(currentStudent, startTime, endTime, studentGapMinutes, new ArrayList<>())) {
                                hasStudentConflict = true;
                                break;
                            }

                            // Check if student exceeds max exams per day
                            int examsOnThisDay = 0;
                            for (TimeSlottedExam alreadyScheduled : timeSlottedExams) {
                                if (alreadyScheduled.getStartTime().toLocalDate().equals(examDay)) {
                                    if (alreadyScheduled.getCourse().getStudents().contains(currentStudent)) {
                                        examsOnThisDay++;
                                    }
                                }
                            }
                            if (examsOnThisDay >= maxExamsPerDay) {
                                hasStudentConflict = true;
                                break;
                            }
                        }

                        if (hasStudentConflict) {
                            continue;
                        }

                        // Check 3: Is there enough remaining capacity?
                        int remainingCapacity = getRemainingCapacityAtTimeSlot(
                            startTime, endTime, allRooms, roomTurnoverMinutes, timeSlottedExams
                        );

                        if (remainingCapacity < studentCount) {
                            continue; // Not enough capacity
                        }

                        // SUCCESS: Create new time slot
                        TimeSlottedExam timeSlottedExam = new TimeSlottedExam(
                            currentCourse,
                            startTime,
                            endTime,
                            durationMinutes,
                            studentCount
                        );

                        timeSlottedExams.add(timeSlottedExam);
                        usedTimeSlots.add(startTime); // Track this time slot for future bin-packing

                        System.out.println("✓ Phase 1 Scheduled: " + currentCourse.getCourseName() +
                            " at " + startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                        hasBeenScheduled = true;
                    }
                }
            }

            if (!hasBeenScheduled) {
                System.out.println("✗ Phase 1 Failed: " + currentCourse.getCourseName() +
                    " could not be time-slotted");
            }
        }

        System.out.println("=== PHASE 1 COMPLETE: " + timeSlottedExams.size() + "/" +
            sortedCourses.size() + " courses time-slotted ===\n");

        return timeSlottedExams;
    }

    /* ===================== PHASE 2: CLASSROOM ASSIGNMENT ===================== */

    /**
     * Phase 2 of scheduling: Assigns specific classrooms to time-slotted exams.
     * This method takes the time-slotted exams from Phase 1 and assigns them to
     * specific classrooms using bin-packing for efficiency.
     *
     * @param timeSlottedExams exams with assigned time slots (from Phase 1)
     * @param allRooms all available classrooms
     * @param config scheduling configuration
     * @param sessionIdStart starting ID for exam sessions
     * @param partitionIdStart starting ID for partitions
     * @param assignmentIdStart starting ID for student assignments
     * @return map of dates to lists of scheduled exam sessions
     */
    private Map<LocalDate, List<ExamSession>> assignClassrooms(
            List<TimeSlottedExam> timeSlottedExams,
            List<Classroom> allRooms,
            ExamConfig config,
            int sessionIdStart,
            int partitionIdStart,
            int assignmentIdStart) {

        System.out.println("\n=== PHASE 2: CLASSROOM ASSIGNMENT ===");

        Map<LocalDate, List<ExamSession>> finalSchedule = new LinkedHashMap<>();
        List<ExamSession> allExamSessions = new ArrayList<>();

        int currentSessionId = sessionIdStart;
        int currentPartitionId = partitionIdStart;
        int currentAssignmentId = assignmentIdStart;

        int roomTurnoverMinutes = config.getRoomTurnoverMinutes();

        // Step 1: Group time-slotted exams by their time slot
        Map<LocalDateTime, List<TimeSlottedExam>> examsByTimeSlot = new LinkedHashMap<>();

        for (TimeSlottedExam timeSlottedExam : timeSlottedExams) {
            LocalDateTime slotStart = timeSlottedExam.getStartTime();

            // Add to the group for this time slot
            examsByTimeSlot.computeIfAbsent(slotStart, k -> new ArrayList<>())
                .add(timeSlottedExam);
        }

        // Step 2: For each time slot, assign classrooms to all exams at that time
        for (Map.Entry<LocalDateTime, List<TimeSlottedExam>> slotEntry : examsByTimeSlot.entrySet()) {
            LocalDateTime slotStart = slotEntry.getKey();
            List<TimeSlottedExam> examsAtThisSlot = slotEntry.getValue();

            // Find all rooms that are available during this entire time slot
            List<Classroom> availableRoomsAtThisSlot = findAvailableRoomsForSlot(
                slotStart,
                slotStart.plusMinutes(60), // Estimate end time for finding available rooms
                allRooms,
                roomTurnoverMinutes,
                allExamSessions
            );

            // Assign classrooms to each exam at this time slot using bin-packing
            for (TimeSlottedExam currentExam : examsAtThisSlot) {
                int studentsInThisCourse = currentExam.getStudentCount();
                LocalDateTime examEnd = currentExam.getEndTime();

                // Allocate rooms for this specific exam (removes them from available pool)
                List<Classroom> allocatedRooms = allocateRoomsForExam(
                    studentsInThisCourse,
                    availableRoomsAtThisSlot
                );

                if (allocatedRooms == null) {
                    // PHASE 2 FAILURE - This should not happen if Phase 1 capacity tracking is correct
                    System.err.println("PHASE 2 FAILURE: Cannot assign rooms for " +
                        currentExam.getCourse().getCourseName() +
                        " - THIS INDICATES A BUG IN PHASE 1 CAPACITY TRACKING!");
                    continue;
                }

                // Create the exam session with assigned rooms
                ExamSession examSession = new ExamSession(
                    currentSessionId++,
                    slotStart,
                    examEnd,
                    currentExam.getDurationMinutes(),
                    currentExam.getCourse()
                );

                // Add each allocated room as a partition in this exam session
                int remainingStudents = studentsInThisCourse;
                for (Classroom allocatedRoom : allocatedRooms) {
                    int capacityToAssign = Math.min(allocatedRoom.getCapacity(), remainingStudents);

                    ExamPartition partition = new ExamPartition(
                        currentPartitionId++,
                        capacityToAssign,
                        allocatedRoom
                    );

                    examSession.addPartition(partition);
                    remainingStudents -= capacityToAssign;
                }

                // Assign students to the partitions they'll sit in
                currentAssignmentId = assignStudentsToPartitions(
                    examSession,
                    currentExam.getCourse().getStudents(),
                    allocatedRooms,
                    currentAssignmentId
                );

                // Add to the full schedule
                allExamSessions.add(examSession);
                LocalDate examDate = slotStart.toLocalDate();
                finalSchedule.computeIfAbsent(examDate, k -> new ArrayList<>())
                    .add(examSession);

                // Update the course's exam sessions
                currentExam.getCourse().getExamSessions().add(examSession);

                System.out.println("✓ Phase 2 Assigned: " + currentExam.getCourse().getCourseName() +
                    " in " + allocatedRooms.size() + " room(s)");
            }
        }

        System.out.println("=== PHASE 2 COMPLETE: " + allExamSessions.size() +
            " exams assigned to classrooms ===\n");

        return finalSchedule;
    }

    /**
     * Finds all classrooms that are available (not occupied) during a specific time window.
     *
     * @param startTime start of the time window
     * @param endTime end of the time window
     * @param allRooms all available classrooms
     * @param roomTurnoverMinutes buffer time needed between exams
     * @param allScheduledSessions all exams already scheduled
     * @return list of available classrooms at this time
     */
    private List<Classroom> findAvailableRoomsForSlot(
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<Classroom> allRooms,
            int roomTurnoverMinutes,
            List<ExamSession> allScheduledSessions) {

        List<Classroom> availableRooms = new ArrayList<>();
        LocalDateTime endTimeWithTurnover = endTime.plusMinutes(roomTurnoverMinutes);

        // Check each room to see if it's available
        for (Classroom currentRoom : allRooms) {
            boolean isRoomOccupied = false;

            // Look through all scheduled exams to see if this room is in use
            for (ExamSession scheduledSession : allScheduledSessions) {
                for (ExamPartition partition : scheduledSession.getPartitions()) {
                    if (partition.getClassroom().equals(currentRoom)) {
                        // This room is used by the scheduled session
                        // Check if the time windows overlap
                        LocalDateTime sessionEndWithTurnover =
                            scheduledSession.getEndTime().plusMinutes(roomTurnoverMinutes);

                        if (!(endTimeWithTurnover.isBefore(scheduledSession.getStartTime()) ||
                              sessionEndWithTurnover.isBefore(startTime))) {
                            // Time windows overlap - room is occupied
                            isRoomOccupied = true;
                            break;
                        }
                    }
                }
                if (isRoomOccupied) break;
            }

            if (!isRoomOccupied) {
                availableRooms.add(currentRoom);
            }
        }

        // Sort rooms by capacity (largest first) for bin-packing efficiency
        availableRooms.sort((roomA, roomB) ->
            Integer.compare(roomB.getCapacity(), roomA.getCapacity())
        );

        return availableRooms;
    }

    /**
     * Allocates classrooms for a single exam using bin-packing.
     * Selects the minimum set of rooms (largest first) needed to accommodate all students.
     * MODIFIES the input list by removing allocated rooms.
     *
     * @param studentCount number of students needing seats
     * @param availableRooms list of available classrooms (will be modified)
     * @return list of allocated rooms, or null if insufficient capacity
     */
    private List<Classroom> allocateRoomsForExam(
            int studentCount,
            List<Classroom> availableRooms) {

        List<Classroom> allocatedRooms = new ArrayList<>();
        int remainingStudents = studentCount;

        // Bin-pack: take the largest available rooms first until all students fit
        for (int i = 0; i < availableRooms.size() && remainingStudents > 0; i++) {
            Classroom currentRoom = availableRooms.get(i);
            allocatedRooms.add(currentRoom);
            remainingStudents -= currentRoom.getCapacity();
        }

        // Remove allocated rooms from the available pool
        availableRooms.removeAll(allocatedRooms);

        // Check if we have enough capacity for all students
        if (remainingStudents > 0) {
            return null; // Not enough capacity
        }

        return allocatedRooms;
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
            ", roomTurnover=" + config.getRoomTurnoverMinutes() +
            "min, studentGap=" + config.getStudentMinGapMinutes() + "min");
        System.out.println("Classrooms available: " + classrooms.size());
        for (Classroom c : classrooms) {
            System.out.println("  - " + c.getName() + " (capacity: " + c.getCapacity() + ")");
        }

        buildRelationships(students, courses, enrollments);
        applyCourseDurations(courses, config);

        List<LocalDate> examDays = buildDateRange(startDate, endDate);
        if (examDays.isEmpty()) return new ScheduleResult(new LinkedHashMap<>(), new ArrayList<>());

        // Sort courses by conflict score (most constrained first) for better scheduling
        List<Course> sortedCourses = sortByConflicts(courses);

        // PHASE 1: Time Slot Assignment
        List<TimeSlottedExam> timeSlottedExams = assignTimeSlots(
            sortedCourses, classrooms, examDays, config
        );

        // PHASE 2: Classroom Assignment
        Map<LocalDate, List<ExamSession>> result = assignClassrooms(
            timeSlottedExams, classrooms, config, 1, 1, 1
        );

        // Track unscheduled courses (those that Phase 1 could not time-slot)
        Set<Course> scheduledCourses = new HashSet<>();
        for (TimeSlottedExam timeSlottedExam : timeSlottedExams) {
            scheduledCourses.add(timeSlottedExam.getCourse());
        }

        List<Course> unscheduledCourses = new ArrayList<>();
        for (Course course : sortedCourses) {
            if (!scheduledCourses.contains(course)) {
                unscheduledCourses.add(course);
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
