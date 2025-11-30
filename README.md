# Exam Scheduler – Project Document 📑

## 1. Purpose of the Project

The **Exam Scheduler** system assists student affairs staff in creating conflict-free exam timetables. It ensures that:

* Students do not have overlapping exams,
* Rooms are assigned appropriately based on capacity,
* Exams are placed into valid time slots,
* The exam week schedule can be viewed clearly in a structured calendar format.

---

## 2. Project Overview

The Exam Scheduler is a **desktop application built using Java and JavaFX**. The main workflow:

1. Student affairs uploads CSV data (students, courses, classrooms, attendance lists).
2. The system analyzes conflicts, capacities, and time slot availability.
3. A weekly exam schedule is generated.
4. Staff can view, filter, export, and revise the schedule.

Main modules:

* **Data Import Module** – Handles CSV inputs.
* **Scheduling Engine** – Detects conflicts and assigns slots.
* **Calendar View** – Displays the generated weekly exam schedule.
* **Export Module** – Outputs PDF/CSV reports.

---

## 3. User Role

### **Student Affairs Staff (Single User Role)**

Since this project focuses only on exam scheduling, there is **one primary user type: Student Affairs Staff**.

#### Responsibilities:

* Upload required CSV files (students, courses, classrooms, attendance).
* Start the scheduling process.
* Review conflicts and warnings.
* Manually adjust exam slots if needed.
* Export the final exam schedule.
* Allow student affairs to filter and search.

There are **no students or instructors logging into the system**. The system is operated solely by student affairs.

---

## 4. Technical Notes

* **Language:** Java
* **GUI:** JavaFX (Scene Builder used for layout)
* **Data Source:** CSV files
* **Export:** PDF/CSV
* **Database:** SQLite 

---

## 5. Application Pages (JavaFX Screens)

* **CSV Upload Page** – Choose and validate student/course/classroom CSVs.
* **Schedule Page** – Generate weekly exam timetable.
* **Calendar View** – Show Mon–Sun grid with time slots.
* **Warnings Panel** – Display errors/conflicts.
* **Export Page** – Export PDF/CSV.

---