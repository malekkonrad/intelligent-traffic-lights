package pl.project.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import pl.project.json.structures.output.StepStatusList;

import java.io.File;
import java.io.IOException;


public class JsonWriter {

    String filepath;

    public JsonWriter(String filepath) {
        this.filepath = filepath;
    }

    public void writeToFile(StepStatusList stepStatusList) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filepath), stepStatusList);
    }
}
