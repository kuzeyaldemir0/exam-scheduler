package examschd.service.readers;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import examschd.model.Course;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CourseCsvReader {

    public static List<Course> read(String filePath) throws IOException {

        List<Course> courses = new ArrayList<>();
        int courseIdCounter = 1;

        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(filePath, StandardCharsets.UTF_8)
        ).build()) {

            String[] line;

            while ((line = reader.readNext()) != null) {

                // combine all cells in the line
                StringBuilder sb = new StringBuilder();
                for (String cell : line) {
                    sb.append(cell).append(" ");
                }

                String fullLine = sb.toString().trim();

                // empty or irrelevant lines
                if (fullLine.isEmpty()) continue;
                if (fullLine.toUpperCase().contains("ALL OF")) continue;

                // course name
                String courseName = fullLine;

                courses.add(
                        new Course(courseIdCounter++, courseName)
                );
            }

        } catch (CsvValidationException e) {
            throw new RuntimeException("Invalid CSV format in courses file", e);
        }

        return courses;
    }
}
