package pl.project.service.states;

import lombok.Getter;
import pl.project.direction.DirectionPair;
import pl.project.direction.Direction;
import pl.project.traffic.lanes.Lane;
import pl.project.traffic.vehicles.Vehicle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class TrafficState {

    private final Map<DirectionPair, Integer> waitingVehicles = new HashMap<>();
    private final Map<DirectionPair, Integer> waitingTime = new HashMap<>();

    /**
     * Updates waiting times for every DirectionPair.
     */
    public void updateWaitingTimes(Map<Direction, List<Lane>> lanesPerDirection) {

        waitingTime.clear();
        waitingVehicles.clear();

        for (Map.Entry<Direction, List<Lane>> entry : lanesPerDirection.entrySet()){
            Direction from = entry.getKey();

            for (Lane lane : entry.getValue()){

                for (Vehicle vehicle : lane.getVehicles()){
                    vehicle.incrementDelay();
                }

                for (Direction to: lane.getAllowedDestinations()){

                    DirectionPair pair = new DirectionPair(from , to);
                    int vehicleCount = lane.size(to);
                    int totalDelay = lane.getSumWaitingTime(to);

                    waitingVehicles.put(pair, waitingVehicles.getOrDefault(pair, 0) + vehicleCount);
                    waitingTime.put(pair, waitingTime.getOrDefault(pair, 0) + totalDelay);

                }
            }
        }

    }

    public int getVehicleCount(DirectionPair pair) {
        return waitingVehicles.getOrDefault(pair, 0);
    }

    public int getTotalWaitingTime(DirectionPair pair) {
        return waitingTime.getOrDefault(pair, 0);
    }


}
