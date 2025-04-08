package pl.project.its;

import pl.project.its.directions.Direction;
import pl.project.its.directions.DirectionPair;

import java.util.*;




public class Intersection {


    // Change Direction -> DirectionPair - it enables to
    // queues ->
    //          Direction : List<Lane> (eventually new Lanes)
    //                      Lane ->
    //                                Map<Destination (type Direction), Queue<Vehicle>

    private Map<Direction, List<Lane>> lanesPerDirection = new EnumMap<>(Direction.class);

    private final TrafficLightController controller;




    public Intersection(Map<Direction, List<Lane>> newQueues) {
        lanesPerDirection = newQueues;
        controller = new TrafficLightController(newQueues); // przekazujemy referencję
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
        for (Lane lane : lanes) {

            // TODO do zmiany!
            if (lane.allows(end)) {
                lane.addVehicle(vehicle);
                return;
            }
        }

        throw new IllegalStateException("No available lane from " + start + " to " + end);

    }



    public List<String> step() {
        List<String> leftVehicles = new ArrayList<>();

        // directions from current TrafficLightPhase
        Set<DirectionPair> greenDirections = controller.getGreenDirections();


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
                        break;
                    }
                }
            }
        }

        controller.nextStep(); // zmień fazę świateł
        return leftVehicles;
    }

}
