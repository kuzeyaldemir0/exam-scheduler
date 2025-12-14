package examschd.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.Enrollment;
import examschd.model.ExamConfig;
import examschd.model.ExamPartition;
import examschd.model.ExamSession;
import examschd.model.Student;

public class Scheduler {

    public Scheduler() {
    }

    // Check if student already has max or more exams on the given day
    // Overload with configurable parameters
    private boolean studentHasMaxExamsOnDay(Map<String, List<ExamSession>> schedule, Student student,
                                             String dayKey, int maxExams, int slotsPerDay) {
        int examCount = 0;

        for (int slot = 1; slot <= slotsPerDay; slot++) {
            String timeSlot = dayKey + "-Slot" + slot;
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

        return examCount >= maxExams;
    }


    // Check if a student would have a back-to-back exam at a specific time slot
    // Overload with configurable parameters
    private boolean studentHasBackToBackExam(Map<String, List<ExamSession>> schedule, Student student,
                                              String dayKey, int slot, int slotsPerDay) {
        // Check previous slot
        if (slot > 1) {
            String previousTimeSlot = dayKey + "-Slot" + (slot - 1);
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
        if (slot < slotsPerDay) {
            String nextTimeSlot = dayKey + "-Slot" + (slot + 1);
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
    private List<Course> sortByEnrollment(List<Course> courses) {
        List<Course> sorted = new ArrayList<>(courses);

        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i + 1; j < sorted.size(); j++) {
                if (sorted.get(i).getStudents().size() < sorted.get(j).getStudents().size()) {
                    Course temp = sorted.get(i);
                    sorted.set(i, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }

        return sorted;
    }

    // Sort classrooms by capacity (largest first for efficient bin-packing)
    private List<Classroom> sortByCapacity(List<Classroom> classrooms) {
        List<Classroom> sorted = new ArrayList<>(classrooms);

        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i + 1; j < sorted.size(); j++) {
                if (sorted.get(i).getCapacity() < sorted.get(j).getCapacity()) {
                    Classroom temp = sorted.get(i);
                    sorted.set(i, sorted.get(j));
                    sorted.set(j, temp);
                }
            }
        }

        return sorted;
    }

    // Get available classrooms for a specific time slot
    private List<Classroom> getAvailableClassrooms(List<Classroom> allClassrooms, Map<String, Set<Classroom>> usedClassrooms, String slotKey) {
        Set<Classroom> used = usedClassrooms.getOrDefault(slotKey, new HashSet<>());
        List<Classroom> available = new ArrayList<>();

        for (Classroom room : allClassrooms) {
            if (!used.contains(room)) {
                available.add(room);
            }
        }

        return sortByCapacity(available);
    }

    // Assign classrooms to fit all students (bin-packing: largest rooms first)
    // Returns list of assigned classrooms, or null if impossible
    private List<Classroom> assignClassroomsForStudents(List<Classroom> availableRooms, int studentCount) {
        List<Classroom> assigned = new ArrayList<>();
        int remaining = studentCount;

        for (Classroom room : availableRooms) {
            if (remaining <= 0) break;

            assigned.add(room);
            remaining -= room.getCapacity();
        }

        // Check if we covered all students
        if (remaining > 0) {
            return null; // Not enough capacity
        }

        return assigned;
    }

    // ==================== NEW METHOD FOR FRONTEND INTEGRATION ====================

    /**
     * Generate schedule with real data from frontend.
     * @return Map of LocalDate -> List of ExamSessions scheduled on that date
     */
    public Map<LocalDate, List<ExamSession>> generateSchedule(
            List<Student> students,
            List<Course> courses,
            List<Classroom> classrooms,
            List<Enrollment> enrollments,
            ExamConfig config) {

        System.out.println("=== Generating Schedule ===");
        System.out.println("Students: " + students.size() + ", Courses: " + courses.size() +
                          ", Classrooms: " + classrooms.size() + ", Enrollments: " + enrollments.size());

        // Step 1: Build student-course relationships from enrollments
        buildRelationships(students, courses, enrollments);

        // Step 2: Apply course durations from config
        applyCourseDurations(courses, config);

        // Step 3: Get allowed exam days
        List<LocalDate> allowedDays = getAllowedDays(config);
        if (allowedDays.isEmpty()) {
            System.out.println("ERROR: No allowed exam days!");
            return new LinkedHashMap<>();
        }

        // Step 4: Get config values
        int maxExamsPerDay = config.getMaxExamsPerDay();
        int slotsPerDay = 6;

        // Step 5: Sort courses by enrollment (greedy) and classrooms by capacity
        List<Course> sortedCourses = sortByEnrollment(courses);
        List<Classroom> sortedClassrooms = sortByCapacity(classrooms);

        // Step 6: Schedule each course
        Map<LocalDate, List<ExamSession>> result = new LinkedHashMap<>();
        Map<String, List<ExamSession>> slotSchedule = new HashMap<>();
        Map<String, Set<Classroom>> usedClassroomsPerSlot = new HashMap<>();
        int sessionIdCounter = 1;
        int partitionIdCounter = 1;

        for (Course course : sortedCourses) {
            boolean scheduled = false;
            int studentCount = course.getStudents().size();

            for (LocalDate day : allowedDays) {
                String dayKey = day.toString();

                for (int slot = 1; slot <= slotsPerDay && !scheduled; slot++) {
                    String slotKey = dayKey + "-Slot" + slot;

                    // Check constraints for all students in the course
                    boolean canSchedule = true;
                    for (Student student : course.getStudents()) {
                        // Conflict at same time?
                        List<ExamSession> sessionsAtSlot = slotSchedule.get(slotKey);
                        if (sessionsAtSlot != null) {
                            for (ExamSession s : sessionsAtSlot) {
                                if (s.getCourse().getStudents().contains(student)) {
                                    canSchedule = false;
                                    break;
                                }
                            }
                        }
                        if (!canSchedule) break;

                        // Max exams per day?
                        if (studentHasMaxExamsOnDay(slotSchedule, student, dayKey, maxExamsPerDay, slotsPerDay)) {
                            canSchedule = false;
                            break;
                        }

                        // Back-to-back?
                        if (studentHasBackToBackExam(slotSchedule, student, dayKey, slot, slotsPerDay)) {
                            canSchedule = false;
                            break;
                        }
                    }

                    // Check classroom availability
                    List<Classroom> assignedRooms = null;
                    if (canSchedule && studentCount > 0) {
                        List<Classroom> availableRooms = getAvailableClassrooms(sortedClassrooms, usedClassroomsPerSlot, slotKey);
                        assignedRooms = assignClassroomsForStudents(availableRooms, studentCount);

                        if (assignedRooms == null) {
                            canSchedule = false; // Not enough classroom capacity
                        }
                    }

                    if (canSchedule) {
                        Date examDate = Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        ExamSession session = new ExamSession(
                            sessionIdCounter++,
                            examDate,
                            "Slot" + slot,
                            course.getDurationMinutes(),
                            course
                        );

                        // Create partitions for each assigned classroom
                        if (assignedRooms != null) {
                            int studentsRemaining = studentCount;
                            for (Classroom room : assignedRooms) {
                                int studentsInRoom = Math.min(room.getCapacity(), studentsRemaining);
                                ExamPartition partition = new ExamPartition(
                                    partitionIdCounter++,
                                    studentsInRoom,
                                    room
                                );
                                session.addPartition(partition);
                                studentsRemaining -= studentsInRoom;

                                // Mark classroom as used for this slot
                                usedClassroomsPerSlot.computeIfAbsent(slotKey, k -> new HashSet<>()).add(room);
                            }
                        }

                        course.getExamSessions().add(session);
                        slotSchedule.computeIfAbsent(slotKey, k -> new ArrayList<>()).add(session);
                        result.computeIfAbsent(day, k -> new ArrayList<>()).add(session);

                        scheduled = true;

                        // Log with classroom info
                        StringBuilder roomInfo = new StringBuilder();
                        for (ExamPartition p : session.getPartitions()) {
                            if (roomInfo.length() > 0) roomInfo.append(", ");
                            roomInfo.append(p.getClassroom().getName())
                                    .append("(").append(p.getCapacityAssigned()).append(")");
                        }
                        System.out.println("Scheduled: " + course.getCourseName() +
                                           " (" + studentCount + " students) -> " + day + " Slot" + slot +
                                           " in [" + roomInfo + "]");
                    }
                }
            }

            if (!scheduled) {
                System.out.println("WARNING: Could not schedule " + course.getCourseName() +
                                   " (" + studentCount + " students)");
            }
        }

        System.out.println("=== Done: " + result.values().stream().mapToInt(List::size).sum() + " exams scheduled ===");
        return result;
    }

    // Build bidirectional Student-Course relationships from Enrollment data
    private void buildRelationships(List<Student> students, List<Course> courses, List<Enrollment> enrollments) {
        Map<Integer, Student> studentMap = new HashMap<>();
        for (Student s : students) {
            studentMap.put(s.getId(), s);
        }

        Map<String, Course> courseMap = new HashMap<>();
        for (Course c : courses) {
            courseMap.put(c.getCourseName(), c);
        }

        for (Enrollment enrollment : enrollments) {
            Course course = courseMap.get(enrollment.getCourseName());
            if (course == null) continue;

            int[] studentIds = enrollment.getStudentIds();
            if (studentIds != null) {
                for (int studentId : studentIds) {
                    Student student = studentMap.get(studentId);
                    if (student != null) {
                        student.enrollInCourse(course);
                        course.addStudent(student);
                    }
                }
            }
        }
    }

    // Apply course durations from ExamConfig
    private void applyCourseDurations(List<Course> courses, ExamConfig config) {
        Map<String, Integer> durations = config.getCourseDurations();
        if (durations == null) return;

        for (Course course : courses) {
            Integer duration = durations.get(course.getCourseName());
            if (duration != null) {
                course.setDurationMinutes(duration);
            }
        }
    }

    // Get sorted list of allowed exam days from config
    private List<LocalDate> getAllowedDays(ExamConfig config) {
        List<LocalDate> allowed = new ArrayList<>();
        Map<LocalDate, Boolean> allowedDays = config.getAllowedExamDays();
        if (allowedDays == null) return allowed;

        for (Map.Entry<LocalDate, Boolean> entry : allowedDays.entrySet()) {
            if (entry.getValue()) {
                allowed.add(entry.getKey());
            }
        }
        allowed.sort(LocalDate::compareTo);
        return allowed;
    }

}
