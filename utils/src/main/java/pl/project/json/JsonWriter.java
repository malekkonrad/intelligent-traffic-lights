package pl.project.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import pl.project.json.structures.output.StepStatus;
import pl.project.json.structures.output.StepStatusList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JsonWriter {

    String filepath;
    StepStatusList stepStatusList = new StepStatusList();

    public JsonWriter(String filepath) {
        this.filepath = filepath;
    }

    public void writeToFile() throws IOException {

        StepStatus s1 = new StepStatus();
        s1.setLeftVehicles(Arrays.asList("vehicle1", "vehicle2"));

        StepStatus s2 = new StepStatus();
        s2.setLeftVehicles(List.of());

        StepStatus s3 = new StepStatus();
        s3.setLeftVehicles(List.of("vehicle3"));

        StepStatus s4 = new StepStatus();
        s4.setLeftVehicles(List.of("vehicle4"));


        stepStatusList.setStepStatuses(Arrays.asList(s1, s2, s3, s4));

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(new File(filepath), stepStatusList);

        System.out.println("Zapisano do pliku stepStatuses.json");
    }



}
