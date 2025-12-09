package examschd.service.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import examschd.model.Student;
import java.util.ArrayList;
import java.util.List;

public class StudentCsvReader  {

    public static List<Student> read(String filePath) throws IOException {
        List<Student> students = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            int counter=1;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("ALL OF")) continue;

                students.add(new Student(counter, line));
                counter++;
            }
        }
        return students;
    }
}
