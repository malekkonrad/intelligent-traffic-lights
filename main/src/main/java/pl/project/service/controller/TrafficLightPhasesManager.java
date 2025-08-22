package pl.project.service.controller;

import pl.project.direction.Direction;
import pl.project.direction.DirectionPair;
import pl.project.service.states.TrafficLightPhase;

import java.util.List;
import java.util.Set;

public class TrafficLightPhasesManager {


    // na sztywno wpisane fazy
    public List<TrafficLightPhase> phases = List.of(
            // phase 0
            // vehicles: N->S, S->N
            // pedestrians: E, W
            new TrafficLightPhase(Set.of(
                    new DirectionPair(Direction.NORTH, Direction.SOUTH),
                    new DirectionPair(Direction.SOUTH, Direction.NORTH)
            ),
                    Set.of(Direction.EAST, Direction.WEST)

            ),

            // phase 1
            // vehicles: E->W, W->E
            // pedestrians: N, S
            new TrafficLightPhase(Set.of(
                    new DirectionPair(Direction.EAST, Direction.WEST),
                    new DirectionPair(Direction.WEST, Direction.EAST)
            ),
                    Set.of(Direction.NORTH, Direction.SOUTH)

            ),

            // phase 2
            // vehicles: N->S, S->N
            // pedestrians:
            new TrafficLightPhase(Set.of(
                    new DirectionPair(Direction.SOUTH, Direction.WEST),
                    new DirectionPair(Direction.NORTH, Direction.EAST),

                    new DirectionPair(Direction.EAST, Direction.NORTH),
                    new DirectionPair(Direction.WEST, Direction.SOUTH)
            ),
                    Set.of() // lack of default crossing for pedestrian because directions crosses every possible

            ),

            // phase 3
            // vehicles: E->S, W->N, S->E, N->W
            // pedestrians:
            new TrafficLightPhase(Set.of(
                    new DirectionPair(Direction.EAST, Direction.SOUTH),
                    new DirectionPair(Direction.WEST, Direction.NORTH),
                    new DirectionPair(Direction.SOUTH, Direction.EAST),
                    new DirectionPair(Direction.NORTH, Direction.WEST)
            ),
                    Set.of() // lack of default crossing for pedestrian because directions crosses every possible

            )

    );


}
