package pl.project.its;

import pl.project.json.structures.Direction;
import pl.project.its.directions.DirectionPair;
import pl.project.json.structures.Lane;
import pl.project.json.structures.Vehicle;

import java.util.*;

public class TrafficLightController {

    private final List<TrafficLightPhase> phases;
    private int currentPhaseIndex = 0;
    private int phaseStepCounter = 0;

    /**
     * Important to remember is that we are considering DirectionPair not Lanes because there can be multiple lanes in
     * specific DirectionPair (from -> to)
     */
    private final Map<DirectionPair, Integer> waitingVehicles = new HashMap<>();
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

        // do przemyślenia
//        Map<DirectionPair, Set<DirectionPair>> conflictMap = Map.of(
//                new DirectionPair(Direction.NORTH, Direction.SOUTH), Set.of(
//                        new DirectionPair(Direction.WEST, Direction.EAST),
//                        new DirectionPair(Direction.EAST, Direction.WEST),
//                        new DirectionPair(Direction.WEST, Direction.NORTH),
//                        new DirectionPair(Direction.EAST, Direction.SOUTH)
//                )
//        );
    }


    public void nextStep() {
        currentPhaseIndex = selectBestPhase();

        // po wyborze fazy sprawdzamy, czy nie ma pairów z max delayem
        for (Map.Entry<Direction, List<Lane>> entry : queues.entrySet()) {
            for (Lane lane : entry.getValue()) {
                // TODO tutaj w teori nie musiałoby być pęli bo powinniśmy sprawdzać tylko pierwszego auta - reszta nie może mieć większej oczekiwania!
                for (Vehicle vehicle : lane.getVehicles()) {

                    // TODO tutuj będzie zmieniany delay -> lepiej chyba jak on

                    getDynamicMaxWaitTime(vehicle);
                    if (vehicle.getDelay() >= maxWaitTime) {
                        DirectionPair pair = new DirectionPair(vehicle.getStartRoad(), vehicle.getEndRoad());
                        // znajdź fazę, która to obsługuje
                        for (int i = 0; i < phases.size(); i++) {
                            if (phases.get(i).getAllowedMovements().contains(pair)) {
                                currentPhaseIndex = i;
                                phaseStepCounter = 0;
                                return;
                            }
                        }
                    }
                }
            }
        }


        phaseStepCounter = 0;   /// nwm po co to????
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
        waitingVehicles.clear();

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
                    waitingVehicles.put(directionPair, lane.size(direction));
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
//                int count = countVehicles(pair); // ile aut chce jechać w tym kierunku
//                int wait = waitingTime.getOrDefault(pair, 0);
//
//                // większy priorytet dla zatłoczonych i długo czekających
//                score += alpha * count + beta * wait;
                int totalDelay = waitingTime.getOrDefault(pair, 0);
                score += totalDelay;
            }

            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    // TODO czy do usunięcia
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


    private int getDynamicMaxWaitTime(Vehicle vehicle) {

        Direction from = vehicle.getStartRoad();
        Direction to = vehicle.getEndRoad();


        int allVehiclesCount = 0;
        int vehiclesCount = 0;

        TrafficLightPhase myPhase = null;
        for (TrafficLightPhase phase : phases) {
            if (phase.getAllowedMovements().contains(new DirectionPair(from, to))) {
                myPhase = phase;
            }
        }


        for (DirectionPair directionPair : waitingVehicles.keySet()) {
            assert myPhase != null;
            if (myPhase.getAllowedMovements().contains(directionPair)) {
                vehiclesCount += waitingVehicles.get(directionPair);
            }
            allVehiclesCount += waitingVehicles.get(directionPair);
        }

        System.out.println(allVehiclesCount + " " + vehiclesCount);
//
//
//        DirectionPair pair = new DirectionPair(vehicle.getStartRoad(), vehicle.getEndRoad());
//
//        // Znajdź kolidujące kierunki
//        Set<DirectionPair> conflicting = conflictMap.get(pair); // to musisz mieć – zbiór konfliktów
//
//        int totalBlockingVehicles = 0;
//        for (DirectionPair conflict : conflicting) {
//            totalBlockingVehicles += queueSizeForDirectionPair(conflict); // metoda pomocnicza
//        }
//
//        // Im większy ruch w kolidujących kierunkach, tym większy dopuszczalny delay
//        // min. 5, max np. 15 – zależnie od ruchu
//        return Math.min(15, 5 + totalBlockingVehicles);
        return allVehiclesCount;
    }








}
