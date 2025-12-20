package examschd.service.readers;

import examschd.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentCsvReaderTest {

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("students", ".csv");
    }

    private void writeCsv(String content) throws IOException {
        try (FileWriter fw = new FileWriter(tempFile.toFile(), StandardCharsets.UTF_8)) {
            fw.write(content);
        }
    }

    @Test
    void testRead_ValidCsvStudents() throws Exception {
        String csvContent = """
                ALL OF THE STUDENTS
                Std_ID_001
                Std_ID_002
                Std_ID_003
                """;

        writeCsv(csvContent);

        List<Student> students = StudentCsvReader.read(tempFile.toString());

        assertEquals(3, students.size());
        assertEquals("Std_ID_001", students.get(0).getStudentName());
        assertEquals("Std_ID_003", students.get(2).getStudentName());
    }

    @Test
    void testRead_ShouldSkipHeader() throws Exception {
        String csvContent = """
                ALL OF THE STUDENTS IN THE SYSTEM
                Std_ID_100
                """;

        writeCsv(csvContent);

        List<Student> students = StudentCsvReader.read(tempFile.toString());

        assertEquals(1, students.size());
        assertEquals("Std_ID_100", students.get(0).getStudentName());
    }

    @Test
    void testRead_ShouldSkipEmptyLines() throws Exception {
        String csvContent = """
                ALL OF THE STUDENTS

                Std_ID_010

                Std_ID_011
                """;

        writeCsv(csvContent);

        List<Student> students = StudentCsvReader.read(tempFile.toString());

        assertEquals(2, students.size());
    }

    @Test
    void testRead_OrderShouldBePreserved() throws Exception {
        String csvContent = """
                A
                B
                C
                """;

        writeCsv(csvContent);

        List<Student> students = StudentCsvReader.read(tempFile.toString());

        assertEquals("A", students.get(0).getStudentName());
        assertEquals("B", students.get(1).getStudentName());
        assertEquals("C", students.get(2).getStudentName());
    }

    @Test
    void testRead_AutoIncrementIds() throws Exception {
        String csvContent = """
                Klaus
                Mikael
                Elijah
                """;

        writeCsv(csvContent);

        List<Student> students = StudentCsvReader.read(tempFile.toString());

        assertEquals(1, students.get(0).getStudentId());
        assertEquals(2, students.get(1).getStudentId());
        assertEquals(3, students.get(2).getStudentId());
    }

    @Test
    void testRead_TrimWhitespace() throws Exception {
        String csvContent = """
                  Klaus 
                Mikael
                   Elijah  
                """;

        writeCsv(csvContent);

        List<Student> students = StudentCsvReader.read(tempFile.toString());

        assertEquals("Klaus", students.get(0).getStudentName());
        assertEquals("Mikael", students.get(1).getStudentName());
        assertEquals("Elijah", students.get(2).getStudentName());
    }

    @Test
    void testRead_WhitespaceOnlyLine_ShouldSkip() throws Exception {
        String csv = """
                Std_ID_001
                   
                Std_ID_002
                """;

        writeCsv(csv);

        List<Student> list = StudentCsvReader.read(tempFile.toString());

        assertEquals(2, list.size());
        assertEquals("Std_ID_001", list.get(0).getStudentName());
        assertEquals("Std_ID_002", list.get(1).getStudentName());
    }

    @Test
    void testRead_FileNotFound() {
        assertThrows(IOException.class,
                () -> StudentCsvReader.read("non_existent_file.csv"));
    }

    @Test
    void testRead_SpecialCharacters() throws Exception {
        String csv = """
                Student-A
                Student_B.2
                Student#C3
                Student@123
                """;

        writeCsv(csv);

        List<Student> list = StudentCsvReader.read(tempFile.toString());

        assertEquals(4, list.size());
        assertEquals("Student-A", list.get(0).getStudentName());
        assertEquals("Student_B.2", list.get(1).getStudentName());
        assertEquals("Student#C3", list.get(2).getStudentName());
    }
}
