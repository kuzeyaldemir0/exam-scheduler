# Exam Scheduler

A desktop application for generating conflict-free exam timetables with automatic classroom assignment.

## Features

### Implemented
- **CSV Import**: Students, courses, classrooms, enrollments
- **Greedy Scheduling Algorithm**: Largest courses first
- **Constraint Checking**:
  - No student conflicts (same time slot)
  - Max 2 exams per day per student
  - No back-to-back exams
- **Classroom Assignment**: Automatic bin-packing by capacity
- **Room Splitting**: Large courses split across multiple classrooms
- **Calendar View**: Visual grid showing scheduled exams
- **User Configuration**: Date range selection, max exams per day
- **Comprehensive Testing**: Stress tests up to 10,000 students, 500 courses

### Pending
- Student schedule view (filter by student ID)
- Classroom schedule view (filter by room)
- PDF/CSV export
- Backtracking algorithm for hard cases

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| GUI | JavaFX |
| Database | SQLite |
| Build | Gradle |
| Testing | JUnit 5 |

## Project Structure

```
examschd/app/src/main/java/examschd/
├── controller/
│   ├── SchedulingController.java   # Main dashboard
│   ├── FileSelectController.java   # CSV import popup
│   └── FilterSettingsController.java
├── service/
│   ├── Scheduler.java              # Scheduling algorithm
│   ├── ImportService.java          # CSV → DB pipeline
│   └── readers/                    # CSV parsers
├── model/
│   ├── Student.java
│   ├── Course.java
│   ├── Classroom.java
│   ├── Enrollment.java
│   ├── ExamSession.java            # Scheduled exam
│   ├── ExamPartition.java          # Room assignment
│   └── ExamConfig.java             # User settings
├── dao/                            # Database interfaces
├── daoimpl/                        # SQLite implementations
└── db/
    └── DBInitializer.java          # Schema creation
```

## Algorithm

**Greedy with Constraint Propagation**

1. Sort courses by enrollment (largest first)
2. For each course, try day/slot combinations:
   - Check: No student has exam at same time
   - Check: No student exceeds max exams/day
   - Check: No back-to-back exams for any student
   - Check: Sufficient classroom capacity available
3. Assign classrooms using bin-packing (largest rooms first)
4. Split across multiple rooms if needed

**Time Complexity**: O(courses × days × slots × students)

### Partial Scheduling & Success Rates

If the algorithm cannot schedule all courses due to constraints:
- **Continues scheduling** remaining courses (doesn't fail entirely)
- **Returns partial schedule** with maximum courses that fit
- **Logs warnings** for unscheduled courses to console: `WARNING: Could not schedule [CourseName]`
- **User sees** what was successfully scheduled in calendar view

#### Expected Success Rates

| Scenario | Success Rate | Conditions |
|----------|-------------|------------|
| **Ideal** | **100%** | Small courses (30), ample time (14+ days), sufficient rooms |
| **Normal University** | **80-84%** | 50-100 courses, 14+ days, moderate conflicts |
| **Dense Conflicts** | **70%** | Many students share most courses |
| **Limited Time** | **57%** | Too many courses for available exam days |
| **Large Scale** | **56%** | 500+ courses, 10,000+ students |

#### Common Reasons for Failed Scheduling

1. **Insufficient time slots** → Too many courses for available exam days
2. **Dense student conflicts** → Many students enrolled in same courses
3. **Insufficient classroom capacity** → Not enough large rooms for big courses
4. **Tight constraints** → Max 2 exams/day + no back-to-back = limited options

#### Solutions When Courses Can't Be Scheduled

**Recommended actions (in order):**

1. **Extend exam period** → Add more days (14-21 days recommended)
   - Each additional day = 6 more time slots
   - Most effective for improving success rate

2. **Add more classrooms** or use larger rooms
   - Enables parallel scheduling of non-conflicting courses
   - Helps with capacity constraints

3. **Relax constraints** (if policy permits)
   - Increase max exams per day (2 → 3)
   - Allow back-to-back exams for specific cases

4. **Review course conflicts**
   - Identify courses with heavy student overlap
   - Consider splitting sections or alternative scheduling

**Viewing Failed Courses:**
Check console output for `WARNING: Could not schedule [CourseName]` messages after clicking "Generate Schedule".

## CSV Format

**students.csv**
```
ALL OF THE STUDENTS IN THE SYSTEM
Std_ID_001
Std_ID_002
...
```

**courses.csv**
```
ALL OF THE COURSES IN THE SYSTEM
CourseCode_01
CourseCode_02
...
```

**classrooms.csv** (tab-separated)
```
ALL OF THE CLASSROOMS; AND THEIR CAPACITIES IN THE SYSTEM
Classroom_01;40
Classroom_02;40
...
```

**enrollments.csv**
```
CourseCode_01
['Std_ID_001', 'Std_ID_002', ...]

CourseCode_02
['Std_ID_003', 'Std_ID_004', ...]
```

## Running

```bash
cd examschd
./gradlew run
```

## Testing

### Run All Tests

```bash
cd examschd
./gradlew test
```

### Run Specific Test Suites

```bash
# Run only SchedulerTest
./gradlew test --tests "examschd.service.SchedulerTest"

# Run only CSV reader tests
./gradlew test --tests "examschd.service.readers.*"

# Run a specific test method
./gradlew test --tests "examschd.service.SchedulerTest.testMediumScaleScheduling"
```

### View Test Results

After running tests, open the HTML report:

```bash
# macOS
open app/build/reports/tests/test/index.html

# Linux
xdg-open app/build/reports/tests/test/index.html

# Windows
start app/build/reports/tests/test/index.html
```

Or view the report at: `examschd/app/build/reports/tests/test/index.html`

### Test Coverage

**Basic Functional Tests:**
- CSV data scheduling (20 courses)
- Classroom splitting (100 students → multiple 30-capacity rooms)
- Student conflict detection (overlapping enrollments)
- Classroom reuse across time slots
- Insufficient capacity handling

**Comprehensive Stress Tests:**
- **Medium scale**: 1,000 students, 50 courses, varied classrooms (~25ms)
- **Large scale**: 10,000 students, 500 courses (~730ms)
- **Dense conflicts**: 500 students sharing most courses
- **Varied capacities**: 10-500 students per course
- **Limited time slots**: Scheduling with constrained availability

### Performance Benchmarks

| Scale | Students | Courses | Classrooms | Time |
|-------|----------|---------|------------|------|
| Small | 100 | 20 | 10 | <5ms |
| Medium | 1,000 | 50 | 20 | ~25ms |
| Large | 10,000 | 500 | 50 | ~730ms |

**All tests verify:**
- No student has conflicting exams (same time slot)
- Max exams per day constraint respected
- No back-to-back exams for any student
- Classroom capacity constraints honored
- Multiple classrooms used (not just Classroom_01)

### Test Data Generation

Tests use `TestDataGenerator` utility for programmatic data generation:

```java
examschd.util.TestDataGenerator.builder()
    .studentCount(1000)
    .courseCount(50)
    .classroomCount(20)
    .avgStudentsPerCourse(30, 15)  // mean=30, stddev=15
    .avgCoursesPerStudent(5, 2)    // mean=5, stddev=2
    .classroomCapacities(20, 30, 40, 50, 100)
    .seed(42)  // reproducible results
    .build()
    .generate();
```

This allows flexible, reproducible testing without large CSV files.

## Configuration

### User Workflow

1. **Import Data**: Upload CSV files (students, courses, classrooms, enrollments)
2. **Set Date Range**: Select start and end dates using date pickers
3. **Apply Date Range**: Click "Apply Date Range" to confirm exam period
4. **Generate Schedule**: Click "Generate Schedule" to run algorithm
5. **View Results**: Exam schedule appears in calendar grid

### Configuration Settings (ExamConfig)

| Setting | Default | Set By | Description |
|---------|---------|--------|-------------|
| **Start Date** | User-set | Date picker | First day of exam period |
| **End Date** | User-set | Date picker | Last day of exam period |
| **maxExamsPerDay** | 2 | Filter settings | Max exams per student per day |
| **slotsPerDay** | 6 | Hardcoded | Time slots available each day |
| **courseDurations** | 120 min | Config/Default | Per-course exam duration |

The scheduler uses these settings to:
- Determine available time slots (dates × slots per day)
- Enforce max exams per day constraint
- Allocate appropriate exam durations

## Key Decisions

1. **Greedy over Backtracking**: Simpler, faster for typical cases. Backtracking planned for edge cases.
2. **SQLite for Persistence**: Lightweight, no server needed. Data survives between sessions.
3. **Bin-packing for Rooms**: Largest rooms first minimizes room count.
4. **6 Slots/Day**: Hardcoded for now, configurable later.
