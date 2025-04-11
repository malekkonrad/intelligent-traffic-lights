package pl.project.its;

import pl.project.its.pedestrian.Pedestrian;
import pl.project.json.structures.Direction;
import pl.project.its.directions.DirectionPair;
import pl.project.json.structures.Lane;
import pl.project.json.structures.Vehicle;
import pl.project.json.structures.output.StepStatus;

import java.util.*;




public class Intersection {


    // Change Direction -> DirectionPair - it enables to
    // queues ->
    //          Direction : List<Lane> (eventually new Lanes)
    //                      Lane ->
    //                                Map<Destination (type Direction), Queue<Vehicle>


    private final Map<Direction, List<Lane>> lanesPerDirection;


    private final Map<Direction, Queue<Pedestrian>> pedestriansPerDirection = new EnumMap<>(Direction.class);

    private final TrafficLightController controller;




    public Intersection(Map<Direction, List<Lane>> lanesPerDirection) {
        this.lanesPerDirection = lanesPerDirection;
        controller = new TrafficLightController(lanesPerDirection);


        for (Direction direction : Direction.values()) {
            pedestriansPerDirection.put(direction, new LinkedList<>());
        }
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

        // TODO dodać własny wyjątek!
        if (lanes == null) {
            throw new IllegalArgumentException("No lanes for direction: " + start);
        }


        Map<Lane, Integer> allowedLanes = new HashMap<>();
        for (Lane lane : lanes) {
            if (lane.allows(end)) {
                allowedLanes.put(lane, lane.getVehicles().size());
                System.out.println("\t" + lane + " " + lane.getVehicles().size());
            }
        }

        Lane bestLane = null;
        if (!allowedLanes.isEmpty()) {
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

        // Update waiting times - some vehicles left the intersection so we need to keep that in mind
        controller.updateWaitingTimes();


        controller.nextStep();

        // directions from current TrafficLightPhase
        Set<DirectionPair> greenDirections = controller.getGreenDirections();

        // displaying green directions
        for (DirectionPair directionPair : greenDirections) {
            System.out.print(" kierunkek: " + directionPair.toString() + " ");
        }


        // Vehicle service on green routes
        for (DirectionPair pair : greenDirections) {
            processVehicles(pair, leftVehicles);
        }


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


        StepStatus stepStatus = new StepStatus(leftVehicles, leftPedestrians);


        System.out.print(" leftVehicles: " + leftVehicles + " " + leftPedestrians + "\n");
        return stepStatus;
    }



    private void processVehicles(DirectionPair pair, List<String> leftVehicles) {
        Direction from = pair.getStartRoad();
        Direction to = pair.getEndRoad();
        List<Lane> lanes = lanesPerDirection.get(from);

        for (Lane lane : lanes) {
            if (lane.getAllowedDestinations().contains(to)) {
                Queue<Vehicle> queue = lane.getVehicles();

                // check if vehicle can pass
                if (!queue.isEmpty() && queue.peek().getEndRoad() == to) {
                    Vehicle vehicle = queue.poll();
                    leftVehicles.add(vehicle.getId());
                }
            }
        }
    }

}
