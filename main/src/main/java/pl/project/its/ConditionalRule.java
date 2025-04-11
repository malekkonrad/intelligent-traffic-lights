package pl.project.its;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.project.its.directions.DirectionPair;
import pl.project.json.structures.Direction;
import pl.project.its.pedestrian.Pedestrian;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ConditionalRule {

    private final DirectionPair directionPair;

    public boolean isAllowed(TrafficState trafficState, TrafficLightPhase currentPhase, Map<Direction, Queue<Pedestrian>> pedestriansPerDirection) {

        // sprawdzić czy jakieś auta jadą w kierunku docelowym
        Set<DirectionPair> activeMovements =  currentPhase.getAllowedMovements();

        for (DirectionPair directionPair_: activeMovements){
            if (directionPair_.getEndRoad().equals(directionPair.getEndRoad()) && trafficState.getVehicleCount(directionPair_) > 0){
                return false;
            }
        }

        // sprawdzić czy przejście dla pieszych SOUTH jest nieaktywne w tej fazie lub nikt przez nie nie przechodzi tak samo z EAST przejściem
        Set<Direction> crossings = currentPhase.getPedestrianCrossings();
        if (crossings.contains(directionPair.getStartRoad()) && !pedestriansPerDirection.get(directionPair.getStartRoad()).isEmpty()) {
            return false;
        }

        if (crossings.contains(directionPair.getEndRoad()) && !pedestriansPerDirection.get(directionPair.getEndRoad()).isEmpty()) {
            return false;
        }

        // wtedy może przejechać
        return true;
    }

}
