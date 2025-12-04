package examschd.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class SchedulingController {

    @FXML
    private TableView<?> scheduleTable;

    @FXML
    public void initialize() {
        System.out.println("Scheduling ekrani acildi");
    }
}
