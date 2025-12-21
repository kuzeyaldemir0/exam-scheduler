package examschd.controller;

import examschd.model.Classroom;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditClassroomController {

    @FXML private TextField nameField;
    @FXML private TextField capacityField;

    private Classroom classroom;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        nameField.setText(classroom.getName());
        capacityField.setText(String.valueOf(classroom.getCapacity()));
    }

    public Classroom getUpdatedClassroom() {
        return classroom;
    }

    @FXML
    private void onSave() {
        try {
            String name = nameField.getText().trim();
            int capacity = Integer.parseInt(capacityField.getText().trim());

            if (name.isEmpty() || capacity <= 0) return;

            classroom.setName(name);
            classroom.setCapacity(capacity);
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        classroom = null;
        close();
    }

    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
