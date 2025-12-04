public class StudentAssignment {
    private int assignmentId;
    private int seatNumber;

    // Relationship: Student "is assigned"
    private Student student;

    // Relationship: Assignment belongs to a Partition
    private ExamPartition partition;

    // Relationship: Assignment has Attendance (0..1)
    private Attendance attendance;

    public StudentAssignment(int assignmentId, int seatNumber, Student student, ExamPartition partition) {
        this.assignmentId = assignmentId;
        this.seatNumber = seatNumber;
        this.student = student;
        this.partition = partition;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public Attendance getAttendance() { return attendance; }
    public int getAssignmentId() { return assignmentId; }
    public int getSeatNumber() { return seatNumber; }
    public Student getStudent() { return student; }
}