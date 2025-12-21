# Exam Scheduler

A desktop application for generating conflict-free exam timetables with automatic classroom assignment.

## Features

### Implemented
- **CSV Import**: Students, courses, classrooms, enrollments
- **Two-Phase Scheduling Architecture**:
  - **Phase 1**: Time slot assignment (student-centric, conflict-aware)
  - **Phase 2**: Classroom assignment (resource-aware, bin-packing)
- **Smart Conflict Resolution**: Most constrained courses scheduled first
- **Constraint Checking**:
  - No student conflicts (overlapping exams)
  - Max 2 exams per day per student
  - Minimum 90-minute gap between exams for students
  - 15-minute room turnover time between exams
- **Classroom Assignment**: Automatic bin-packing by capacity
- **Room Splitting**: Large courses split across multiple classrooms
- **Concurrent Exams**: Multiple exams can share time slots (different rooms, no student overlap)
- **Precise Capacity Tracking**: Phase 2 failures prevented by accurate Phase 1 capacity estimation
- **Calendar View**: Visual grid showing scheduled exams
- **Student Schedule View**: Filter exams by student ID
- **Classroom Schedule View**: Filter exams by room
- **User Configuration**: Date range selection, max exams per day
- **Help Dialog**: In-app guide explaining the workflow
- **Comprehensive Testing**: Stress tests up to 10,000 students, 500 courses (generated)
- **Demo Datasets**: Example CSVs including multi-room splitting and multi-day demo sets

### Pending
- PDF/CSV export

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

## Architecture

### Two-Phase Scheduling Approach

The scheduler separates the problem into two distinct phases for clarity, modularity, and robustness:

#### Phase 1: Time Slot Assignment
- **Goal**: Assign time slots to all courses
- **Heuristic**: "Most Constrained Variable First" (courses with most student conflicts scheduled first)
- **Constraints Checked**:
  - No student has overlapping exams (with 90-min gap requirement)
  - No student exceeds max exams/day
  - Sufficient total classroom capacity available
- **Optimization**: Bin-packing (tries to reuse existing time slots before creating new ones)
- **Output**: List of courses with assigned time slots (no specific rooms yet)

#### Phase 2: Classroom Assignment
- **Goal**: Assign specific classrooms to time-slotted courses
- **Input**: Time-slotted courses from Phase 1
- **Approach**: Groups courses by time slot, allocates rooms using bin-packing
- **Features**:
  - Multiple exams can share a time slot (if no student overlap)
  - Large courses automatically split across multiple classrooms
  - Largest rooms assigned first (bin-packing efficiency)
- **Output**: Final schedule with exam sessions and classroom assignments

### Algorithm Details

**Phase 1: Conflict-Based Sorting (Most Constrained Variable First)**

1. **Calculate conflict score** for each course:
   - Count how many OTHER courses share students with this course
   - Example: If CourseA has students enrolled in 8 other courses, conflict score = 8
   - Higher score = more constrained (harder to schedule) = schedule first

2. **Sort courses** by conflict score (descending - most conflicts first)
   - This is the "Most Constrained Variable First" heuristic from constraint satisfaction
   - Scheduling hard courses first leaves more flexibility for easier courses

3. **For each course**, try bin-packing into existing time slots, then new time slots:
   - Verify no student has overlapping exam with 90-min buffer
   - Verify no student exceeds max exams/day
   - Verify sufficient remaining capacity at this time slot

4. **Track result**: If successful, mark as time-slotted; otherwise mark as unscheduled

**Phase 2: Bin-Packed Room Allocation**

1. Group all time-slotted courses by their assigned time slot
2. For each time slot, find all available rooms
3. For each course at that time slot:
   - Allocate necessary rooms using bin-packing (largest rooms first)
   - Remove allocated rooms from the available pool
   - Create exam partitions and assign students to seats
4. Return final schedule with specific classroom assignments

**Time Complexity**:
- Phase 1: O(courses² × days × time_windows × students)
- Phase 2: O(courses × classrooms)
- Overall: O(courses² × days × time_windows × students)

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

1. **Insufficient exam time** → Too many courses for available exam days
2. **Dense student conflicts** → Many students enrolled in same courses
3. **Insufficient classroom capacity** → Not enough large rooms for big courses
4. **Tight constraints** → Max 2 exams/day + no back-to-back = limited options

#### Solutions When Courses Can't Be Scheduled

**Recommended actions (in order):**

1. **Extend exam period** → Add more days (14-21 days recommended)
   - Each additional day = more available exam hours
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

## Demo Datasets

Sample CSV files are provided in `app/src/test/resources/csv/` for testing and demonstration:

- **Base CSVs (root)**: Small default dataset used by tests
- **demo-multi-room-split/**: Forces room splitting for large courses
- **demo-9day-500s/**: 500 students, 40 courses, 12 rooms (minimum 9-day schedulable)
- **demo-14day-1000s/**: 1,000 students, 80 courses, 20 rooms (minimum 14-day schedulable)

The demo datasets use randomized, realistic overlaps (each student is in multiple courses).

## Running

### Quick Start (Recommended)

Simply double-click the launcher script for your platform:

- **Windows**: Double-click [run.bat](run.bat)
- **Mac**: Double-click [run.command](run.command)
- **Linux**: Right-click [run.sh](run.sh) → "Run as Program" (or run from terminal)

The launcher will automatically:
- Check if Java is installed
- Start the application
- Show helpful error messages if needed

### Manual Method

Alternatively, you can run from the command line:

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
- **Limited exam time**: Scheduling with constrained availability

### Performance Benchmarks

| Scale | Students | Courses | Classrooms | Time |
|-------|----------|---------|------------|------|
| Small | 100 | 20 | 10 | <5ms |
| Medium | 1,000 | 50 | 20 | ~25ms |
| Large | 10,000 | 500 | 50 | ~730ms |

**All tests verify:**
- No student has overlapping exams
- Max exams per day constraint respected
- Minimum 90-minute gap between exams for students
- Room turnover time (15 min) respected between exams
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
| **roomTurnoverMinutes** | 15 | Filter settings | Time for room changeover between exams |
| **studentMinGapMinutes** | 90 | Filter settings | Minimum gap between exams for same student |
| **examStartHour** | 9 | Filter settings | Exam day start time (24-hour format) |
| **examEndHour** | 21 | Filter settings | Exam day end time (24-hour format) |
| **courseDurations** | 90 min | Config/Default | Per-course exam duration |

The scheduler uses these settings to:
- Determine available exam windows (dates × exam hours)
- Enforce max exams per day constraint
- Ensure students have adequate rest between exams (90-min gap)
- Allow rooms to be prepared between exams (15-min turnover)
- Allocate appropriate exam durations

## Key Decisions

1. **Greedy over Backtracking**: Simpler, faster for typical cases. Backtracking planned for edge cases.
2. **SQLite for Persistence**: Lightweight, no server needed. Data survives between sessions.
3. **Bin-packing for Rooms**: Largest rooms first minimizes room count.
4. **Real-time Scheduling**: Exams scheduled at exact times (not fixed slots) within configurable exam hours (default 9:00-21:00).
