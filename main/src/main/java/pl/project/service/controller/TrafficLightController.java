package pl.project.service.controller;

import lombok.Getter;
import pl.project.service.states.TrafficLightPhase;
import pl.project.service.states.TrafficState;
import pl.project.traffic.pedestrians.Pedestrian;
import pl.project.direction.Direction;
import pl.project.direction.DirectionPair;
import pl.project.traffic.lanes.Lane;
import pl.project.traffic.vehicles.Vehicle;

import java.util.*;

public class TrafficLightController {


    // IMPORTANT
    private final List<TrafficLightPhase> phases;
    private int currentPhaseIndex = 0;
    private int previousPhaseIndex = 0;

    private int stepsWithoutPhaseChange = 0;


    // History
    private final Deque<Integer> phaseHistory = new LinkedList<>();
    private final int HISTORY_SIZE = 10;


    private final Map<Integer, Double> phaseEffectiveness = new HashMap<>();
    private static final double LEARNING_RATE = 0.1;

    // Statistics for analysis and dynamic adjustment
    private final Map<DirectionPair, Integer> cumulativeWaitTime = new HashMap<>();
    private final Map<DirectionPair, Integer> vehicleServedCount = new HashMap<>();


    //max wait steps
    private final int maxWaitTime = 5;


    private  double alpha = 1.0;    // intensity weight
    private  double beta = 0.5;     // waiting time weight
    private  double gamma = 0.5;    // pedestrians weight


    private final Map<Direction, List<Lane>>  queues;

    @Getter
    private TrafficState trafficState;

    private final Map<Direction, Queue<Pedestrian>> pedestriansPerDirection;


    /**
     * Constructor that contains defined phases of Light Traffic
     * @param queues - read intersection status for vehicles
     * @param pedestriansPerDirection - read intersection status for pedestrians
     */
    public TrafficLightController(Map<Direction, List<Lane>>  queues, Map<Direction, Queue<Pedestrian>> pedestriansPerDirection) {
        this.pedestriansPerDirection = pedestriansPerDirection;
        this.trafficState = new TrafficState();
        this.queues = queues;
        phases = List.of(
                // phase 0
                // vehicles: N->S, S->N
                // pedestrians: E, W
                new TrafficLightPhase(Set.of(
                        new DirectionPair(Direction.NORTH, Direction.SOUTH),
                        new DirectionPair(Direction.SOUTH, Direction.NORTH)
                        ),
                        Set.of(Direction.EAST, Direction.WEST)

                ),

                // phase 1
                // vehicles: E->W, W->E
                // pedestrians: N, S
                new TrafficLightPhase(Set.of(
                        new DirectionPair(Direction.EAST, Direction.WEST),
                        new DirectionPair(Direction.WEST, Direction.EAST)
                        ),
                        Set.of(Direction.NORTH, Direction.SOUTH)

                ),

                // phase 2
                // vehicles: N->S, S->N
                // pedestrians:
                new TrafficLightPhase(Set.of(
                        new DirectionPair(Direction.SOUTH, Direction.WEST),
                        new DirectionPair(Direction.NORTH, Direction.EAST),

                        new DirectionPair(Direction.EAST, Direction.NORTH),
                        new DirectionPair(Direction.WEST, Direction.SOUTH)
                        ),
                        Set.of() // lack of default crossing for pedestrian because directions crosses every possible

                ),

                // phase 3
                // vehicles: E->S, W->N, S->E, N->W
                // pedestrians:
                new TrafficLightPhase(Set.of(
                        new DirectionPair(Direction.EAST, Direction.SOUTH),
                        new DirectionPair(Direction.WEST, Direction.NORTH),
                        new DirectionPair(Direction.SOUTH, Direction.EAST),
                        new DirectionPair(Direction.NORTH, Direction.WEST)
                        ),
                        Set.of() // lack of default crossing for pedestrian because directions crosses every possible

                )

        );


    }


    public TrafficLightPhase getCurrentPhase() {
        return phases.get(currentPhaseIndex);
    }


    /**
     *
     */
    public void nextStep() {

        updateWaitingTimes();
        updateTrafficStatistics();
        adjustWeightFactors();

        previousPhaseIndex = currentPhaseIndex;
        currentPhaseIndex = selectBestPhase();

        // Check if phase changed
        if (currentPhaseIndex == previousPhaseIndex) {
            stepsWithoutPhaseChange++;

            // If too long in the same phase, force a change
            int MAX_STEPS_WITHOUT_PHASE_CHANGE = 10;
            if (stepsWithoutPhaseChange >= MAX_STEPS_WITHOUT_PHASE_CHANGE) { //
                forcePhaseChange();
            }
        } else {
            stepsWithoutPhaseChange = 0;
            updatePhaseHistory(currentPhaseIndex);
            evaluateLastPhaseEffectiveness();
        }

    }


    private int selectBestPhase() {

        int bestIndex = 0;
        double bestScore = -1;

        for (int i = 0; i < phases.size(); i++) {
            // Standard coefficient for the currently considered phase
            double phaseFactor = 1.0;

            double score = evaluatePhase(i, phaseFactor);

            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        return bestIndex;
    }



    public void updateWaitingTimes() {
        trafficState.updateWaitingTimes(queues, pedestriansPerDirection);
    }


    private void adjustWeightFactors() {

        int totalPedestrians = countTotalPedestrians();

        // Mean waiting time
        double avgVehicleWaitTime = calculateAverageVehicleWaitTime();
        double avgPedestrianWaitTime = calculateAveragePedestrianWaitTime();

        Map<DirectionPair, Double> relativeTrafficDensity = calculateRelativeTrafficDensity();

        if (hasTrafficImbalance(relativeTrafficDensity)) {
            // When traffic is uneven (some directions are congested, others are not)
            // we increase the weight of the number of vehicles to prioritize congested directions
            alpha = 1.5;
            beta = 0.8;
        } else if (avgVehicleWaitTime > maxWaitTime * 0.7 || avgPedestrianWaitTime > maxWaitTime * 0.7) {
            // When wait times are high, prioritize them
            alpha = 0.8;
            beta = 1.5;
        } else {
            alpha = 1.0;
            beta = 1.0;
        }

        // Pedestrians coefficient
        if (totalPedestrians > 0 && avgPedestrianWaitTime > avgVehicleWaitTime) {
            gamma = 2.0; // Prioritize
        } else {
            gamma = 1.5; // Standard
        }
    }


    private void forcePhaseChange() {
        // Check if the current phase actually handles traffic
        boolean currentPhaseHasTraffic = false;
        TrafficLightPhase currentPhase = phases.get(currentPhaseIndex);

        for (DirectionPair pair : currentPhase.getAllowedMovements()) {
            if (trafficState.getVehicleCount(pair) > 0) {
                currentPhaseHasTraffic = true;
                break;
            }
        }

        for (Direction direction : currentPhase.getPedestrianCrossings()) {
            Queue<Pedestrian> queue = pedestriansPerDirection.get(direction);
            if (queue != null && !queue.isEmpty()) {
                currentPhaseHasTraffic = true;
                break;
            }
        }

        // If current phase has movement, don't force change
        if (currentPhaseHasTraffic) {
            stepsWithoutPhaseChange = 0;
            return;
        }

        // Only find the best alternative phase if it currently does not support traffic
        int bestAlternativeIndex = -1;
        double bestAlternativeScore = -1;

        for (int i = 0; i < phases.size(); i++) {
            if (i != currentPhaseIndex) {
                double score = evaluatePhase(i, 3);

                if (score > bestAlternativeScore) {
                    bestAlternativeScore = score;
                    bestAlternativeIndex = i;
                }
            }
        }

        if (bestAlternativeIndex != -1 && bestAlternativeScore > 0) {
            currentPhaseIndex = bestAlternativeIndex;
            updatePhaseHistory(currentPhaseIndex);
            stepsWithoutPhaseChange = 0;
        }
    }


    /**
     * Calculates relative traffic intensity in each direction.
     */
    private Map<DirectionPair, Double> calculateRelativeTrafficDensity() {

        Map<DirectionPair, Double> density = new HashMap<>();

        int totalVehicles = countTotalVehicles();

        if (totalVehicles == 0) {
            return density;
        }


        // calculates the percentage of each direction in the total traffic
        for (Map.Entry<DirectionPair, Integer> entry : trafficState.getWaitingVehicles().entrySet()) {
            double relativeDensity = (double) entry.getValue() / totalVehicles;
            density.put(entry.getKey(), relativeDensity);
        }

        return density;
    }



    private boolean hasTrafficImbalance(Map<DirectionPair, Double> relativeDensity) {
        if (relativeDensity.isEmpty()) {
            return false;
        }

        // Expected mean for uniform distribution
        double mean = 1.0 / relativeDensity.size();
        double sumSquaredDiff = 0;

        for (Double density : relativeDensity.values()) {
            double diff = density - mean;
            sumSquaredDiff += diff * diff;
        }

        double stdDev = Math.sqrt(sumSquaredDiff / relativeDensity.size());

        // If the standard deviation is higher than 0.2 (20%), we consider the traffic as non-uniform
        return stdDev > 0.2;
    }







    private int countTotalVehicles() {
        int total = 0;
        for (Integer count : trafficState.getWaitingVehicles().values()) {
            total += count;
        }
        return total;
    }


    private int countTotalPedestrians() {
        int total = 0;
        for (Queue<Pedestrian> queue : pedestriansPerDirection.values()) {
            total += queue.size();
        }
        return total;
    }


    private double calculateAverageVehicleWaitTime() {
        int totalDelay = 0;
        int totalVehicles = 0;

        for (Map.Entry<DirectionPair, Integer> entry : trafficState.getWaitingVehicles().entrySet()) {
            DirectionPair pair = entry.getKey();
            int count = entry.getValue();
            int delay = trafficState.getTotalWaitingTime(pair);

            totalVehicles += count;
            totalDelay += delay;
        }
        return totalVehicles > 0 ? (double) totalDelay / totalVehicles : 0;
    }


    private double calculateAveragePedestrianWaitTime() {
        int totalDelay = 0;
        int totalPedestrians = 0;

        for (Queue<Pedestrian> queue : pedestriansPerDirection.values()) {
            for (Pedestrian pedestrian : queue) {
                totalDelay += pedestrian.getDelay();
                totalPedestrians++;
            }
        }
        return totalPedestrians > 0 ? (double) totalDelay / totalPedestrians : 0;
    }



    private void updateTrafficStatistics() {
        for (DirectionPair pair : trafficState.getWaitingVehicles().keySet()) {
            int waitTime = trafficState.getTotalWaitingTime(pair);

            cumulativeWaitTime.put(pair, cumulativeWaitTime.getOrDefault(pair, 0) + waitTime);

            Set<DirectionPair> allowedDirections = phases.get(currentPhaseIndex).getAllowedMovements();
            if (allowedDirections.contains(pair)) {
                vehicleServedCount.put(pair, vehicleServedCount.getOrDefault(pair, 0) + 1);
            }
        }
    }


    private void updatePhaseHistory(int phaseIndex) {
        phaseHistory.addLast(phaseIndex);
        if (phaseHistory.size() > HISTORY_SIZE) {
            phaseHistory.removeFirst();
        }
    }



    private double calculateStarvationFactor(int phaseIndex) {
        int occurrences = 0;
        for (Integer phase : phaseHistory) {
            if (phase == phaseIndex) occurrences++;
        }

        // Less frequently used phases get a bigger bonus
        return 0.2 * (HISTORY_SIZE - occurrences);
    }



    public Set<DirectionPair> getGreenDirections(){
        return phases.get(currentPhaseIndex).getAllowedMovements();
    }


    public Set<Direction> getCrossingPedestrianDirections(){
        return phases.get(currentPhaseIndex).getPedestrianCrossings();
    }


    /**
     * Crucial method.
     */
    private double evaluatePhase(int phaseIndex, double phaseFactor) {
        TrafficLightPhase phase = phases.get(phaseIndex);
        double score = 0.0;

        // Vehicle Rating
        for (DirectionPair pair : phase.getAllowedMovements()) {
            int vehicleCount = trafficState.getWaitingVehicles().getOrDefault(pair, 0);
            int passableInThisDirection = countPassableVehicles(pair);

            // Calculation of throughput efficiency (how many % of vehicles will be served)
            double throughputEfficiency = vehicleCount > 0 ? (double)passableInThisDirection / vehicleCount : 0;

            int totalDelay = trafficState.getTotalWaitingTime(pair);
            double avgDelay = vehicleCount > 0 ? (double)totalDelay / vehicleCount : 0;

            // Using alpha and beta parameters to weigh the importance of vehicle count and waiting time
            score += (alpha * vehicleCount) + (beta * avgDelay) + (passableInThisDirection * throughputEfficiency);

            // Increase priority for directions close to timeouts
            if (avgDelay > maxWaitTime * 0.8) {
                score *= 1.5;
            }
        }



        // Pedestrian rating
        for (Direction direction : phase.getPedestrianCrossings()) {
            Queue<Pedestrian> pedestrianQueue = pedestriansPerDirection.get(direction);

            if (pedestrianQueue != null && !pedestrianQueue.isEmpty()) {
                int pedestrianCount = pedestrianQueue.size();

                int totalPedestrianDelay = 0;
                for (Pedestrian pedestrian : pedestrianQueue) {
                    totalPedestrianDelay += pedestrian.getDelay();
                }
                double avgPedestrianDelay = (double)totalPedestrianDelay / pedestrianCount;

                // We add to the result with appropriate weight
                score += gamma * (alpha * pedestrianCount + beta * avgPedestrianDelay);

                // Extra priority for pedestrians waiting too long
                if (avgPedestrianDelay > maxWaitTime * 0.7) {
                    score *= 1.3;
                }
            }
        }

        // Avoiding phase starvation - add a bonus for rarely used phases
        double starvationFactor = calculateStarvationFactor(phaseIndex);
        score += starvationFactor;

        // Additional multiplier depending on the parameter passed to the function
        score *= phaseFactor;


        // Take into account the learned phase efficiency
        double effectiveness = phaseEffectiveness.getOrDefault(phaseIndex, 1.0);
        score *= effectiveness;

        return score;
    }


    /**
     * Helper method to count how many vehicles can actually pass in a direction
     */
    private int countPassableVehicles(DirectionPair direction) {
        Direction from = direction.getStartRoad();
        Direction to = direction.getEndRoad();
        int count = 0;

        // Check each lane for this direction
        for (Lane lane : queues.getOrDefault(from, Collections.emptyList())) {
            // Skip lanes that don't go to our destination
            if (!lane.getAllowedDestinations().contains(to)) {
                continue;
            }

            // Count vehicles that can pass (first unblocked vehicle in each lane going to our destination)
            if (!lane.getVehicles().isEmpty()) {
                Vehicle frontVehicle = lane.getVehicles().peek();
                if (frontVehicle.getEndRoad().equals(to) && !frontVehicle.isBlocked()) {
                    count++;
                }
            }
        }

        return count;
    }


    /**
     * Calculate the efficiency of the previous phase.
     */
    private void evaluateLastPhaseEffectiveness() {
        if (previousPhaseIndex != currentPhaseIndex) {
            double effectiveness = calculateEffectiveness(previousPhaseIndex);
            double oldValue = phaseEffectiveness.getOrDefault(previousPhaseIndex, 1.0);
            double newValue = oldValue + LEARNING_RATE * (effectiveness - oldValue);
            phaseEffectiveness.put(previousPhaseIndex, newValue);
        }
    }

    /**
     * We calculate efficiency as the ratio of vehicles served to those waiting.
     */
    private double calculateEffectiveness(int phaseIndex) {
        TrafficLightPhase phase = phases.get(phaseIndex);
        int totalServedInPhase = 0;
        int totalWaitingInPhase = 0;

        for (DirectionPair pair : phase.getAllowedMovements()) {
            int served = vehicleServedCount.getOrDefault(pair, 0);
            int waiting = trafficState.getWaitingVehicles().getOrDefault(pair, 0);

            totalServedInPhase += served;
            totalWaitingInPhase += waiting;
        }

        return totalWaitingInPhase > 0 ? (double)totalServedInPhase / totalWaitingInPhase : 0;
    }

}
