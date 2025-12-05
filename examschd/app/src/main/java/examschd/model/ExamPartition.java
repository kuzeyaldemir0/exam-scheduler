import java.util.ArrayList;
import java.util.List;

public class ExamPartition
{
    private int partitionId;
    private int capacityAssigned;

    // Relationship: Classroom "hosts" Partition
    private Classroom classroom;

    // Relationship: Composition (Diamond) - Partition contains Assignments
    private List<StudentAssignment> studentAssignments;

    public ExamPartition(int partitionId, int capacityAssigned, Classroom classroom)
    {
        this.partitionId = partitionId;
        this.capacityAssigned = capacityAssigned;
        this.classroom = classroom;
        this.studentAssignments = new ArrayList<>();
    }

    public void addAssignment(StudentAssignment assignment) {
        this.studentAssignments.add(assignment);
    }

    public int getPartitionId() { return partitionId; }
    public int getCapacityAssigned() { return capacityAssigned; }
    public Classroom getClassroom() { return classroom; }
    public List<StudentAssignment> getStudentAssignments() { return studentAssignments; }
}