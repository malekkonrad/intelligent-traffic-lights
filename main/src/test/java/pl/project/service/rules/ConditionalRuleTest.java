package pl.project.service.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.project.direction.Direction;
import pl.project.direction.DirectionPair;
import pl.project.service.states.TrafficLightPhase;
import pl.project.service.states.TrafficState;
import pl.project.traffic.pedestrians.Pedestrian;
import java.util.*;
class ConditionalRuleTest {

    private ConditionalRule conditionalRule;
    private DirectionPair directionPair;
    private TrafficState trafficState;
    private TrafficLightPhase trafficLightPhase;
    private Map<Direction, Queue<Pedestrian>> pedestriansPerDirection;
    
    @BeforeEach
    void setUp() {
        Direction startRoad = Direction.NORTH;
        Direction endRoad = Direction.SOUTH;
        directionPair = new DirectionPair(startRoad, endRoad);
        conditionalRule = new ConditionalRule(directionPair);
        
        trafficState = mock(TrafficState.class);
        trafficLightPhase = mock(TrafficLightPhase.class);
        pedestriansPerDirection = new HashMap<>();
        
        for (Direction direction : Direction.values()) {
            pedestriansPerDirection.put(direction, new LinkedList<>());
        }
    }
    
    @Test
    void shouldAllowMovementWhenNoActiveConflicts() {
        // given
        when(trafficLightPhase.getAllowedMovements()).thenReturn(Collections.emptySet());
        when(trafficLightPhase.getPedestrianCrossings()).thenReturn(Collections.emptySet());
        
        // when
        boolean result = conditionalRule.isAllowed(trafficState, trafficLightPhase, pedestriansPerDirection);
        
        // then
        assertTrue(result);
    }
    
    @Test
    void shouldNotAllowMovementWhenVehiclesGoingToSameDestination() {
        // given
        DirectionPair conflictingPair = new DirectionPair(Direction.EAST, Direction.SOUTH); // Same endpoint as directionPair
        Set<DirectionPair> activeMovements = new HashSet<>(Arrays.asList(conflictingPair));
        
        when(trafficLightPhase.getAllowedMovements()).thenReturn(activeMovements);
        when(trafficLightPhase.getPedestrianCrossings()).thenReturn(Collections.emptySet());
        when(trafficState.getVehicleCount(conflictingPair)).thenReturn(5);
        
        // when
        boolean result = conditionalRule.isAllowed(trafficState, trafficLightPhase, pedestriansPerDirection);
        
        // then
        assertFalse(result);
    }
    
    @Test
    void shouldNotAllowMovementWhenPedestriansCrossingStartRoad() {
        // given
        when(trafficLightPhase.getAllowedMovements()).thenReturn(Collections.emptySet());
        Set<Direction> crossings = new HashSet<>(Arrays.asList(Direction.NORTH)); // NORTH is start road
        when(trafficLightPhase.getPedestrianCrossings()).thenReturn(crossings);
        
        Queue<Pedestrian> pedestrians = pedestriansPerDirection.get(Direction.NORTH);
        pedestrians.add(mock(Pedestrian.class));
        
        // when
        boolean result = conditionalRule.isAllowed(trafficState, trafficLightPhase, pedestriansPerDirection);
        
        // then
        assertFalse(result);
    }
    
    @Test
    void shouldNotAllowMovementWhenPedestriansCrossingEndRoad() {
        // given
        when(trafficLightPhase.getAllowedMovements()).thenReturn(Collections.emptySet());
        Set<Direction> crossings = new HashSet<>(Arrays.asList(Direction.SOUTH)); // SOUTH is end road
        when(trafficLightPhase.getPedestrianCrossings()).thenReturn(crossings);
        
        Queue<Pedestrian> pedestrians = pedestriansPerDirection.get(Direction.SOUTH);
        pedestrians.add(mock(Pedestrian.class));
        
        // when
        boolean result = conditionalRule.isAllowed(trafficState, trafficLightPhase, pedestriansPerDirection);
        
        // then
        assertFalse(result);
    }
    
    @Test
    void shouldAllowMovementWhenCrossingWithNoPedestrians() {
        // given
        when(trafficLightPhase.getAllowedMovements()).thenReturn(Collections.emptySet());
        Set<Direction> crossings = new HashSet<>(Arrays.asList(Direction.NORTH, Direction.SOUTH));
        when(trafficLightPhase.getPedestrianCrossings()).thenReturn(crossings);
        
        // No pedestrians in any queue
        
        // when
        boolean result = conditionalRule.isAllowed(trafficState, trafficLightPhase, pedestriansPerDirection);
        
        // then
        assertTrue(result);
    }
}
