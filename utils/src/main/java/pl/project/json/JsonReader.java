package pl.project.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.project.json.structures.input.Command;
import pl.project.json.structures.input.CommandList;

import java.io.File;
import java.io.IOException;

public class JsonReader {

    public JsonReader(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Wczytanie JSON-a z pliku
        CommandList commandList = mapper.readValue(new File(filePath), CommandList.class);

        // Przejście po komendach
        for (Command cmd : commandList.getCommands()) {
            System.out.println("Type: " + cmd.getType());
            if ("addVehicle".equals(cmd.getType())) {
                System.out.println("  ID: " + cmd.getVehicleId());
                System.out.println("  Start: " + cmd.getStartRoad());
                System.out.println("  End: " + cmd.getEndRoad());
            }
        }

    }




}
