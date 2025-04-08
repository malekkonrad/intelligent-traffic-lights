package pl.project.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.project.json.structures.output.StepStatusList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JsonWriterTest {

//    @Test
//    public void testWriteToFile_createsJsonFileWithCorrectContent() throws IOException {
//        // given
//        StepStatusList list = new StepStatusList();
//
//        list.addStep(Arrays.asList("aaa", "bbb", "ccc"));
//        list.addStep(Arrays.asList("ada", "acca", "seww"));
//
//
//        File tempFile = Files.createTempFile("step_status", ".json").toFile();
//        JsonWriter writer = new JsonWriter(tempFile.getAbsolutePath());
//
//        // when
//        writer.writeToFile(list);
//
//        // then
//        ObjectMapper mapper = new ObjectMapper();
//        StepStatusList result = mapper.readValue(tempFile, StepStatusList.class);
//
//        assertTrue(list.equals(result));
//        assertTrue(tempFile.exists());
//    }

    @Test
    public void testWriteToFile_invalidPath_throwsIOException() {
        String invalidPath = "/invalid_path/step.json";
        JsonWriter writer = new JsonWriter(invalidPath);
        StepStatusList list = new StepStatusList();
        assertThrows(IOException.class, () -> writer.writeToFile(list));
    }

}