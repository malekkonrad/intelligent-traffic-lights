package pl.project.its;

import pl.project.its.directions.Direction;
import pl.project.its.directions.DirectionPair;

import java.util.*;

public class TrafficLightController {

    private final List<TrafficLightPhase> phases;
    private int currentPhaseIndex = 0;
    private int phaseStepCounter = 0;

    /**
     * Important to remember is that we are considering DirectionPair not Lanes because there can be multiple lanes in
     * specific DirectionPair (from -> to)
     */
    private final Map<DirectionPair, Integer> waitingTime = new HashMap<>();
    private final int maxWaitTime = 5; // maksymalna liczba kroków oczekiwania
    private final double alpha = 1.0; // waga natężenia
    private final double beta = 0.5; // waga czasu oczekiwania


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
//        currentPhaseIndex = selectBestPhase();
    }


    public void nextStep() {
        currentPhaseIndex = selectBestPhase();






        for (Map.Entry<DirectionPair, Integer> entry : waitingTime.entrySet()){

            // sprawdzam czy któryś DirectionPair czeka za długo
            if (entry.getValue() >= maxWaitTime){

                // Znajduje fazę która zawiera kierunek zbyt długo czekający
                for (int i = 0; i < phases.size(); i++){
                    if (phases.get(i).getAllowedMovements().contains(entry.getKey())){  // entry.getKey() -> DirectionPair
                        currentPhaseIndex = i;
                        phaseStepCounter = 0;
                        return;
                    }
                }
            }
        }


        // adaptacyjny wybór fazy
//        currentPhaseIndex = selectBestPhase();
        phaseStepCounter = 0;
//        updateWaitingTimes();
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



    public void updateWaitingTimes() {

        waitingTime.clear();

        for (Map.Entry<Direction, List<Lane>> entry : queues.entrySet()){
            Direction from = entry.getKey();


            // aktualizuje czas oczekiwania
            for (Lane lane : entry.getValue()){
                for (Vehicle vehicle : lane.getVehicles()){
                    vehicle.setDelay(vehicle.getDelay() + 1);       // TODO dodać dedykowaną metodę
                }


                for (Direction direction: lane.getAllowedDestinations()){
                    DirectionPair directionPair = new DirectionPair(from , direction);
                    waitingTime.put(directionPair, lane.getSumWaitingTime(direction));
                }
            }
        }

    }

    private int selectBestPhase() {
        int bestIndex = 0;
        double bestScore = -1;

        for (int i = 0; i < phases.size(); i++) {
            TrafficLightPhase phase = phases.get(i);
            double score = 0.0;

            for (DirectionPair pair : phase.getAllowedMovements()) {
                int count = countVehicles(pair); // ile aut chce jechać w tym kierunku
                int wait = waitingTime.getOrDefault(pair, 0);

                // większy priorytet dla zatłoczonych i długo czekających
                score += alpha * count + beta * wait;
            }

            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        return bestIndex;
    }


    private int countVehicles(DirectionPair pair) {
        Direction from = pair.getStartRoad();
        Direction to = pair.getEndRoad();
        List<Lane> lanes = queues.get(from);

        int total = 0;
        if (lanes == null) return 0;

        for (Lane lane : lanes) {
            if (lane.getAllowedDestinations().contains(to)) {
                Queue<Vehicle> q = lane.getVehicles();
                if (!q.isEmpty() && q.peek().getEndRoad() == to) {
                    total += q.size();
                }
            }
        }
        return total;
    }






}
