package examschd.service.readers;

import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import examschd.model.Enrollment;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentCsvReader {

    public static List<Enrollment> read(String filePath) throws IOException {

        List<Enrollment> enrollments = new ArrayList<>();
        int counter = 1;

        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(filePath, StandardCharsets.UTF_8)
        ).withCSVParser(
                new CSVParserBuilder()
                        .withSeparator(',') // CSV default
                        .build()
        ).build()) {

            String[] line;

            while ((line = reader.readNext()) != null) {

                // Empty line
                if (line.length == 0 || line[0].trim().isEmpty())
                    continue;

                // Course line
                if (!line[0].startsWith("CourseCode_"))
                    continue;

                String courseName = line[0].trim();
                //System.out.println("\n📘 Course: " + courseName);

                // STUDENT LIST LINE
                String[] listLine = reader.readNext();
                if (listLine == null) break;

                StringBuilder sb = new StringBuilder();
                for (String cell : listLine) {
                    sb.append(cell).append(" ");
                }

                String listStr = sb.toString();
                //System.out.println("Raw list: " + listStr);

                //CLEANING
                listStr = listStr
                        .replaceAll("[\\[\\]]", "")   // [ ]
                        .replaceAll("[‘’']", "")      // all single quotes
                        .replaceAll(";", " ");        // ; -> space

                List<String> studentIds = new ArrayList<>();

                //SPLIT EVERYTHING
                for (String s : listStr.split("[,;|\\t ]+")) {
                    s = s.trim();
                    if (s.matches("Std_ID_\\d+")) {
                        studentIds.add(s);
                    }
                }

                // DEBUG 
                /*System.out.println("Parsed students: " + studentIds.size());
                System.out.println("Stored students:");
                for (String id : studentIds) {
                    System.out.println("   - " + id);
                }*/

                enrollments.add(
                        new Enrollment(counter++, studentIds, courseName)
                );
            }

        } catch (CsvValidationException e) {
            throw new RuntimeException(
                    "Invalid CSV format in enrollments file", e
            );
        }

       // System.out.println("\nTotal enrollments read: " + enrollments.size());
        return enrollments;
    }
}
