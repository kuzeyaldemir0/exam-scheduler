public class Classroom {
    private int classroomId;
    private String code;
    private int capacity;

    public Classroom(int classroomId, String code, int capacity) {
        this.classroomId = classroomId;
        this.code = code;
        this.capacity = capacity;
    }

    public int getClassroomId() { return classroomId; }
    public String getCode() { return code; }
    public int getCapacity() { return capacity; }
}