package pl.project.service.intersection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.project.direction.Direction;
import pl.project.direction.DirectionPair;
import pl.project.json.structures.output.StepStatus;
import pl.project.traffic.lanes.Lane;
import pl.project.traffic.pedestrians.Pedestrian;
import pl.project.traffic.vehicles.Vehicle;
import java.util.*;

class IntersectionTest {

    private Intersection intersection;
    private Map<Direction, List<Lane>> lanesPerDirection;

    @BeforeEach
    void setUp() {
        lanesPerDirection = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            List<Lane> lanes = new ArrayList<>();
            // Create a lane that allows all possible destinations
            List<Direction> directions = new ArrayList<>();
            for (Direction dest : Direction.values()) {
                if (direction != dest) {
                    directions.add(dest);

                }
            }
            Lane lane = new Lane(directions);
            lanes.add(lane);
            lanesPerDirection.put(direction, lanes);
        }
        
        intersection = new Intersection(lanesPerDirection);
    }

    @Test
    void testAddVehicle() {
        // Create a vehicle
        Vehicle vehicle = new Vehicle("V1", "north", "south");
        
        // Add vehicle to intersection
        intersection.addVehicle(vehicle);
        
        // Check if the vehicle was added to the correct lane
        Lane lane = lanesPerDirection.get(Direction.NORTH).get(0);
        assertTrue(lane.getVehicles().contains(vehicle));
    }

    @Test
    void testAddVehicleSelectsBestLane() {
        // Create multiple lanes for one direction with different queue lengths
        Lane lane1 = new Lane(List.of(Direction.SOUTH));
        lane1.addVehicle(new Vehicle("V0", "north", "south")); // Add one vehicle to make this less optimal
        Lane lane2 = new Lane(List.of(Direction.SOUTH));

        
        List<Lane> lanes = List.of(lane1, lane2);
        lanesPerDirection.put(Direction.NORTH, lanes);
        
        intersection = new Intersection(lanesPerDirection);
        
        Vehicle vehicle = new Vehicle("V1", "north", "south");
        intersection.addVehicle(vehicle);
        
        // Vehicle should be in lane2 as it has fewer vehicles
        assertTrue(lane2.getVehicles().contains(vehicle));
        assertFalse(lane1.getVehicles().contains(vehicle));
    }

    @Test
    void testAddPedestrian() {
        Pedestrian pedestrian = new Pedestrian("P1", "north");
        intersection.addPedestrian(pedestrian);
        
        // Verify the pedestrian was added by running a step and checking if it leaves
        StepStatus status = intersection.step();
        assertTrue(status.getLeftPedestrians().contains("P1"));
    }

    @Test
    void testStepProcessesVehicles() {
        // Add vehicles from different directions
        Vehicle v1 = new Vehicle("V1", "north", "south");
        Vehicle v2 = new Vehicle("V2", "east", "west");
        
        intersection.addVehicle(v1);
        intersection.addVehicle(v2);
        
        // Run a step and check if any vehicles left
        StepStatus status = intersection.step();
        
        // Due to the traffic light cycle, at least one vehicle should leave
        assertFalse(status.getLeftVehicles().isEmpty());
    }

    @Test
    void testAddVehicleWithInvalidDirection() {
        Vehicle vehicle = new Vehicle("V1", "north", "north");
        
        // Trying to add a vehicle to go from NORTH to NORTH should fail
        assertThrows(IllegalStateException.class, () -> {
            intersection.addVehicle(vehicle);
        });
    }

    @Test
    void testMultipleSteps() {
        // Add many vehicles
        for (int i = 0; i < 10; i++) {
            Vehicle v = new Vehicle("V" + i, "north", "south");
            intersection.addVehicle(v);
        }
        
        int totalVehiclesLeft = 0;
        
        // Run multiple steps
        for (int i = 0; i < 5; i++) {
            StepStatus status = intersection.step();
            totalVehiclesLeft += status.getLeftVehicles().size();
        }
        
        // Check that some vehicles have left
        assertTrue(totalVehiclesLeft > 0);
    }
}
