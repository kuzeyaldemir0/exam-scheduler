package examschd.util;

import examschd.model.Classroom;
import examschd.model.Course;
import examschd.model.Enrollment;
import examschd.model.Student;

import java.util.*;

/**
 * Utility for generating realistic test data for stress testing the scheduler.
 * Uses a builder pattern for flexible configuration.
 */
public class TestDataGenerator {

    private final int studentCount;
    private final int courseCount;
    private final int classroomCount;
    private final int avgStudentsPerCourse;
    private final int stdDevStudentsPerCourse;
    private final int avgCoursesPerStudent;
    private final int stdDevCoursesPerStudent;
    private final int[] classroomCapacities;
    private final Random random;

    private TestDataGenerator(Builder builder) {
        this.studentCount = builder.studentCount;
        this.courseCount = builder.courseCount;
        this.classroomCount = builder.classroomCount;
        this.avgStudentsPerCourse = builder.avgStudentsPerCourse;
        this.stdDevStudentsPerCourse = builder.stdDevStudentsPerCourse;
        this.avgCoursesPerStudent = builder.avgCoursesPerStudent;
        this.stdDevCoursesPerStudent = builder.stdDevCoursesPerStudent;
        this.classroomCapacities = builder.classroomCapacities;
        this.random = new Random(builder.seed);
    }

    public static Builder builder() {
        return new Builder();
    }

    public GeneratedData generate() {
        List<Student> students = generateStudents();
        List<Course> courses = generateCourses();
        List<Classroom> classrooms = generateClassrooms();
        List<Enrollment> enrollments = generateEnrollments(students, courses);

        // Build bidirectional relationships
        buildRelationships(students, courses, enrollments);

        return new GeneratedData(students, courses, classrooms, enrollments);
    }

    private List<Student> generateStudents() {
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= studentCount; i++) {
            students.add(new Student(i, String.format("Student_%05d", i)));
        }
        return students;
    }

    private List<Course> generateCourses() {
        List<Course> courses = new ArrayList<>();
        for (int i = 1; i <= courseCount; i++) {
            Course course = new Course(i, String.format("Course_%04d", i));
            // Random duration between 60-180 minutes
            course.setDurationMinutes(60 + random.nextInt(121));
            courses.add(course);
        }
        return courses;
    }

    private List<Classroom> generateClassrooms() {
        List<Classroom> classrooms = new ArrayList<>();
        for (int i = 1; i <= classroomCount; i++) {
            // Pick a random capacity from the configured options
            int capacity = classroomCapacities[random.nextInt(classroomCapacities.length)];
            classrooms.add(new Classroom(i, String.format("Room_%03d", i), capacity));
        }
        return classrooms;
    }

    private List<Enrollment> generateEnrollments(List<Student> students, List<Course> courses) {
        // Strategy: For each course, decide how many students based on normal distribution
        // Then randomly select that many students

        Map<Course, Set<Student>> courseToStudents = new HashMap<>();

        // Initialize empty sets
        for (Course course : courses) {
            courseToStudents.put(course, new HashSet<>());
        }

        // For each course, assign students
        for (Course course : courses) {
            int targetSize = Math.max(1, (int) (random.nextGaussian() * stdDevStudentsPerCourse + avgStudentsPerCourse));
            targetSize = Math.min(targetSize, studentCount); // Cap at total students

            Set<Student> enrolled = new HashSet<>();
            List<Student> shuffled = new ArrayList<>(students);
            Collections.shuffle(shuffled, random);

            for (int i = 0; i < targetSize && i < shuffled.size(); i++) {
                enrolled.add(shuffled.get(i));
            }

            courseToStudents.get(course).addAll(enrolled);
        }

        // Also ensure each student has around avgCoursesPerStudent courses
        // (optional balancing pass)
        balanceStudentCourseLoad(students, courses, courseToStudents);

        // Convert to Enrollment objects
        List<Enrollment> enrollments = new ArrayList<>();
        int enrollmentId = 1;
        for (Map.Entry<Course, Set<Student>> entry : courseToStudents.entrySet()) {
            Course course = entry.getKey();
            Set<Student> enrolledStudents = entry.getValue();

            int[] studentIds = enrolledStudents.stream()
                    .mapToInt(Student::getId)
                    .toArray();

            Enrollment enrollment = new Enrollment(enrollmentId++, studentIds, course.getCourseName());
            enrollments.add(enrollment);
        }

        return enrollments;
    }

    private void balanceStudentCourseLoad(List<Student> students, List<Course> courses,
                                          Map<Course, Set<Student>> courseToStudents) {
        // For each student, check course count and adjust
        for (Student student : students) {
            int currentCourses = 0;
            for (Set<Student> enrolled : courseToStudents.values()) {
                if (enrolled.contains(student)) {
                    currentCourses++;
                }
            }

            int targetCourses = Math.max(1, (int) (random.nextGaussian() * stdDevCoursesPerStudent + avgCoursesPerStudent));
            targetCourses = Math.min(targetCourses, courseCount);

            // If student has too few courses, add more
            if (currentCourses < targetCourses) {
                List<Course> availableCourses = new ArrayList<>();
                for (Course course : courses) {
                    if (!courseToStudents.get(course).contains(student)) {
                        availableCourses.add(course);
                    }
                }
                Collections.shuffle(availableCourses, random);

                int toAdd = Math.min(targetCourses - currentCourses, availableCourses.size());
                for (int i = 0; i < toAdd; i++) {
                    courseToStudents.get(availableCourses.get(i)).add(student);
                }
            }
        }
    }

    private void buildRelationships(List<Student> students, List<Course> courses,
                                     List<Enrollment> enrollments) {
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

    public static class Builder {
        private int studentCount = 100;
        private int courseCount = 20;
        private int classroomCount = 10;
        private int avgStudentsPerCourse = 25;
        private int stdDevStudentsPerCourse = 10;
        private int avgCoursesPerStudent = 5;
        private int stdDevCoursesPerStudent = 2;
        private int[] classroomCapacities = {30, 40, 50, 100};
        private long seed = 42;

        public Builder studentCount(int count) {
            this.studentCount = count;
            return this;
        }

        public Builder courseCount(int count) {
            this.courseCount = count;
            return this;
        }

        public Builder classroomCount(int count) {
            this.classroomCount = count;
            return this;
        }

        public Builder avgStudentsPerCourse(int avg, int stdDev) {
            this.avgStudentsPerCourse = avg;
            this.stdDevStudentsPerCourse = stdDev;
            return this;
        }

        public Builder avgCoursesPerStudent(int avg, int stdDev) {
            this.avgCoursesPerStudent = avg;
            this.stdDevCoursesPerStudent = stdDev;
            return this;
        }

        public Builder classroomCapacities(int... capacities) {
            this.classroomCapacities = capacities;
            return this;
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public TestDataGenerator build() {
            return new TestDataGenerator(this);
        }
    }

    public static class GeneratedData {
        private final List<Student> students;
        private final List<Course> courses;
        private final List<Classroom> classrooms;
        private final List<Enrollment> enrollments;

        public GeneratedData(List<Student> students, List<Course> courses,
                             List<Classroom> classrooms, List<Enrollment> enrollments) {
            this.students = students;
            this.courses = courses;
            this.classrooms = classrooms;
            this.enrollments = enrollments;
        }

        public List<Student> getStudents() {
            return students;
        }

        public List<Course> getCourses() {
            return courses;
        }

        public List<Classroom> getClassrooms() {
            return classrooms;
        }

        public List<Enrollment> getEnrollments() {
            return enrollments;
        }

        public void printStatistics() {
            System.out.println("=== Generated Test Data Statistics ===");
            System.out.println("Students: " + students.size());
            System.out.println("Courses: " + courses.size());
            System.out.println("Classrooms: " + classrooms.size());

            // Course enrollment stats
            int minEnrollment = courses.stream().mapToInt(c -> c.getStudents().size()).min().orElse(0);
            int maxEnrollment = courses.stream().mapToInt(c -> c.getStudents().size()).max().orElse(0);
            double avgEnrollment = courses.stream().mapToInt(c -> c.getStudents().size()).average().orElse(0);

            System.out.println("Course enrollments: min=" + minEnrollment +
                    ", max=" + maxEnrollment +
                    ", avg=" + String.format("%.1f", avgEnrollment));

            // Student course load stats
            int minCourses = students.stream().mapToInt(s -> s.getEnrolledCourses().size()).min().orElse(0);
            int maxCourses = students.stream().mapToInt(s -> s.getEnrolledCourses().size()).max().orElse(0);
            double avgCourses = students.stream().mapToInt(s -> s.getEnrolledCourses().size()).average().orElse(0);

            System.out.println("Student course loads: min=" + minCourses +
                    ", max=" + maxCourses +
                    ", avg=" + String.format("%.1f", avgCourses));

            // Classroom capacity distribution
            Map<Integer, Long> capacityDist = new HashMap<>();
            for (Classroom c : classrooms) {
                capacityDist.merge(c.getCapacity(), 1L, Long::sum);
            }
            System.out.println("Classroom capacity distribution: " + capacityDist);
        }
    }
}
