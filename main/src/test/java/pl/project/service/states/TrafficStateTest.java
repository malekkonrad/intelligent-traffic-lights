package pl.project.service.states;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.project.direction.Direction;
import pl.project.direction.DirectionPair;
import pl.project.traffic.lanes.Lane;
import pl.project.traffic.pedestrians.Pedestrian;
import pl.project.traffic.vehicles.Vehicle;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;



class TrafficStateTest {

        private TrafficState trafficState;
        private Map<Direction, List<Lane>> lanesPerDirection;
        private Map<Direction, Queue<Pedestrian>> pedestriansPerDirection;

        @BeforeEach
        void setUp() {
            trafficState = new TrafficState();
            lanesPerDirection = new HashMap<>();
            pedestriansPerDirection = new HashMap<>();
        }

        @Test
        void testUpdateWaitingTimesWithEmptyData() {
            trafficState.updateWaitingTimes(lanesPerDirection, pedestriansPerDirection);

            assertEquals(0, trafficState.getVehicleCount(new DirectionPair(Direction.NORTH, Direction.SOUTH)));
            assertEquals(0, trafficState.getTotalWaitingTime(new DirectionPair(Direction.NORTH, Direction.SOUTH)));
        }

        @Test
        void testUpdateWaitingTimesWithVehicles() {
            Lane lane = new Lane(List.of(Direction.NORTH, Direction.SOUTH));
            Vehicle vehicle1 = new Vehicle("1", "north", "south");
            Vehicle vehicle2 = new Vehicle("2", "north", "south");
            lane.addVehicle(vehicle1);
            lane.addVehicle(vehicle2);

            lanesPerDirection.put(Direction.NORTH, Collections.singletonList(lane));

            trafficState.updateWaitingTimes(lanesPerDirection, pedestriansPerDirection);

            DirectionPair pair = new DirectionPair(Direction.NORTH, Direction.SOUTH);
            assertEquals(2, trafficState.getVehicleCount(pair));
            assertEquals(vehicle1.getDelay() + vehicle2.getDelay(), trafficState.getTotalWaitingTime(pair));
        }

        @Test
        void testUpdateWaitingTimesWithPedestrians() {
            Queue<Pedestrian> pedestrians = new LinkedList<>();
            Pedestrian pedestrian1 = new Pedestrian("1", "south");
            Pedestrian pedestrian2 = new Pedestrian("2", "west");
            pedestrians.add(pedestrian1);
            pedestrians.add(pedestrian2);

            pedestriansPerDirection.put(Direction.EAST, pedestrians);

            trafficState.updateWaitingTimes(lanesPerDirection, pedestriansPerDirection);

            assertEquals(0, trafficState.getVehicleCount(new DirectionPair(Direction.EAST, Direction.WEST)));
            assertEquals(0, trafficState.getTotalWaitingTime(new DirectionPair(Direction.EAST, Direction.WEST)));
            assertEquals(1, pedestrian1.getDelay());
            assertEquals(1, pedestrian2.getDelay());
        }
}
