package pl.project;

import java.util.List;
import java.util.Map;

import pl.project.service.intersection.IntersectionManager;
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
    static String inputFile = "";
    static String outputFile = "output.json";
    static String configFile = "config.json";


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


    public static void parseArguments(String... args) throws Exception {
        // parsing files name
        if (args.length == 0) {
            throw new Exception("No arguments provided. Please specify input file.");
        }
        inputFile = args[0];

        if (args.length > 1) {
            outputFile = args[1];
        }
        if (args.length > 2) {
            configFile = args[2];
        }

    }



    public static void showLanesConfig(Map<Direction, List<Lane>> lanes){
        for (Map.Entry<Direction, List<Lane>> dir : lanes.entrySet()) {
            System.out.println("Direction: " + dir.getKey());
            for (Lane lane : lanes.get(dir.getKey())) {
                System.out.println("  Allowed exits: " + lane.getAllowedDestinations());
            }
        }
    }


    public static StepStatusList mainLogic(CommandList commandList, Map<Direction, List<Lane>> lanes) throws Exception {

        IntersectionManager intersectionManager = new IntersectionManager(lanes);

        StepStatusList stepStatusList = new StepStatusList();

        for (Command cmd : commandList.getCommands()) {
            System.out.print("Type: " + cmd.getType());
            if ("addVehicle".equals(cmd.getType())) {
                System.out.print(" directions: " + cmd.getStartRoad() + " " + cmd.getEndRoad() + "\n");
                intersectionManager.addVehicle(new Vehicle(cmd.getVehicleId(), cmd.getStartRoad(), cmd.getEndRoad()));
            } else if ("addPedestrian".equals(cmd.getType())) {
                System.out.print(" crossing: " + cmd.getCrossingDirection() + "\n");
                intersectionManager.addPedestrian(new Pedestrian(cmd.getVehicleId(), cmd.getCrossingDirection()));
            } else if ("step".equals(cmd.getType())) {
                StepStatus stepStatus = intersectionManager.step();
                stepStatusList.addStep(stepStatus);
            }else{
                throw new Exception("Incorrect command in input file.");
            }
        }
        return stepStatusList;
    }

}
