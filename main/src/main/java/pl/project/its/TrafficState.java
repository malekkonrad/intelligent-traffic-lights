package pl.project.its;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.project.its.directions.DirectionPair;
import pl.project.json.structures.Direction;
import pl.project.json.structures.Lane;
import pl.project.json.structures.Vehicle;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class TrafficState {

    private final Map<DirectionPair, Integer> waitingVehicles = new HashMap<>();
    private final Map<DirectionPair, Integer> waitingTime = new HashMap<>();


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
