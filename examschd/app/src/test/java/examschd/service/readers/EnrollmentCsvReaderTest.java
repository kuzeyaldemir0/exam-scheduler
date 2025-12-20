package examschd.service.readers;

import examschd.model.Enrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentCsvReaderTest {

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("enrollments", ".csv");
    }

    private void writeCsv(String content) throws IOException {
        try (FileWriter fw = new FileWriter(tempFile.toFile(), StandardCharsets.UTF_8)) {
            fw.write(content);
        }
    }

    @Test
    void testRead_SingleCourseEnrollment() throws Exception {
        String csv = """
                CourseCode_01
                ['Std_ID_001', 'Std_ID_002', 'Std_ID_003']
                """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        Enrollment e = list.get(0);

        assertEquals("CourseCode_01", e.getCourseName());
        assertEquals(1, e.getEnrollmentId());

        assertEquals(3, e.getStudentNumbers().size());
        assertTrue(e.getStudentNumbers().contains("Std_ID_001"));
    }

    @Test
    void testRead_MultipleCourses() throws Exception {
        String csv = """
                CourseCode_01
                ['Std_ID_010', 'Std_ID_020']
                
                CourseCode_02
                ['Std_ID_030', 'Std_ID_040', 'Std_ID_050']
                """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(2, list.size());
        assertEquals(2, list.get(0).getStudentNumbers().size());
        assertEquals(3, list.get(1).getStudentNumbers().size());
    }

    @Test
    void testRead_IgnoresInvalidStudentTokens() throws Exception {
        String csv = """
                CourseCode_01
                ['Std_ID_001', 'INVALID', '123', 'Std_ID_002']
                """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        List<String> students = list.get(0).getStudentNumbers();

        assertEquals(2, students.size());
        assertTrue(students.contains("Std_ID_001"));
        assertTrue(students.contains("Std_ID_002"));
    }


    @Test
    void testRead_SkipsNonCourseLines() throws Exception {
        String csv = """
                XXXXX
                ['Std_ID_001']
                
                CourseCode_01
                ['Std_ID_003']
                """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        assertEquals("CourseCode_01", list.get(0).getCourseName());
    }

    @Test
    void testRead_AutoIncrementIds() throws Exception {
        String csv = """
                CourseCode_01
                ['Std_ID_001']
                
                CourseCode_02
                ['Std_ID_002']
                """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getEnrollmentId());
        assertEquals(2, list.get(1).getEnrollmentId());
    }

    @Test
    void testRead_EmptyFile() throws Exception {
        writeCsv("");

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testRead_EmptyStudentList() throws Exception {
        String csv = """
                CourseCode_01
                []
                """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        assertTrue(list.get(0).getStudentNumbers().isEmpty());
    }

    @Test
    void testRead_ExtraWhitespaceAroundStudentIds() throws Exception {
        String csv = """
            CourseCode_01
            ['  Std_ID_001  ', '   Std_ID_002   ']
            """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        List<String> students = list.get(0).getStudentNumbers();
        assertEquals(2, students.size());
        assertEquals("Std_ID_001", students.get(0));
        assertEquals("Std_ID_002", students.get(1));
    }

    @Test
    void testRead_MixedValidAndInvalid() throws Exception {
        String csv = """
            CourseCode_01
            ['Std_ID_001', 'Student_123', 'Std_ID_002', 'STD_ID_003', 'Std_ID_', 'Std_ID_004']
            """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        List<String> students = list.get(0).getStudentNumbers();

        assertEquals(3, students.size());
        assertTrue(students.contains("Std_ID_001"));
        assertTrue(students.contains("Std_ID_002"));
        assertTrue(students.contains("Std_ID_004"));
    }

    @Test
    void testRead_CourseWithoutStudentList_ShouldNotCrash() throws Exception {
        String csv = """
            CourseCode_01
            ['Std_ID_001']
            
            CourseCode_02
            """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        assertEquals("CourseCode_01", list.get(0).getCourseName());
    }

    @Test
    void testRead_PipeSeparator() throws Exception {
        String csv = """
            CourseCode_01
            Std_ID_001 | Std_ID_002 | Std_ID_003
            """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        assertEquals(3, list.get(0).getStudentNumbers().size());
    }

    @Test
    void testRead_NoBrackets_JustCommas() throws Exception {
        String csv = """
            CourseCode_01
            Std_ID_001, Std_ID_002, Std_ID_003
            """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        assertEquals(3, list.get(0).getStudentNumbers().size());
    }

    @Test
    void testRead_SemicolonInStudentList_ShouldRemove() throws Exception {
        String csv = """
            CourseCode_01
            ['Std_ID_001'; 'Std_ID_002'; 'Std_ID_003']
            """;

        writeCsv(csv);

        List<Enrollment> list = EnrollmentCsvReader.read(tempFile.toString());

        assertEquals(1, list.size());
        assertEquals(3, list.get(0).getStudentNumbers().size());
    }

    @Test
    void testRead_FileNotFound() {
        assertThrows(IOException.class,
                () -> EnrollmentCsvReader.read("non_existent_file.csv"));
    }
}
