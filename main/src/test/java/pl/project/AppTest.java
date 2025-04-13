package pl.project;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import pl.project.direction.Direction;
import pl.project.json.JsonReader;
import pl.project.json.JsonWriter;
import pl.project.json.structures.input.Command;
import pl.project.json.structures.input.CommandList;
import pl.project.json.structures.output.StepStatus;
import pl.project.json.structures.output.StepStatusList;
import pl.project.service.intersection.Intersection;
import pl.project.traffic.lanes.Lane;


public class AppTest  {


    @Test
    public void testParseArguments() throws Exception {
        String[] args = {"input.json", "output.json"};
        App.parseArguments(args);
        assertEquals("input.json", App.inputFile);
        assertEquals("output.json", App.outputFile);
    }
    @Test
    public void testParseArgumentsWithEmptyArray() {
        // Arrange
        String[] emptyArray = new String[0];

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            App.parseArguments(emptyArray);
        });

        // Verify exception message (optional)
        assertEquals("No arguments provided. Please specify input file.", exception.getMessage());
    }
    @Test
    public void testShowLanesConfig() {
        // This test mainly ensures the method doesn't throw exceptions
        Map<Direction, List<Lane>> lanes = new HashMap<>();
        lanes.put(Direction.NORTH, new ArrayList<>());
        App.showLanesConfig(lanes);
    }

    @Test
    public void testMainLogic() throws Exception {
        // Mock objects
        CommandList commandList = mock(CommandList.class);

        Map<Direction, List<Lane>> lanes = new HashMap<>();

        // Initialize lanes for each direction
        for (Direction direction : Direction.values()) {
            List<Lane> dirLanes = new ArrayList<>();

            List<Direction> allowedDestinations = new ArrayList<>();
            for (Direction dest : Direction.values()) {
                if (direction != dest) {
                    allowedDestinations.add(dest);
                }
            }

            Lane lane = new Lane(allowedDestinations);
            dirLanes.add(lane);
            lanes.put(direction, dirLanes);
        }
        // Mock command list
        List<Command> commands = new ArrayList<>();
        Command stepCommand = mock(Command.class);
        when(stepCommand.getType()).thenReturn("step");
        commands.add(stepCommand);

        when(commandList.getCommands()).thenReturn(commands);

        // Execute and verify
        StepStatusList result = App.mainLogic(commandList, lanes);
        assertNotNull(result);
        assertEquals(1, result.getStepStatuses().size());
    }


    @Test
    public void testMainLogicWithVehicleCommand() throws Exception {
        // Mock objects
        CommandList commandList = mock(CommandList.class);

        Map<Direction, List<Lane>> lanes = new HashMap<>();

        // Initialize lanes for each direction
        for (Direction direction : Direction.values()) {
            List<Lane> dirLanes = new ArrayList<>();
            List<Direction> allowedDestinations = new ArrayList<>();
            for (Direction dest : Direction.values()) {
                if (direction != dest) {
                    allowedDestinations.add(dest);
                }
            }

            Lane lane = new Lane(allowedDestinations);
            dirLanes.add(lane);
            lanes.put(direction, dirLanes);
        }

        // Mock command list
        List<Command> commands = new ArrayList<>();
        Command vehicleCommand = mock(Command.class);
        when(vehicleCommand.getType()).thenReturn("addVehicle");
        when(vehicleCommand.getVehicleId()).thenReturn("vehicle1");

        when(vehicleCommand.getStartRoad()).thenReturn("north");
        when(vehicleCommand.getEndRoad()).thenReturn("south");

        commands.add(vehicleCommand);

        when(commandList.getCommands()).thenReturn(commands);

        // Execute and verify
        StepStatusList result = App.mainLogic(commandList, lanes);
        assertNotNull(result);
        assertEquals(0, result.getStepStatuses().size()); // No step command was added
    }

    @Test
    public void testMainLogicWithPedestrianCommand() throws Exception {
        // Mock objects
        CommandList commandList = mock(CommandList.class);
        Map<Direction, List<Lane>> lanes = new HashMap<>();
        
        // Initialize lanes for each direction
        for (Direction direction : Direction.values()) {
            lanes.put(direction, new ArrayList<>());
        }
        
        // Mock command list
        List<Command> commands = new ArrayList<>();
        Command pedestrianCommand = mock(Command.class);
        
        // Mock command list
        when(pedestrianCommand.getType()).thenReturn("addPedestrian");
        when(pedestrianCommand.getVehicleId()).thenReturn("addPedestrian1");
        when(pedestrianCommand.getCrossingDirection()).thenReturn(String.valueOf(Direction.NORTH));
        commands.add(pedestrianCommand);
        
        when(commandList.getCommands()).thenReturn(commands);

        // Execute and verify
        StepStatusList result = App.mainLogic(commandList, lanes);
        assertNotNull(result);
        assertEquals(0, result.getStepStatuses().size()); // No step command was added
    }

    @Test
    public void testMainLogicWithStepCommand() throws Exception {
        // Mock objects
        CommandList commandList = mock(CommandList.class);
        Map<Direction, List<Lane>> lanes = new HashMap<>();

        // Initialize lanes for each direction
        for (Direction direction : Direction.values()) {
            List<Lane> dirLanes = new ArrayList<>();
            List<Direction> allowedDestinations = new ArrayList<>();
            for (Direction dest : Direction.values()) {
                if (direction != dest) {
                    allowedDestinations.add(dest);
                }
            }
            dirLanes.add(new Lane(allowedDestinations));
            lanes.put(direction, dirLanes);
        }

        // Mock command list
        List<Command> commands = new ArrayList<>();
        Command stepCommand = mock(Command.class);
        when(stepCommand.getType()).thenReturn("step");
        commands.add(stepCommand);

        when(commandList.getCommands()).thenReturn(commands);

        // Execute and verify
        StepStatusList result = App.mainLogic(commandList, lanes);

        // Check that result is not null and contains one step
        assertNotNull(result);
        assertEquals(1, result.getStepStatuses().size());
    }

    @Test
    public void testCompleteMainLogicWithMultipleCommands() throws Exception {
        // Mock objects
        CommandList commandList = mock(CommandList.class);
        Map<Direction, List<Lane>> lanes = new HashMap<>();

        // Initialize lanes
        for (Direction direction : Direction.values()) {
            List<Lane> dirLanes = new ArrayList<>();
            List<Direction> allowedDestinations = new ArrayList<>();
            for (Direction dest : Direction.values()) {
                if (direction != dest) {
                    allowedDestinations.add(dest);
                }
            }
            dirLanes.add(new Lane(allowedDestinations));
            lanes.put(direction, dirLanes);
        }

        // Mock command list with multiple commands
        List<Command> commands = new ArrayList<>();

        // Vehicle command
        Command vehicleCommand = mock(Command.class);
        when(vehicleCommand.getType()).thenReturn("addVehicle");
        when(vehicleCommand.getVehicleId()).thenReturn("vehicle1");
        when(vehicleCommand.getStartRoad()).thenReturn("north");
        when(vehicleCommand.getEndRoad()).thenReturn("south");
        commands.add(vehicleCommand);

        // Pedestrian command
        Command pedestrianCommand = mock(Command.class);
        when(pedestrianCommand.getType()).thenReturn("addPedestrian");
        when(pedestrianCommand.getVehicleId()).thenReturn("pedestrian1");
        when(pedestrianCommand.getCrossingDirection()).thenReturn(String.valueOf(Direction.EAST));
        commands.add(pedestrianCommand);

        // Step command
        Command stepCommand = mock(Command.class);
        when(stepCommand.getType()).thenReturn("step");
        commands.add(stepCommand);

        when(commandList.getCommands()).thenReturn(commands);

        // Execute and verify
        StepStatusList result = App.mainLogic(commandList, lanes);
        assertNotNull(result);
        assertEquals(1, result.getStepStatuses().size());
    }


}
