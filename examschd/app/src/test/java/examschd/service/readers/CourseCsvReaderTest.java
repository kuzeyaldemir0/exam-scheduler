package examschd.service.readers;

import examschd.model.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseCsvReaderTest {

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("courses", ".csv");
    }

    private void writeCsv(String content) throws IOException {
        try (FileWriter fw = new FileWriter(tempFile.toFile(), StandardCharsets.UTF_8)) {
            fw.write(content);
        }
    }

    @Test
    void testRead_ValidCourses() throws Exception {
        String csv = """
                ALL OF THE COURSES IN THE SYSTEM
                CourseCode_01
                CourseCode_02
                CourseCode_03
                """;

        writeCsv(csv);

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        assertEquals(3, list.size());
        assertEquals("CourseCode_01", list.get(0).getCourseName());
        assertEquals(1, list.get(0).getCourseId());
        assertEquals("CourseCode_03", list.get(2).getCourseName());
        assertEquals(3, list.get(2).getCourseId());
    }

    @Test
    void testRead_IgnoresHeaders_And_EmptyLines() throws Exception {
        String csv = """
                ALL OF THE COURSES
                                
                A
                B
                                
                """;

        writeCsv(csv);

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getCourseName());
        assertEquals("B", list.get(1).getCourseName());
    }

    @Test
    void testRead_OrderPreserved() throws Exception {
        String csv = """
                X1
                X2
                X3
                """;

        writeCsv(csv);

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        assertEquals("X1", list.get(0).getCourseName());
        assertEquals("X2", list.get(1).getCourseName());
        assertEquals("X3", list.get(2).getCourseName());
        assertEquals(1, list.get(0).getCourseId());
        assertEquals(2, list.get(1).getCourseId());
        assertEquals(3, list.get(2).getCourseId());
    }

    @Test
    void testRead_FileNotFound() {
        assertThrows(IOException.class,
                () -> CourseCsvReader.read("non_existent_file.csv"));
    }

    @Test
    void testRead_TrimsWhitespace() throws Exception {
        writeCsv("""
               Course_A    
           Course_B
                Course_C 
                """);

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        assertEquals(3, list.size());
        assertEquals("Course_A", list.get(0).getCourseName());
        assertEquals("Course_B", list.get(1).getCourseName());
        assertEquals("Course_C", list.get(2).getCourseName());
    }

    @Test
    void testRead_EmptyFile() throws Exception {
        writeCsv("");

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        assertTrue(list.isEmpty());
    }

    @Test
    void testRead_OnlyHeader_NoCourses() throws Exception {
        String csv = """
            ALL OF THE COURSES IN THE SYSTEM
            """;
        writeCsv(csv);

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        assertTrue(list.isEmpty());
    }

    @Test
    void testRead_DuplicateCourseNames_BothIncluded() throws Exception {
        String csv = """
            CourseCode_01
            CourseCode_01
            CourseCode_02
            """;
        writeCsv(csv);

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        // Reader duplicate kontrolü yapmıyor, ImportService yapıyor
        assertEquals(3, list.size());
        assertEquals("CourseCode_01", list.get(0).getCourseName());
        assertEquals("CourseCode_01", list.get(1).getCourseName());
        assertEquals(1, list.get(0).getCourseId());
        assertEquals(2, list.get(1).getCourseId());
    }

    @Test
    void testRead_SpecialCharacters() throws Exception {
        String csv = """
            Course-A
            Course_B.2
            Course#C3
            Course@123
            """;
        writeCsv(csv);

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        assertEquals(4, list.size());
        assertEquals("Course-A", list.get(0).getCourseName());
        assertEquals("Course_B.2", list.get(1).getCourseName());
        assertEquals("Course#C3", list.get(2).getCourseName());
    }

    @Test
    void testRead_NumericCourseName() throws Exception {
        String csv = """
            101
            202
            303
            """;
        writeCsv(csv);

        List<Course> list = CourseCsvReader.read(tempFile.toString());

        assertEquals(3, list.size());
        assertEquals("101", list.get(0).getCourseName());
        assertEquals("202", list.get(1).getCourseName());
    }
}
