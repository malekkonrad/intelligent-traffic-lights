package pl.project.traffic.lanes;

import lombok.Getter;
import lombok.Setter;
import pl.project.direction.Direction;
import pl.project.traffic.vehicles.Vehicle;

import java.util.*;

@Getter
public class Lane{

    private final List<Direction> allowedDestinations;
    @Setter
    private Queue<Vehicle> vehicles;


    public Lane(List<Direction> allowedExits) {
        this.allowedDestinations = allowedExits;
        this.vehicles = new LinkedList<>();
    }


    public boolean allows(Direction end) {
        return allowedDestinations.contains(end);
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public int size(Direction endRoad) {
        int sum = 0;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getEndRoad().equals(endRoad)) {
                sum += 1;
            }
        }
        return sum;
    }

    public int getSumWaitingTime(Direction endRoad) {
        int delay = 0;

        if (!vehicles.isEmpty()) {
            if (allowedDestinations.contains(endRoad)) {
                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getEndRoad().equals(endRoad)) {
                        delay += vehicle.getDelay();
                    }
                }
            }
        }

        return delay;
    }

}
