package pl.project.service.intersection;

import pl.project.service.controller.TrafficLightController;
import pl.project.service.rules.ConditionalRule;
import pl.project.traffic.pedestrians.Pedestrian;
import pl.project.direction.Direction;
import pl.project.direction.DirectionPair;
import pl.project.traffic.lanes.Lane;
import pl.project.traffic.vehicles.Vehicle;
import pl.project.json.structures.output.StepStatus;

import java.util.*;




public class IntersectionManager {


    private final Map<Direction, List<Lane>> lanesPerDirection;
    private final Map<Direction, Queue<Pedestrian>> pedestriansPerDirection = new EnumMap<>(Direction.class);
    private final TrafficLightController controller;

    // defined rules for conditional green light
    private final List<ConditionalRule> conditionalRules = List.of(
            new ConditionalRule(new DirectionPair(Direction.SOUTH, Direction.EAST)),
            new ConditionalRule(new DirectionPair(Direction.EAST, Direction.NORTH)),
            new ConditionalRule(new DirectionPair(Direction.NORTH, Direction.WEST)),
            new ConditionalRule(new DirectionPair(Direction.WEST, Direction.SOUTH))
    );



    public IntersectionManager(Map<Direction, List<Lane>> lanesPerDirection) {
        this.lanesPerDirection = lanesPerDirection;

        for (Direction direction : Direction.values()) {
            pedestriansPerDirection.put(direction, new LinkedList<>());
        }

        controller = new TrafficLightController(lanesPerDirection, pedestriansPerDirection);
    }


    /**
     * Adds vehicles to the lane that leads from defined startRoad to endRoad directions.
     * If there is more than one lane that allows to vehicle to drive in a given direction,
     * will assign vehicle to the lane with the lowest number of cars in front of it
     * @param vehicle -
     */
    public void addVehicle(Vehicle vehicle) {
        Direction start = vehicle.getStartRoad();
        Direction end = vehicle.getEndRoad();

        List<Lane> lanes = this.lanesPerDirection.get(start);

        if (lanes == null) {
            throw new IllegalArgumentException("No lanes for direction: " + start);
        }

        Map<Lane, Integer> allowedLanes = new HashMap<>();
        for (Lane lane : lanes) {
            if (lane.allows(end)) {
                allowedLanes.put(lane, lane.getVehicles().size());
            }
        }

        Lane bestLane = null;
        if (!allowedLanes.isEmpty()) {//optional
            int bestScore = Integer.MAX_VALUE;
            for(Map.Entry<Lane, Integer> entry : allowedLanes.entrySet()) {
                if (entry.getValue() < bestScore) {
                    bestScore = entry.getValue();
                    bestLane = entry.getKey();
                }
            }
            assert bestLane != null;
            bestLane.addVehicle(vehicle);
            return;
        }

        throw new IllegalStateException("No available lane from " + start + " to " + end);

    }


    /**
     * Simple logic
     * @param pedestrian =
     */
    public void addPedestrian(Pedestrian pedestrian) {
        pedestriansPerDirection.get(pedestrian.getCrossingDirection()).add(pedestrian);
    }


    /**
     * Main method to execute step in simulation.
     * returns: leftVehicles
     */
    public StepStatus step() {
        List<String> leftVehicles = new ArrayList<>();
        List<String> leftPedestrians = new ArrayList<>();

        // computing the best phase to handle traffic at intersection
        controller.nextStep();

        // directions from current TrafficLightPhase
        Set<DirectionPair> greenDirections = controller.getGreenDirections();

        // displaying green directions
        System.out.print(" \tdirections:");
        for (DirectionPair directionPair : greenDirections) {
            System.out.print(" " + directionPair.toString() + " ");
        }


        processVehiclesOnGreen(greenDirections);

        processVehiclesOnConditional();

        processPedestrians(leftPedestrians);


        // before vehicles are only blocked and only here deleted from intersection
        deleteVehicles(leftVehicles);


        StepStatus stepStatus = new StepStatus(leftVehicles, leftPedestrians);

        System.out.print(" leftVehicles: " + leftVehicles + " \tleftPedestrians: " + leftPedestrians + "\n");

        return stepStatus;
    }



    private void processVehiclesOnGreen(Set<DirectionPair> greenDirections) {
        // Vehicle service on green routes
        for (DirectionPair pair : greenDirections) {
            processVehicles(pair);
        }
    }


    private void processVehiclesOnConditional() {
        // Handling conditional green arrows
        for (ConditionalRule rule : conditionalRules) {
            if (rule.isAllowed(controller.getTrafficState(), controller.getCurrentPhase(), pedestriansPerDirection)) {
                processVehicles(rule.getDirectionPair());
            }

        }
    }


    private void deleteVehicles(List<String> leftVehicles){
        for (Direction direction : Direction.values()) {
            for (Lane lane : lanesPerDirection.get(direction)) {
                Queue<Vehicle> queue = lane.getVehicles();
                if (!queue.isEmpty() && queue.peek().isBlocked()){
                    Vehicle vehicle = queue.poll();
                    leftVehicles.add(vehicle.getId());
                }
            }
        }
    }


    private void processPedestrians(List<String> leftPedestrians){
        // Handling pedestrian crossing intersection
        Set<Direction> allowedCrossings = controller.getCrossingPedestrianDirections();
        if (!allowedCrossings.isEmpty()) {
            for (Direction direction : allowedCrossings) {
                Queue<Pedestrian> queue = pedestriansPerDirection.get(direction);
                if (!queue.isEmpty()) {
                    Pedestrian pedestrian = queue.poll();
                    leftPedestrians.add(pedestrian.getId());
                }
            }
        }
    }


    private void processVehicles(DirectionPair pair) {
        Direction from = pair.getStartRoad();
        Direction to = pair.getEndRoad();
        List<Lane> lanes = lanesPerDirection.get(from);

        for (Lane lane : lanes) {
            if (lane.getAllowedDestinations().contains(to)) {
                Queue<Vehicle> queue = lane.getVehicles();
                // check if vehicle can pass
                if (!queue.isEmpty() && queue.peek().getEndRoad() == to && !queue.peek().isBlocked()) {
                    queue.peek().setBlocked(true);
                }
            }
        }
    }

}
