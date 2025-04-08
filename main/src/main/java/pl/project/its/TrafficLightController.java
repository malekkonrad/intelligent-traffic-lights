package pl.project.its;

import pl.project.its.directions.Direction;
import pl.project.its.directions.DirectionPair;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TrafficLightController {

    private final List<TrafficLightPhase> phases;
    private int currentPhaseIndex = 0;

    private int phaseStepCounter = 0;


    // myśle że w przyszłości zamienić Direction na DirectionPair
    private final Map<Direction, List<Lane>>  queues;

    public TrafficLightController(Map<Direction, List<Lane>>  queues) {
        this.queues = queues;
        phases = List.of(
                // phase 1:
                new TrafficLightPhase(Set.of(
                        new DirectionPair(Direction.NORTH, Direction.SOUTH),
                        new DirectionPair(Direction.SOUTH, Direction.NORTH),

                        // ability to turn right from north/south
                        new DirectionPair(Direction.SOUTH, Direction.EAST),
                        new DirectionPair(Direction.NORTH, Direction.WEST)
                )),

                // phase 2:
                new TrafficLightPhase(Set.of(
                        new DirectionPair(Direction.EAST, Direction.WEST),
                        new DirectionPair(Direction.WEST, Direction.EAST),

                        // ability to turn right from east/west
                        new DirectionPair(Direction.EAST, Direction.NORTH),
                        new DirectionPair(Direction.WEST, Direction.SOUTH)
                )),

                // phase 3:
                new TrafficLightPhase(Set.of(
                        new DirectionPair(Direction.SOUTH, Direction.WEST),
                        new DirectionPair(Direction.NORTH, Direction.EAST),

                        new DirectionPair(Direction.EAST, Direction.NORTH),
                        new DirectionPair(Direction.WEST, Direction.SOUTH)
                )),

                // phase 4:
                new TrafficLightPhase(Set.of(
                        new DirectionPair(Direction.EAST, Direction.SOUTH),
                        new DirectionPair(Direction.WEST, Direction.NORTH),
                        new DirectionPair(Direction.SOUTH, Direction.EAST),
                        new DirectionPair(Direction.NORTH, Direction.WEST)
                ))

        );
    }


    public void nextStep() {
        phaseStepCounter++;

        // dopasowanie długości fazy do natężenia aut na danym kierunku
        int currentPhaseDuration = estimatePhaseDuration(phases.get(currentPhaseIndex));

        currentPhaseIndex = (currentPhaseIndex + 1) % phases.size();

        // przejście do następnej fazy i wyzerowanie licznika długości obecnej
        if (phaseStepCounter >= currentPhaseDuration) {
            currentPhaseIndex = (currentPhaseIndex + 1) % phases.size();
            phaseStepCounter = 0;
        }

    }


    // adaptacyjne obliczenie długości fazy (im więcej aut, tym dłużej)
    private int estimatePhaseDuration(TrafficLightPhase phase) {
//        int totalVehicles = 0;
//        for (Direction dir : Direction.values()) {
//            Queue<Vehicle> q = queues.get(dir);
//            if (q == null || q.isEmpty()) continue;
//
//            Vehicle peek = q.peek();
//            if (peek != null && phase.allows(peek.getStartRoad(), peek.getEndRoad())) {
//                totalVehicles += q.size();
//            }
//        }

//        return Math.max(1, Math.min(1, totalVehicles)); // min. 1 krok, max. 5
        return 1;
    }



    public boolean canPass(Vehicle vehicle) {
        // pobieram aktualną faze - phase i sprawdzam czy dozwolony jest ruch
        return phases.get(currentPhaseIndex).allows(vehicle.getStartRoad(), vehicle.getEndRoad());
    }


    // TODO do zmiany jakoś inaczej - może wprowadzić ten TrafficLightPhase????
    public Set<DirectionPair> getGreenDirections(){
        return phases.get(currentPhaseIndex).getAllowedMovements();
    }


}
