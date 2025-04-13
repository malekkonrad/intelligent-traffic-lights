package pl.project.traffic.vehicles;

import lombok.Getter;
import lombok.Setter;
import pl.project.direction.Direction;

import java.util.Map;

@Getter

public class Vehicle {

    String id;

    @Setter
    int delay;

    @Setter
    Direction startRoad;

    @Setter
    Direction endRoad;

    @Getter
    @Setter
    private boolean isBlocked = false;

    Map<String, Direction> converter = Map.of(
            "north", Direction.NORTH,
            "south", Direction.SOUTH,
            "west", Direction.WEST,
            "east", Direction.EAST
    );

    public Vehicle(String id, String startRoad, String endRoad) {
        this.id = id;
        this.delay = 0;
        this.startRoad = converter.get(startRoad);
        this.endRoad = converter.get(endRoad);
    }


    public void incrementDelay() {
        if (delay > 5){
            delay *= 2;
        }
        else{
            delay++;
        }
    }


}
