package examschd.service.readers;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvValidationException;
import examschd.model.Classroom;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClassroomCsvReader {

    public static List<Classroom> read(String filePath) throws IOException {

        List<Classroom> classrooms = new ArrayList<>();
        int classroomIdCounter = 1;

        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(filePath, StandardCharsets.UTF_8)
        ).withCSVParser(
                new CSVParserBuilder()
                        .withSeparator('\t')
                        .build()
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

                // parse classroom name and capacity
                String[] parts = fullLine.split("[\\s,;]+");

                if (parts.length < 2) continue;

                String name = parts[0].trim();
                int capacity;

                try {
                    capacity = Integer.parseInt(parts[1].trim());
                    if (capacity <= 0) continue;
                } catch (NumberFormatException e) {
                    continue;
                }

                classrooms.add(
                        new Classroom(classroomIdCounter++, name, capacity)
                );
            }

        } catch (CsvValidationException e) {
            throw new RuntimeException("Invalid CSV format in classrooms file", e);
        }

        return classrooms;
    }
}
