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
    }


    public void nextStep() {

        updateWaitingTimes(phases.get(currentPhaseIndex).getAllowedMovements());





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
        currentPhaseIndex = selectBestPhase();
        phaseStepCounter = 0;


//        phaseStepCounter++;


//
//
//
//
//
//        // dopasowanie długości fazy do natężenia aut na danym kierunku
//        int currentPhaseDuration = estimatePhaseDuration(phases.get(currentPhaseIndex));
//
//        currentPhaseIndex = (currentPhaseIndex + 1) % phases.size();
//
//        // przejście do następnej fazy i wyzerowanie licznika długości obecnej
//        if (phaseStepCounter >= currentPhaseDuration) {
//            currentPhaseIndex = (currentPhaseIndex + 1) % phases.size();
//            phaseStepCounter = 0;
//        }

    }


    /*

    a co jakby liczona jest średnia aut przejeżdzająca w danej fazie i

     */



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



    private void updateWaitingTimes(Set<DirectionPair> greenDirections) {
        for (TrafficLightPhase phase : phases) {
            for (DirectionPair pair : phase.getAllowedMovements()) {
                if (greenDirections.contains(pair)) {
                    waitingTime.put(pair, 0); // zresetuj jeśli przepuszczamy
                } else {
                    waitingTime.put(pair, waitingTime.getOrDefault(pair, 0) + 1);
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
