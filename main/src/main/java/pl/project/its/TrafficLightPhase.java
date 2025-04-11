package pl.project.its;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.project.json.structures.Direction;
import pl.project.its.directions.DirectionPair;

import java.util.Set;

@Getter
@AllArgsConstructor
public class TrafficLightPhase {

    private final Set<DirectionPair> allowedMovements;
    private final Set<Direction> pedestrainCrossings;

    public boolean allows(Direction start, Direction end) {
        return allowedMovements.contains(new DirectionPair(start, end));
    }
}
