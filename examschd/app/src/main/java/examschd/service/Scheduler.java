package examschd.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.Enrollment;
import examschd.model.ExamConfig;
import examschd.model.ExamPartition;
import examschd.model.ExamSession;
import examschd.model.ScheduleResult;
import examschd.model.Student;

public class Scheduler {

    public Scheduler() {}

    /* ===================== HELPERS ===================== */

    private boolean studentHasMaxExamsOnDay(
            Map<String, List<ExamSession>> schedule,
            Student student,
            String dayKey,
            int maxExams,
            int slotsPerDay) {

        int count = 0;

        for (int slot = 1; slot <= slotsPerDay; slot++) {
            String key = dayKey + "-Slot" + slot;
            List<ExamSession> sessions = schedule.get(key);

            if (sessions != null) {
                for (ExamSession s : sessions) {
                    if (s.getCourse().getStudents().contains(student)) {
                        count++;
                        break;
                    }
                }
            }
        }

        return count >= maxExams;
    }

    private boolean studentHasBackToBackExam(
            Map<String, List<ExamSession>> schedule,
            Student student,
            String dayKey,
            int slot,
            int slotsPerDay) {

        if (slot > 1) {
            List<ExamSession> prev = schedule.get(dayKey + "-Slot" + (slot - 1));
            if (prev != null) {
                for (ExamSession s : prev) {
                    if (s.getCourse().getStudents().contains(student)) {
                        return true;
                    }
                }
            }
        }

        if (slot < slotsPerDay) {
            List<ExamSession> next = schedule.get(dayKey + "-Slot" + (slot + 1));
            if (next != null) {
                for (ExamSession s : next) {
                    if (s.getCourse().getStudents().contains(student)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private List<Course> sortByEnrollment(List<Course> courses) {
        List<Course> sorted = new ArrayList<>(courses);
        sorted.sort((a, b) ->
                Integer.compare(b.getStudents().size(), a.getStudents().size()));
        return sorted;
    }

    private List<Classroom> sortByCapacity(List<Classroom> rooms) {
        List<Classroom> sorted = new ArrayList<>(rooms);
        sorted.sort((a, b) ->
                Integer.compare(b.getCapacity(), a.getCapacity()));
        return sorted;
    }

    private List<Classroom> getAvailableClassrooms(
            List<Classroom> all,
            Map<String, Set<Classroom>> used,
            String slotKey) {

        Set<Classroom> busy = used.getOrDefault(slotKey, new HashSet<>());
        List<Classroom> available = new ArrayList<>();

        for (Classroom r : all) {
            if (!busy.contains(r)) {
                available.add(r);
            }
        }

        return sortByCapacity(available);
    }

    private List<Classroom> assignRooms(List<Classroom> rooms, int students) {
        List<Classroom> result = new ArrayList<>();
        int remaining = students;

        for (Classroom r : rooms) {
            if (remaining <= 0) break;
            result.add(r);
            remaining -= r.getCapacity();
        }

        return remaining > 0 ? null : result;
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

        buildRelationships(students, courses, enrollments);
        applyCourseDurations(courses, config);

        List<LocalDate> examDays = buildDateRange(startDate, endDate);
        if (examDays.isEmpty()) return new ScheduleResult(new LinkedHashMap<>(), new ArrayList<>());

        int maxExamsPerDay = config.getMaxExamsPerDay();
        int slotsPerDay = 6;

        List<Course> sortedCourses = sortByEnrollment(courses);
        List<Classroom> sortedRooms = sortByCapacity(classrooms);
        List<Course> unscheduledCourses = new ArrayList<>();

        Map<LocalDate, List<ExamSession>> result = new LinkedHashMap<>();
        Map<String, List<ExamSession>> slotMap = new HashMap<>();
        Map<String, Set<Classroom>> usedRooms = new HashMap<>();

        int sessionId = 1;
        int partitionId = 1;

        for (Course course : sortedCourses) {

            boolean placed = false;
            int studentCount = course.getStudents().size();

            for (LocalDate day : examDays) {
                String dayKey = day.toString();

                for (int slot = 1; slot <= slotsPerDay && !placed; slot++) {

                    String slotKey = dayKey + "-Slot" + slot;
                    boolean ok = true;

                    for (Student st : course.getStudents()) {

                        List<ExamSession> atSlot = slotMap.get(slotKey);
                        if (atSlot != null) {
                            for (ExamSession s : atSlot) {
                                if (s.getCourse().getStudents().contains(st)) {
                                    ok = false;
                                    break;
                                }
                            }
                        }

                        if (!ok) break;

                        if (studentHasMaxExamsOnDay(
                                slotMap, st, dayKey, maxExamsPerDay, slotsPerDay)) {
                            ok = false;
                            break;
                        }

                        if (studentHasBackToBackExam(
                                slotMap, st, dayKey, slot, slotsPerDay)) {
                            ok = false;
                            break;
                        }
                    }

                    List<Classroom> assigned = null;
                    if (ok && studentCount > 0) {
                        assigned = assignRooms(
                                getAvailableClassrooms(sortedRooms, usedRooms, slotKey),
                                studentCount
                        );
                        if (assigned == null) ok = false;
                    }

                    if (ok) {
                        Date examDate = Date.from(
                                day.atStartOfDay(ZoneId.systemDefault()).toInstant());

                        ExamSession session = new ExamSession(
                                sessionId++,
                                examDate,
                                "Slot" + slot,
                                course.getDurationMinutes(),
                                course
                        );

                        if (assigned != null) {
                            int remaining = studentCount;
                            for (Classroom r : assigned) {
                                int take = Math.min(r.getCapacity(), remaining);
                                session.addPartition(new ExamPartition(
                                        partitionId++, take, r));
                                remaining -= take;
                                usedRooms.computeIfAbsent(slotKey, k -> new HashSet<>()).add(r);
                            }
                        }

                        course.getExamSessions().add(session);
                        slotMap.computeIfAbsent(slotKey, k -> new ArrayList<>()).add(session);
                        result.computeIfAbsent(day, k -> new ArrayList<>()).add(session);

                        placed = true;
                    }
                }
            }

            if (!placed) {
                unscheduledCourses.add(course);
                System.out.println("WARNING: Could not schedule " + course.getCourseName());
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
