package pl.project.its;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.project.its.directions.Direction;

import java.util.Map;

@Getter

public class Vehicle {

    String id;

    @Setter
    int delay = 0;

    @Setter
    Direction startRoad;

    @Setter
    Direction endRoad;

    Map<String, Direction> converter = Map.of(
            "north", Direction.NORTH,
            "south", Direction.SOUTH,
            "west", Direction.WEST,
            "east", Direction.EAST
    );

    public Vehicle(String id, int delay, String startRoad, String endRoad) {
        this.id = id;
        this.delay = delay;
        this.startRoad = converter.get(startRoad);
        this.endRoad = converter.get(endRoad);
    }


    public void incrementDelay() {
        delay++;
    }


}
