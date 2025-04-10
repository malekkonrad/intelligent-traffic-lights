package pl.project.json;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pl.project.json.structures.output.StepStatusList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonWriterTest {

    private static final String TEST_FILE_PATH = "src/test/resources/test_output.json";

    @AfterEach
    void cleanUp() {
        // Usuwanie pliku testowego po każdym teście
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testWriteToFile_createsJsonFileWithCorrectContent() throws IOException {
        // given
        StepStatusList stepStatusList = new StepStatusList();
        stepStatusList.addStep(List.of("vehicle1", "vehicle2"));
        stepStatusList.addStep(List.of("vehicle3"));

        // when
        JsonWriter.writeToFile(TEST_FILE_PATH, stepStatusList);

        // then
        File file = new File(TEST_FILE_PATH);
        assertTrue(file.exists(), "Plik JSON nie został utworzony");

        String content = Files.readString(Paths.get(TEST_FILE_PATH));
        assertTrue(content.contains("\"leftVehicles\" : [ \"vehicle1\", \"vehicle2\" ]"));
        assertTrue(content.contains("\"leftVehicles\" : [ \"vehicle3\" ]"));
    }

    @Test
    void testWriteToFile_invalidPath_throwsIOException() {
        // given
        String invalidPath = "/invalid_path/test_output.json";
        StepStatusList stepStatusList = new StepStatusList();
        stepStatusList.addStep(List.of("vehicle1"));

        // when & then
        assertThrows(IOException.class, () -> JsonWriter.writeToFile(invalidPath, stepStatusList));
    }

    @Test
    void testWriteToFile_emptyStepStatusList_createsEmptyJsonFile() throws IOException {
        // given
        StepStatusList stepStatusList = new StepStatusList();

        // when
        JsonWriter.writeToFile(TEST_FILE_PATH, stepStatusList);

        // then
        File file = new File(TEST_FILE_PATH);
        assertTrue(file.exists(), "Plik JSON nie został utworzony");

        String content = Files.readString(Paths.get(TEST_FILE_PATH));
        assertTrue(content.contains("\"stepStatuses\" : [ ]"), "Plik JSON nie zawiera pustej listy kroków");
    }
}