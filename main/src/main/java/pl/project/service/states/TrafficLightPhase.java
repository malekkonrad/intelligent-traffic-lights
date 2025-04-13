package pl.project.service.states;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.project.direction.Direction;
import pl.project.direction.DirectionPair;

import java.util.Set;

@Getter
@AllArgsConstructor
public class TrafficLightPhase {

    private final Set<DirectionPair> allowedMovements;
    private final Set<Direction> pedestrianCrossings;

}
