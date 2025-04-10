package pl.project.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.project.json.structures.input.CommandList;

import java.io.File;
import java.io.IOException;

public class JsonReader {

    CommandList commandList;


    // TODO zrobić go uniwersalnym, żeby pozbyć się @LaneManager
    public JsonReader(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        this.commandList = mapper.readValue(new File(filePath), CommandList.class);
    }

    public CommandList read(){
        return commandList;
    }




}
