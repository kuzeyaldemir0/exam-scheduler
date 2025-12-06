package examschd.service.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.io.IOException;

import examschd.model.Enrollment;

public class EnrollmentCsvReader implements CsvReader<Enrollment> {

    @Override
    public List<Enrollment> read(String filePath) throws Exception {
        List<Enrollment> enrollments = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            int counter=1;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("ALL OF")) continue;

                // CourseCode_01 ['Std_ID_x', ...]
                String[] parts = line.split("\\s+", 2);

                String courseName = parts[0].trim();
                String listPart = parts[1].trim();

                // Remove brackets [ ]
                listPart = listPart.replace("[", "")
                                   .replace("]", "")
                                   .replace("'", "");

                // Split by comma
                List<String> studentNumbers = new ArrayList<>();
                for (String s : listPart.split(",")) {
                    studentNumbers.add(s.trim());
                }

                enrollments.add(new Enrollment(counter, studentNumbers, courseName));
                counter++;
            }
        }
        return enrollments;
    }
}
