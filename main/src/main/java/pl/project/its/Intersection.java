package pl.project.its;

import pl.project.its.directions.Direction;

import java.util.*;

public class Intersection {

    private final Map<Direction, Queue<Vehicle>> queues = new EnumMap<>(Direction.class);
    private final TrafficLightController controller;

    public Intersection() {
        for (Direction dir : Direction.values()) {
            queues.put(dir, new LinkedList<>());
        }
        controller = new TrafficLightController(queues); // przekazujemy referencję
    }






    public void addVehicle(Vehicle v) {
        queues.get(v.getStartRoad()).add(v);
    }



    public List<String> step() {
        List<String> leftVehicles = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            Queue<Vehicle> queue = queues.get(dir);
            if (!queue.isEmpty()) {
                Vehicle next = queue.peek();
                if (controller.canPass(next)) {
                    leftVehicles.add(next.getId());
                    queue.poll(); // remove from queue
                }
            }
        }

        controller.nextStep();
        return leftVehicles;
    }

}
