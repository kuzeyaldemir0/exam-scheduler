package examschd.service.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import examschd.model.Enrollment;

public class EnrollmentCsvReader {

    public static List<Enrollment> read(String filePath) throws IOException {

        List<Enrollment> enrollments = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String courseLine;
            int counter = 1;

            while ((courseLine = br.readLine()) != null) {

                courseLine = courseLine.trim();

                if (courseLine.isEmpty()) continue;

                if (!courseLine.startsWith("CourseCode_"))
                    continue;

                String courseName = courseLine;

                // İkinci satır = öğrenci listesi
                String listLine = br.readLine();
                if (listLine == null) break;

                listLine = listLine.trim();

                // Köşeli parantezleri temizle
                listLine = listLine.replace("[", "")
                                   .replace("]", "")
                                   .replace("'", "");

                List<String> studentIds = new ArrayList<>();

                for (String s : listLine.split(",")) {
                    String cleaned = s.trim();
                    if (!cleaned.isEmpty())
                        studentIds.add(cleaned);
                }

                enrollments.add(new Enrollment(counter, studentIds, courseName));
                counter++;
            }
        }

        return enrollments;
    }
}
