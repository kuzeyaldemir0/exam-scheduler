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

```bash
./gradlew test
```

Tests verify:
- CSV data scheduling
- Classroom splitting (100 students → multiple 30-capacity rooms)
- Student conflict detection
- Classroom reuse across slots
- Insufficient capacity handling

## Configuration (ExamConfig)

| Setting | Default | Description |
|---------|---------|-------------|
| maxExamsPerDay | 2 | Max exams per student per day |
| slotsPerDay | 6 | Time slots available each day |
| breakTimeBetweenExams | 30 | Minutes between exams (unused) |
| allowedExamDays | User-set | Which dates are exam days |
| courseDurations | 120 min | Per-course exam duration |

## Key Decisions

1. **Greedy over Backtracking**: Simpler, faster for typical cases. Backtracking planned for edge cases.
2. **SQLite for Persistence**: Lightweight, no server needed. Data survives between sessions.
3. **Bin-packing for Rooms**: Largest rooms first minimizes room count.
4. **6 Slots/Day**: Hardcoded for now, configurable later.
