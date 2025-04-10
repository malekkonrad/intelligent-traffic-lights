package pl.project;

import java.util.List;
import java.util.Map;

import pl.project.its.Intersection;
import pl.project.its.lane.Lane;
import pl.project.its.lane.LaneManager;
import pl.project.its.Vehicle;
import pl.project.its.directions.Direction;
import pl.project.json.JsonReader;
import pl.project.json.JsonWriter;
import pl.project.json.structures.input.Command;
import pl.project.json.structures.input.CommandList;
import pl.project.json.structures.output.StepStatusList;



/**
 * Main program that runs intelligent traffic lights
 */
public class App 
{
    static String inputFile = "duzo_na_raz.json";
    static String outputFile = "output.json";


    public static void main( String... args ) throws Exception {

        // parsing files name
        try{

            inputFile = args[0];
            outputFile = args[1];

        }catch (Exception e) {
            System.out.println(e.getMessage());
        }





        // wczytanie ustawień skrzyżowania!
        LaneManager laneManager = new LaneManager();
        laneManager.loadFromJson("config.json");

        Map<Direction, List<Lane>> lanes = laneManager.getLanesPerDirection();
        for (Direction dir : lanes.keySet()) {
            System.out.println("Direction: " + dir);
            for (Lane lane : lanes.get(dir)) {
                System.out.println("  Allowed exits: " + lane.getAllowedDestinations());
            }
        }

        // wczytanie listy komend
        JsonReader jsonReader = new JsonReader(inputFile);
        CommandList commandList = jsonReader.read();

        // Obiekt w którym będę zapisywał statusy
        StepStatusList stepStatusList = new StepStatusList();


        Intersection intersection = new Intersection(laneManager.getLanesPerDirection());

        // Przejście po komendach
        for (Command cmd : commandList.getCommands()) {
            System.out.print("Type: " + cmd.getType());
            if ("addVehicle".equals(cmd.getType())) {
                System.out.print(" directions: " + cmd.getStartRoad() + " " + cmd.getEndRoad() + "\n");
                intersection.addVehicle(new Vehicle(cmd.getVehicleId(),0, cmd.getStartRoad(), cmd.getEndRoad()));
            }else{
                List<String> leftVehicle = intersection.step();

                // add step to list that will be saved into json
                stepStatusList.addStep(leftVehicle);
            }
        }


        // zapis
        JsonWriter jsonWriter = new JsonWriter(outputFile);
        jsonWriter.writeToFile(stepStatusList);


    }
}
