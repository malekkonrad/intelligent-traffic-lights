package pl.project.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import pl.project.json.structures.output.StepStatusList;

import java.io.File;
import java.io.IOException;


public class JsonWriter {

    public static void writeToFile(String filepath, StepStatusList stepStatusList) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filepath), stepStatusList);
    }
}
