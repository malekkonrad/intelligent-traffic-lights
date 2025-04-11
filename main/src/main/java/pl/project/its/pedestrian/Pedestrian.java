package pl.project.its.pedestrian;

import lombok.Getter;
import lombok.Setter;
import pl.project.json.structures.Direction;


@Getter
public class Pedestrian {
    private final String id;
    private final Direction crossingDirection;
    @Setter
    int delay;


    public Pedestrian(String id, String crossingDirection) {
        this.id = id;
        this.crossingDirection = Direction.valueOf(crossingDirection.toUpperCase());
        this.delay = 0;
    }

    public void incrementDelay() {
        this.delay++;
    }
}
