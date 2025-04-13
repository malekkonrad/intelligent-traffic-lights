package pl.project.service.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.project.direction.DirectionPair;
import pl.project.direction.Direction;
import pl.project.service.states.TrafficLightPhase;
import pl.project.service.states.TrafficState;
import pl.project.traffic.pedestrians.Pedestrian;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ConditionalRule {

    private final DirectionPair directionPair;

    public boolean isAllowed(TrafficState trafficState, TrafficLightPhase currentPhase, Map<Direction, Queue<Pedestrian>> pedestriansPerDirection) {

        // check if any cars are going to the destination
        Set<DirectionPair> activeMovements =  currentPhase.getAllowedMovements();

        for (DirectionPair directionPair_: activeMovements){
            if (directionPair_.getEndRoad().equals(directionPair.getEndRoad()) && trafficState.getVehicleCount(directionPair_) > 0){
                return false;
            }
        }

        // crossings
        Set<Direction> crossings = currentPhase.getPedestrianCrossings();
        if (crossings.contains(directionPair.getStartRoad()) && !pedestriansPerDirection.get(directionPair.getStartRoad()).isEmpty()) {
            return false;
        }

        return !crossings.contains(directionPair.getEndRoad()) || pedestriansPerDirection.get(directionPair.getEndRoad()).isEmpty();
    }

}
