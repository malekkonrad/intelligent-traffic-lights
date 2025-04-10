package pl.project.its;

import lombok.Getter;
import lombok.Setter;
import pl.project.its.directions.Direction;

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
