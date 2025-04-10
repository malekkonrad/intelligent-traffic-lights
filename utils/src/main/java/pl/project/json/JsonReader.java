package pl.project.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.project.json.structures.Direction;
import pl.project.json.structures.Lane;
import pl.project.json.structures.input.CommandList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class JsonReader {

    public static CommandList loadCommandList(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), CommandList.class);
    }


    public static Map<Direction, List<Lane>> loadLanesFromJson(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(filePath));
        Map<Direction, List<Lane>> lanesPerDirection = new EnumMap<>(Direction.class);

        for (Direction dir : Direction.values()) {
            JsonNode laneArray = root.get(dir.name().toLowerCase());
            if (laneArray == null || !laneArray.isArray()) continue;

            List<Lane> laneList = new ArrayList<>();
            for (JsonNode laneNode : laneArray) {
                List<Direction> allowedDestinations = new ArrayList<>();
                for (JsonNode exit : laneNode.get("allowedDestinations")) {
                    allowedDestinations.add(Direction.valueOf(exit.asText().toUpperCase()));
                }
                laneList.add(new Lane(allowedDestinations));
            }
            lanesPerDirection.put(dir, laneList);
        }
        return lanesPerDirection;
    }


}
