package pl.project.its;

import pl.project.json.structures.Direction;
import pl.project.its.directions.DirectionPair;
import pl.project.json.structures.Lane;
import pl.project.json.structures.Vehicle;

import java.util.*;




public class Intersection {


    // Change Direction -> DirectionPair - it enables to
    // queues ->
    //          Direction : List<Lane> (eventually new Lanes)
    //                      Lane ->
    //                                Map<Destination (type Direction), Queue<Vehicle>


    private Map<Direction, List<Lane>> lanesPerDirection = new EnumMap<>(Direction.class);

    private final TrafficLightController controller;




    public Intersection(Map<Direction, List<Lane>> lanesPerDirection) {
        this.lanesPerDirection = lanesPerDirection;
        controller = new TrafficLightController(lanesPerDirection);
    }




    public void addVehicle(Vehicle vehicle) {
        Direction start = vehicle.getStartRoad();
        Direction end = vehicle.getEndRoad();

        List<Lane> lanes = this.lanesPerDirection.get(start);

        // TODO dodać własny wyjątek!
        if (lanes == null) {
            throw new IllegalArgumentException("No lanes for direction: " + start);
        }


        // w przyszłości można to rozbudować o dodawanie tam gdzie jest najmniej pojazdów
        // albo losowo - wsm fajna opcja

        Map<Lane, Integer> allowedLanes = new HashMap<>();
//        List<Lane> allowedLanes = new ArrayList<>();

        for (Lane lane : lanes) {

            // TODO do zmiany! - partially done
            if (lane.allows(end)) {
                allowedLanes.put(lane, lane.getVehicles().size());
                System.out.println("\t" + lane + " " + lane.getVehicles().size());
//                allowedLanes.add(lane);
//                lane.addVehicle(vehicle);
//                return;
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
     * Main method to execute step in simulation.
     * returns: leftVehicles
     */
    public List<String> step() {
        List<String> leftVehicles = new ArrayList<>();

        // Update waiting times - some vehicles left the intersection so we need to keep that in mind
        controller.updateWaitingTimes();


        controller.nextStep();
        // directions from current TrafficLightPhase
        Set<DirectionPair> greenDirections = controller.getGreenDirections();

        for (DirectionPair directionPair : greenDirections) {
            System.out.print(" kierunkek: " + directionPair.toString() + " ");
        }

        for (DirectionPair pair : greenDirections) {

            Direction from = pair.getStartRoad();
            Direction to = pair.getEndRoad();

            // pasy skąd jadę
            List<Lane> lanes = lanesPerDirection.get(from);

            for (Lane lane : lanes) {
                if (lane.getAllowedDestinations().contains(to)) {

                    Queue<Vehicle> queue = lane.getVehicles();
                    // może być taki przypadek że z danego pasa można jechać w dwóch lub więcej kierunkach
                    // dlatego musi być druga część warunku
                    if (!queue.isEmpty() && queue.peek().getEndRoad() == to) {
                        Vehicle vehicle = queue.poll();
                        leftVehicles.add(vehicle.getId());
//                        break;
                    }
                }
            }


        }

        System.out.print(" leftVehicles: " + leftVehicles + " \n");
        return leftVehicles;
    }

}
