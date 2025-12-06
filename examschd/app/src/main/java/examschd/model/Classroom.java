package examschd.model;

public class Classroom {
    private int classroomId;
    private String name;
    private int capacity;

    // Constructor
    public Classroom(int classroomId, String name, int capacity) {
        this.classroomId = classroomId;
        this.name = name;
        this.capacity = capacity;
    }

    // Getter & Setter
    public int getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}