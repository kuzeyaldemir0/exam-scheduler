package examschd.service.readers;

import examschd.model.Classroom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassroomCsvReaderTest {

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("classrooms", ".csv");
    }

    private void writeCsv(String content) throws IOException {
        try (FileWriter fw = new FileWriter(tempFile.toFile(), StandardCharsets.UTF_8)) {
            fw.write(content);
        }
    }

    @Test
    void testRead_ValidClassrooms() throws Exception {
        String csvContent = """
                ALL OF THE CLASSROOMS
                Classroom_01;40
                Classroom_02;50
                Classroom_03;60
                """;
        writeCsv(csvContent);

        List<Classroom> list = ClassroomCsvReader.read(tempFile.toString());

        assertEquals(3, list.size());
        assertEquals("Classroom_01", list.get(0).getName());
        assertEquals(50, list.get(1).getCapacity());
        assertEquals(1, list.get(0).getClassroomId());
    }

    @Test
    void testRead_SkipHeadersAndEmptyLines() throws Exception {
        String csvContent = """
                ALL OF SOMETHING
                                
                Classroom_A;30
                                
                Classroom_B;40
                """;

        writeCsv(csvContent);

        List<Classroom> list = ClassroomCsvReader.read(tempFile.toString());

        assertEquals(2, list.size());
        assertEquals("Classroom_A", list.get(0).getName());
        assertEquals("Classroom_B", list.get(1).getName());
    }

    @Test
    void testRead_HandleDifferentSeparators() throws Exception {
        String csvContent = """
                ClassA  10
                ClassB,20
                ClassC\t30
                ClassD;40
                """;

        writeCsv(csvContent);

        List<Classroom> list = ClassroomCsvReader.read(tempFile.toString());

        assertEquals(4, list.size());
        assertEquals("ClassA", list.get(0).getName());
        assertEquals(10, list.get(0).getCapacity());
    }

    @Test
    void testRead_SkipMalformedLines() throws Exception {
        String csvContent = """
                ValidRoom;40
                WrongLineNoCapacity
                AnotherValidRoom;50
                """;

        writeCsv(csvContent);

        List<Classroom> list = ClassroomCsvReader.read(tempFile.toString());

        assertEquals(2, list.size());
        assertEquals("ValidRoom", list.get(0).getName());
        assertEquals("AnotherValidRoom", list.get(1).getName());
    }

    @Test
    void testRead_SkipInvalidCapacity() throws Exception {
        String csvContent = """
                Room1;30
                Room2;NotNumber
                Room3;50
                """;

        writeCsv(csvContent);

        List<Classroom> list = ClassroomCsvReader.read(tempFile.toString());

        assertEquals(2, list.size());
        assertEquals("Room1", list.get(0).getName());
        assertEquals("Room3", list.get(1).getName());
    }

    @Test
    void testRead_AutoIncrementIds() throws Exception {
        String csvContent = """
                R1;10
                R2;20
                R3;30
                """;

        writeCsv(csvContent);

        List<Classroom> list = ClassroomCsvReader.read(tempFile.toString());

        assertEquals(1, list.get(0).getClassroomId());
        assertEquals(2, list.get(1).getClassroomId());
        assertEquals(3, list.get(2).getClassroomId());
    }

    @Test
    void testRead_SpecialCharacters() throws Exception {
        String csvContent = """
                Room-A;10
                Room_B.2;20
                Room#C3;30
                """;

        writeCsv(csvContent);

        List<Classroom> list = ClassroomCsvReader.read(tempFile.toString());

        assertEquals(3, list.size());
        assertEquals("Room-A", list.get(0).getName());
        assertEquals("Room_B.2", list.get(1).getName());
    }

    @Test
    void testRead_EmptyFile() throws Exception {
        writeCsv("");

        List<Classroom> list = ClassroomCsvReader.read(tempFile.toString());

        assertTrue(list.isEmpty());
    }

}
