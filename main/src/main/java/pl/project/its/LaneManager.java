package pl.project.its;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import pl.project.its.directions.Direction;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public class LaneManager {

    private final Map<Direction, List<Lane>> lanesPerDirection = new EnumMap<>(Direction.class);

    public void loadFromJson(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(filePath));

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
    }


}
