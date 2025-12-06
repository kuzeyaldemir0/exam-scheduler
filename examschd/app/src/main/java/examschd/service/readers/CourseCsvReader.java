package examschd.service.readers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


import examschd.model.Course;

public class CourseCsvReader implements CsvReader<Course> {

    @Override
    public List<Course> read(String filePath) throws IOException {
        List<Course> courses = new ArrayList<>();
        int courseIdCounter = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                courses.add(new Course(courseIdCounter++, line));
            }
        }

        return courses;
    }
}
