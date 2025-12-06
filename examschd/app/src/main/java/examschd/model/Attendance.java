package examschd.model;

public class Attendance {
    private int attendanceId;
    private boolean present;

    public Attendance(int attendanceId, boolean present) {
        this.attendanceId = attendanceId;
        this.present = present;
    }

    public int getAttendanceId() { 
        return attendanceId; 
    }

    public boolean isPresent() { 
        return present; 
    }

    public void setPresent(boolean present) { 
        this.present = present; 
    }
}