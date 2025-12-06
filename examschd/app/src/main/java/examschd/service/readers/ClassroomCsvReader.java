package examschd.service.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


import examschd.model.Classroom;

public class ClassroomCsvReader implements CsvReader<Classroom> {

    @Override
    public List<Classroom> read(String filePath) throws IOException {
        List<Classroom> classrooms = new ArrayList<>();
        int classroomIdCounter = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Örnek CSV: "A101,30"
                String[] parts = line.split(",");
                String name = parts[0].trim();
                int capacity = Integer.parseInt(parts[1].trim());

                classrooms.add(new Classroom(classroomIdCounter++, name, capacity));
            }
        }

        return classrooms;
    }
}
