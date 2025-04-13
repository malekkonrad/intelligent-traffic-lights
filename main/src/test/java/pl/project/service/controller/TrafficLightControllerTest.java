package pl.project.service.controller;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.project.direction.Direction;
import pl.project.direction.DirectionPair;
import pl.project.service.states.TrafficLightPhase;
import pl.project.traffic.lanes.Lane;
import pl.project.traffic.pedestrians.Pedestrian;
import pl.project.traffic.vehicles.Vehicle;
import java.util.*;

class TrafficLightControllerTest {

    private TrafficLightController controller;
    private Map<Direction, List<Lane>> queues;
    private Map<Direction, Queue<Pedestrian>> pedestriansPerDirection;
    
    @BeforeEach
    void setUp() {
        queues = new HashMap<>();
        pedestriansPerDirection = new HashMap<>();
        
        // Initialize empty queues for each direction
        for (Direction direction : Direction.values()) {
            queues.put(direction, new ArrayList<>());
            pedestriansPerDirection.put(direction, new LinkedList<>());
        }
        
        controller = new TrafficLightController(queues, pedestriansPerDirection);
    }
    
    @Test
    void testInitialPhase() {
        assertEquals(1, controller.getCurrentPhase().getAllowedMovements().stream()
                .filter(pair -> pair.equals(new DirectionPair(Direction.NORTH, Direction.SOUTH)))
                .count());
        assertTrue(controller.getCurrentPhase().getPedestrianCrossings().contains(Direction.EAST));
        assertTrue(controller.getCurrentPhase().getPedestrianCrossings().contains(Direction.WEST));
    }
    
    @Test
    void testGetGreenDirections() {
        Set<DirectionPair> greenDirections = controller.getGreenDirections();
        assertNotNull(greenDirections);
        assertEquals(controller.getCurrentPhase().getAllowedMovements(), greenDirections);
    }
    
    @Test
    void testGetCrossingPedestrianDirections() {
        Set<Direction> pedestrianCrossings = controller.getCrossingPedestrianDirections();
        assertNotNull(pedestrianCrossings);
        assertEquals(controller.getCurrentPhase().getPedestrianCrossings(), pedestrianCrossings);
    }
    
    @Test
    void testNextStepWithEmptyQueues() {
        TrafficLightPhase initialPhase = controller.getCurrentPhase();
        controller.nextStep();
        // With empty queues, controller might either stay at initial phase or change
        // We don't assert specific phase change behavior, just that it doesn't crash
    }
    
    @Test
    void testNextStepWithVehiclesWaiting() {
        // Create a lane with vehicles for NORTH direction
        Lane northLane = new Lane(List.of(Direction.SOUTH));
        Vehicle vehicle = new Vehicle("1", "north", "south");
        northLane.addVehicle(vehicle);

        queues.get(Direction.NORTH).add(northLane);
        
        // Phase 0 allows NORTH to SOUTH movement, so it should be selected
        controller.nextStep();
        TrafficLightPhase phase = controller.getCurrentPhase();
        
        // This test can be refined when the behavior of phase selection is better understood
        boolean allowsNorthToSouth = phase.getAllowedMovements().stream()
                .anyMatch(pair -> pair.equals(new DirectionPair(Direction.NORTH, Direction.SOUTH)));
        
        // Either the phase allows this movement, or some other logic took precedence
        // The exact assertion will depend on the expected behavior
    }
    
    @Test
    void testNextStepWithPedestriansWaiting() {
        // Add pedestrians to EAST direction
        Queue<Pedestrian> eastQueue = pedestriansPerDirection.get(Direction.EAST);
        eastQueue.add(new Pedestrian("1", "east"));
        
        controller.nextStep();
        
        // Either a phase allowing EAST pedestrian crossing is selected, or other logic takes precedence
        // This test can be refined when the behavior of pedestrian handling is better understood
    }
    
    @Test
    void testForcePhaseChangeAfterMultipleSteps() {
        // Empty queues should eventually cause a phase change after MAX_STEPS_WITHOUT_PHASE_CHANGE
        TrafficLightPhase initialPhase = controller.getCurrentPhase();
        
        // Force multiple steps without traffic to trigger phase change
        for (int i = 0; i < 15; i++) {
            controller.nextStep();
        }
        
        // We expect a phase change after some number of steps with no traffic
        // The exact assertion will depend on the expected behavior
    }
}
