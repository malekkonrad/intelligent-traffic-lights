package pl.project;

import java.util.List;
import java.util.Map;

import pl.project.service.intersection.Intersection;
import pl.project.traffic.pedestrians.Pedestrian;
import pl.project.traffic.lanes.Lane;
import pl.project.traffic.vehicles.Vehicle;
import pl.project.direction.Direction;
import pl.project.json.JsonReader;
import pl.project.json.JsonWriter;
import pl.project.json.structures.input.Command;
import pl.project.json.structures.input.CommandList;
import pl.project.json.structures.output.StepStatus;
import pl.project.json.structures.output.StepStatusList;



/**
 * Main program that runs intelligent traffic lights
 */
public class App 
{
    static String inputFile = "commands/blocked.json";
    static String outputFile = "output.json";
    static String configFile = "config/config.json";


    public static void main( String... args ) throws Exception {

        // parsing files name - input file and output name
        parseArguments(args);

        // reading configuration of intersection - additional feature
        Map<Direction, List<Lane>> lanes = JsonReader.loadLanesFromJson(configFile);

        // displaying current configuration in console
        showLanesConfig(lanes);

        // reading commands from specified file
        CommandList commandList = JsonReader.loadCommandList(inputFile);

        // method which handles main logic - performs a simulation
        StepStatusList stepStatusList = mainLogic(commandList, lanes);

        // saving expected output from simulation to file
        JsonWriter.writeToFile(outputFile ,stepStatusList);

    }


    public static void parseArguments(String... args)  {
        // parsing files name
        try{

            inputFile = args[0];
            outputFile = args[1];

        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    public static void showLanesConfig(Map<Direction, List<Lane>> lanes){
        for (Direction dir : lanes.keySet()) {
            System.out.println("Direction: " + dir);
            for (Lane lane : lanes.get(dir)) {
                System.out.println("  Allowed exits: " + lane.getAllowedDestinations());
            }
        }
    }


    public static StepStatusList mainLogic(CommandList commandList, Map<Direction, List<Lane>> lanes)  {

        Intersection intersection = new Intersection(lanes);

        StepStatusList stepStatusList = new StepStatusList();

        for (Command cmd : commandList.getCommands()) {
            System.out.print("Type: " + cmd.getType());
            if ("addVehicle".equals(cmd.getType())) {
                System.out.print(" directions: " + cmd.getStartRoad() + " " + cmd.getEndRoad() + "\n");
                intersection.addVehicle(new Vehicle(cmd.getVehicleId(), cmd.getStartRoad(), cmd.getEndRoad()));
            } else if ("addPedestrian".equals(cmd.getType())) {
                System.out.print(" crossing: " + cmd.getCrossingDirection() + "\n");
                intersection.addPedestrian(new Pedestrian(cmd.getVehicleId(), cmd.getCrossingDirection()));
            } else{
                StepStatus stepStatus = intersection.step();

                // add step to list that will be saved into json
                stepStatusList.addStep(stepStatus);
            }
        }
        return stepStatusList;
    }

}
