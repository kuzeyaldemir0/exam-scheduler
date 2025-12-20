package examschd.service.readers;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import examschd.model.Student;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StudentCsvReader  {

    public static List<Student> read(String filePath) throws IOException {
        List<Student> students = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(filePath, StandardCharsets.UTF_8)
        ).build()) {

            String[] line;
            int counter = 1;

            while ((line = reader.readNext()) != null) {

                if (line.length == 0) continue;

                String value = line[0].trim();

                // empty or irrelevant lines
                if (value.isEmpty() || value.toUpperCase().startsWith("ALL OF")) continue;

                students.add(new Student(counter++, value));
            }

        } catch (CsvValidationException e) {
            throw new RuntimeException(
                    "Invalid CSV format in students file", e
            );
        }

        return students;
    }
}
